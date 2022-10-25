/*
 * Copyright 2022 yihtserns.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.yihtserns.spring.remoting.jsonrpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonRpcServiceExporter implements HttpRequestHandler, BeanFactoryAware, InitializingBean {

    private Map<String, Method> name2Method = new HashMap<>();
    private ObjectReader objectReader;
    private ObjectWriter objectWriter;

    @Setter
    private Class<?> serviceInterface;
    @Setter
    private Object service;
    @Setter
    private ObjectMapper objectMapper = new ObjectMapper();
    private ConversionService conversionService;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.conversionService = beanFactory.getBeanProvider(ConversionService.class)
                .getIfAvailable(() -> NoOpConversionService.INSTANCE);
    }

    @Override
    public void afterPropertiesSet() {
        if (service == null) {
            throw new IllegalArgumentException("Property 'service' is required");
        }
        if (serviceInterface == null) {
            throw new IllegalArgumentException("Property 'serviceInterface' is required");
        }
        if (!serviceInterface.isInstance(service)) {
            throw new IllegalArgumentException(String.format(
                    "Service interface [%s] needs to be implemented by service [%s] of class [%s]",
                    serviceInterface.getName(),
                    service,
                    service.getClass().getName()));
        }

        objectReader = objectMapper.reader(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectWriter = objectMapper.writer();

        for (Method method : serviceInterface.getMethods()) {
            if (name2Method.containsKey(method.getName())) {
                throw new IllegalArgumentException("Duplicate method name is not supported: " + method.getName());
            }
            name2Method.put(method.getName(), method);
        }
    }

    @Override
    public void handleRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException { // TODO: Handle IOException
        ServletInputStream inputStream = httpRequest.getInputStream();

        JsonRpcRequest request = null;
        Method method = null;
        boolean returnResponse = true;
        JsonRpcResponse response = new JsonRpcResponse();

        try {
            request = objectReader.readValue(inputStream, JsonRpcRequest.class);
            response.setId(request.getId());

            method = name2Method.get(request.getMethod());
            returnResponse = !StringUtils.isEmpty(request.getId());
        } catch (StreamReadException ex) {
            response.setError(JsonRpcResponse.Error.parseError());
        }

        if (request != null) {
            try {
                if (method == null) {
                    response.setError(JsonRpcResponse.Error.methodNotFound());
                } else {
                    if (request.getParams().isArray()) {
                        executeArrayParamsMethod(request, response, method);
                    } else if (request.getParams().isObject()) {
                        executeObjectParamsMethod(request, response, method);
                    } else {
                        returnResponse = true;
                        response.setError(JsonRpcResponse.Error.invalidRequest());
                    }
                }
            } catch (IllegalArgumentException ex) {
                log.debug("Error when trying to call method: {}", request.getMethod(), ex);
                response.setError(JsonRpcResponse.Error.invalidParams());
            } catch (InvocationTargetException ex) {
                if (conversionService.canConvert(ex.getCause().getClass(), JsonRpcResponse.Error.class)) {
                    response.setError(conversionService.convert(ex.getCause(), JsonRpcResponse.Error.class));

                    if (response.getError().getCode() <= -32000 && response.getError().getCode() >= -32768) {
                        log.error("Exception [{}] was converted into Error object using a reserved error code: {}",
                                ex.getCause(),
                                response.getError().getCode());

                        response.setError(JsonRpcResponse.Error.internalError());
                    }
                } else {
                    log.error("Error thrown by method: {}", method, ex.getCause());
                    response.setError(JsonRpcResponse.Error.internalError());
                }
            } catch (IllegalAccessException | InstantiationException | RuntimeException ex) {
                log.error("Failed to call method: {}", request.getMethod(), ex);
                response.setError(JsonRpcResponse.Error.internalError());
            }
        }

        if (!returnResponse) {
            httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
        } else {
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectWriter.writeValue(httpResponse.getOutputStream(), response);
        }
    }

    private void executeArrayParamsMethod(JsonRpcRequest request, JsonRpcResponse response, Method method) throws IllegalAccessException, InvocationTargetException {
        JsonNode requestParameters = request.getParams();
        Parameter[] methodParameters = method.getParameters();

        if (requestParameters.size() != methodParameters.length) {
            // TODO: Better error message & error response
            log.error("Expecting array params of length {} but was {}", methodParameters.length, requestParameters.size());
            response.setError(JsonRpcResponse.Error.invalidParams());
            return;
        }

        try {
            List<Object> params = new ArrayList<>();
            for (int i = 0; i < methodParameters.length; i++) {
                Parameter methodParameter = methodParameters[i];
                JsonNode param = request.getParams().get(i);

                // TODO: Can support method parameter annotation e.g. @JsonFormat?
                Object methodArgument = objectReader.treeToValue(
                        param,
                        objectReader.getTypeFactory().constructType(methodParameter.getParameterizedType()));

                params.add(methodArgument);
            }

            response.setResult(method.invoke(service, params.toArray()));
        } catch (JsonProcessingException ex) {
            // TODO: Better error message & error response
            log.error("Failed to convert params entry type to method argument type", ex);
            response.setError(JsonRpcResponse.Error.invalidParams());
        }
    }

    private void executeObjectParamsMethod(JsonRpcRequest request, JsonRpcResponse response, Method method) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        // TODO: param count != 1 --> throw
        try {
            Object beanArg = objectReader.treeToValue(request.getParams(), method.getParameterTypes()[0]);

            response.setResult(method.invoke(service, beanArg));
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}

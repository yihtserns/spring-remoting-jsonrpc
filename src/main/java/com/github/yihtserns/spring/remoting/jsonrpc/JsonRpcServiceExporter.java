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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
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

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

@Slf4j
public class JsonRpcServiceExporter implements HttpRequestHandler, InitializingBean {

    private final Map<String, Method> name2Method = new HashMap<>();
    private ObjectReader objectReader;
    private ObjectWriter objectWriter;

    @Setter
    private Class<?> serviceInterface;
    @Setter
    private Object service;
    @Setter
    private ObjectMapper objectMapper = new ObjectMapper();
    @Setter
    private ExceptionHandler exceptionHandler = new DefaultExceptionHandler();

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
    public void handleRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        JsonRpcRequest request = null;
        Method method = null;
        boolean returnResponse = true;
        JsonRpcResponse response = new JsonRpcResponse();

        try {
            ServletInputStream inputStream = httpRequest.getInputStream();
            request = objectReader.readValue(inputStream, JsonRpcRequest.class);
            response.setId(request.getId());

            if (request.getMethod() == null) {
                response.setError(JsonRpcResponse.Error.invalidRequest());
                request = null;
            } else {
                method = name2Method.get(request.getMethod());
                returnResponse = !StringUtils.isEmpty(request.getId());
            }
        } catch (IOException ex) {
            log.debug("An error occurred when trying to read the request body", ex);
            response.setError(JsonRpcResponse.Error.parseError());
        }

        if (request != null) {
            try {
                if (method == null) {
                    response.setError(JsonRpcResponse.Error.methodNotFound());
                } else {
                    if (request.getParams() == null || request.getParams().isNull()) {
                        executeMethod(
                                emptyList(),
                                response,
                                method);
                    } else if (request.getParams().isArray()) {
                        executeMethod(
                                stream(request.getParams().spliterator(), false).collect(toList()),
                                response,
                                method);
                    } else if (request.getParams().isObject()) {
                        executeMethod(
                                singletonList(request.getParams()),
                                response,
                                method);
                    } else {
                        log.error("Expected params of type JSON array or object, but was: {}", request.getParams());
                        returnResponse = true;
                        response.setError(JsonRpcResponse.Error.invalidRequest());
                    }
                }
            } catch (InvocationTargetException ex) {
                JsonRpcResponse.Error error = exceptionHandler.handleException(ex.getCause(), method);
                if (error.getCode() <= -32000 && error.getCode() >= -32768) {
                    log.error("Exception [{}] was converted into Error object using a reserved error code: {}",
                            ex.getCause(),
                            error.getCode());

                    error = JsonRpcResponse.Error.internalError();
                }
                response.setError(error);
            } catch (IllegalAccessException | RuntimeException ex) {
                log.error("Failed to call method: {}", request.getMethod(), ex);
                response.setError(JsonRpcResponse.Error.internalError());
            }
        }

        if (!returnResponse) {
            httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
        } else {
            try {
                httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);

                ServletOutputStream outputStream = httpResponse.getOutputStream();
                objectWriter.writeValue(outputStream, response);
            } catch (IOException ex) {
                log.error("An error has occurred while trying to write the response body", ex);
                httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }
    }

    private void executeMethod(List<JsonNode> requestParameters, JsonRpcResponse response, Method method) throws IllegalAccessException, InvocationTargetException {
        Parameter[] methodParameters = method.getParameters();

        if (requestParameters.size() != methodParameters.length) {
            log.error("Expecting params of length {} as arguments for method {}, but was {}",
                    methodParameters.length,
                    method,
                    requestParameters.size());
            response.setError(JsonRpcResponse.Error.invalidParams());
            return;
        }

        List<Object> params = new ArrayList<>();
        for (int i = 0; i < methodParameters.length; i++) {
            Parameter methodParameter = methodParameters[i];
            JsonNode param = requestParameters.get(i);

            try {
                Object methodArgument = objectReader.treeToValue(
                        param,
                        objectReader.getTypeFactory().constructType(methodParameter.getParameterizedType()));

                params.add(methodArgument);
            } catch (JsonProcessingException ex) {
                log.error("Failed to convert params #{} to method [{}] argument [{}]", i, method, methodParameter, ex);
                response.setError(JsonRpcResponse.Error.invalidParams());
                return;
            }
        }

        response.setResult(method.invoke(service, params.toArray()));
    }
}

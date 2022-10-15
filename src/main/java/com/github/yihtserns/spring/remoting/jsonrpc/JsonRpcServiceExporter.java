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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.NullValueInNestedPathException;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.MethodParameter;
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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonRpcServiceExporter implements HttpRequestHandler, BeanFactoryAware, InitializingBean {

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private Map<String, Method> name2Method = new HashMap<>();

    @Setter
    private Class<?> serviceInterface;
    @Setter
    private Object service;
    private ConversionService conversionService;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.conversionService = beanFactory.getBean(ConversionService.class);
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

        for (Method method : serviceInterface.getMethods()) {
            if (name2Method.containsKey(method.getName())) {
                throw new IllegalArgumentException("Duplicate method name is not supported: " + method.getName());
            }
            name2Method.put(method.getName(), method);
        }
    }

    @Override
    public void handleRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        ServletInputStream inputStream = httpRequest.getInputStream();

        JsonRpcRequest request = objectMapper.readValue(inputStream, JsonRpcRequest.class);
        JsonRpcResponse response = new JsonRpcResponse();
        response.setId(request.getId());

        Method method = name2Method.get(request.getMethod());
        boolean returnResponse = !StringUtils.isEmpty(request.getId());

        try {
            if (method == null) {
                response.setError(JsonRpcResponse.Error.methodNotFound());
            } else {
                if (request.getParams() instanceof List) {
                    executeArrayParamsMethod(request, response, method);
                } else if (request.getParams() instanceof Map) {
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
        } catch (IllegalAccessException | RuntimeException ex) {
            log.error("Failed to call method: {}", request.getMethod(), ex);
            response.setError(JsonRpcResponse.Error.internalError());
        }

        if (!returnResponse) {
            httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
        } else {
            httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(httpResponse.getOutputStream(), response);
        }
    }

    private void executeArrayParamsMethod(JsonRpcRequest request, JsonRpcResponse response, Method method) throws IllegalAccessException, InvocationTargetException {
        List<Object> requestParameters = (List) request.getParams();
        Parameter[] methodParameters = method.getParameters();

        if (requestParameters.size() != methodParameters.length) {
            // TODO: Better error message & error response
            log.error("Expecting array params of length {} but was {}", methodParameters.length, requestParameters.size());
            response.setError(JsonRpcResponse.Error.invalidParams());
            return;
        }

        SimpleTypeConverter typeConverter = new SimpleTypeConverter();
        typeConverter.registerCustomEditor(OffsetDateTime.class, null, new OffsetDateTimeEditor());
        try {
            List<Object> params = new ArrayList<>();
            for (int i = 0; i < methodParameters.length; i++) {
                Parameter methodParameter = methodParameters[i];
                Object rawMethodArgument = requestParameters.get(i);

                Object methodArgument = typeConverter.convertIfNecessary(
                        rawMethodArgument,
                        methodParameter.getType(),
                        MethodParameter.forParameter(methodParameter));

                params.add(methodArgument);
            }

            response.setResult(method.invoke(service, params.toArray()));
        } catch (TypeMismatchException ex) {
            // TODO: Better error message & error response
            log.error("Failed to convert params entry type to method argument type", ex);
            response.setError(JsonRpcResponse.Error.invalidParams());
        }
    }

    private void executeObjectParamsMethod(JsonRpcRequest request, JsonRpcResponse response, Method method) throws InvocationTargetException {
        // TODO: When empty object
        try {
            Map<String, Object> propertyName2Value = (Map) request.getParams();

            Object beanArg = method.getParameters()[0].getType().newInstance();
            BeanWrapper bean = PropertyAccessorFactory.forBeanPropertyAccess(beanArg);
            bean.registerCustomEditor(OffsetDateTime.class, new OffsetDateTimeEditor());

            // TODO: When property not found
            // TODO: When property count does not match bean property count
            propertyName2Value.forEach(bean::setPropertyValue);

            response.setResult(method.invoke(service, beanArg));
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new IllegalArgumentException("TODO: Return error");
        } catch (ConversionNotSupportedException | NullValueInNestedPathException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}

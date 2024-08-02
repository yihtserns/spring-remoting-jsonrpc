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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonRpcServiceExporter implements HttpRequestHandler, InitializingBean {

    private final Map<String, Method> name2Method = new HashMap<>();

    @Setter
    private Class<?> serviceInterface;
    @Setter
    private Object service;
    @Setter
    private JsonProcessor jsonProcessor;
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
        if (jsonProcessor == null) {
            throw new IllegalArgumentException("Property 'jsonProcessor' is required");
        }

        for (Method method : serviceInterface.getMethods()) {
            if (name2Method.containsKey(method.getName())) {
                throw new IllegalArgumentException("Duplicate method name is not supported: " + method.getName());
            }
            name2Method.put(method.getName(), method);
        }
    }

    @Override
    public void handleRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        JsonRpcRequest<?> request = null;
        Method method = null;
        boolean returnResponse = true;
        JsonRpcResponse response = new JsonRpcResponse();

        try {
            ServletInputStream inputStream = httpRequest.getInputStream();
            request = jsonProcessor.processRequest(inputStream);
            response.setId(request.getId());

            if (request.getMethod() == null) {
                response.setError(JsonRpcResponse.Error.invalidRequest());
                request = null;
            } else {
                method = name2Method.get(request.getMethod());
                returnResponse = !StringUtils.isEmpty(request.getId());
            }
        } catch (Exception ex) {
            log.debug("An error occurred when trying to read the request body", ex);
            response.setError(JsonRpcResponse.Error.parseError());
        }

        if (request != null) {
            if (method == null) {
                response.setError(JsonRpcResponse.Error.methodNotFound());
            } else {
                List<Object> methodArgs;
                try {
                    methodArgs = jsonProcessor.processParamsIntoMethodArguments(request, method);
                    if (methodArgs == null) {
                        log.error("Expected params of type JSON array or object, but was: {}", request.getParams());
                        returnResponse = true;
                        response.setError(JsonRpcResponse.Error.invalidRequest());
                    }
                } catch (Exception ex) {
                    methodArgs = null;
                    log.error("An error has occurred while creating arguments for method: {}", method, ex);
                    response.setError(JsonRpcResponse.Error.invalidParams());
                }

                if (methodArgs != null) {
                    if (methodArgs.size() != method.getParameterCount()) {
                        log.error("Expecting params of length {} as arguments for method {}, but was {}",
                                method.getParameterCount(),
                                method,
                                methodArgs.size());
                        response.setError(JsonRpcResponse.Error.invalidParams());
                    } else {
                        try {
                            response.setResult(method.invoke(service, methodArgs.toArray()));
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
                }
            }
        }

        if (!returnResponse) {
            httpResponse.setStatus(HttpStatus.NO_CONTENT.value());
        } else {
            try {
                httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);

                ServletOutputStream outputStream = httpResponse.getOutputStream();
                jsonProcessor.processResponse(response, outputStream);
            } catch (Exception ex) {
                log.error("An error has occurred while trying to write the response body", ex);
                httpResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
        }
    }
}

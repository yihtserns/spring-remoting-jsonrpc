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
import java.util.Optional;

@Slf4j
public class JsonRpcServiceExporter implements HttpRequestHandler, InitializingBean {

    private final Map<String, ServiceMethod> name2Method = new HashMap<>();

    @Setter
    private Class<?> serviceInterface;
    @Setter
    private Object service;
    @Setter
    private JsonProcessor jsonProcessor;
    @Setter
    private ExceptionHandler exceptionHandler = new DefaultExceptionHandler();

    @Override
    public void afterPropertiesSet() throws NoSuchMethodException {
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

        for (Method interfaceMethod : serviceInterface.getMethods()) {
            if (name2Method.containsKey(interfaceMethod.getName())) {
                throw new IllegalArgumentException("Duplicate method name is not supported: " + interfaceMethod.getName());
            }
            name2Method.put(interfaceMethod.getName(), new ServiceMethod(
                    interfaceMethod,
                    service.getClass().getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes())));
        }
    }

    @Override
    public void handleRequest(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        ExecutionContext executionContext = new ExecutionContext();
        boolean returnResponse = true;
        JsonRpcResponse response = new JsonRpcResponse();

        try {
            ServletInputStream inputStream = httpRequest.getInputStream();
            executionContext.request = jsonProcessor.processRequest(inputStream);
            response.setId(Optional.ofNullable(executionContext.getRequest().getId()));

            if (executionContext.getRequest().getMethod() == null) {
                response.setError(JsonRpcResponse.Error.invalidRequest());
                executionContext.request = null;
            } else {
                ServiceMethod serviceMethod = name2Method.get(executionContext.getRequest().getMethod());
                if (serviceMethod != null) {
                    executionContext.serviceInterfaceMethod = serviceMethod.interfaceMethod;
                    executionContext.serviceImplementationMethod = serviceMethod.implementationMethod;
                }

                returnResponse = !StringUtils.isEmpty(executionContext.getRequest().getId());
            }
        } catch (Exception ex) {
            log.debug("An error occurred when trying to read the request body", ex);
            response.setError(JsonRpcResponse.Error.parseError());
        }

        if (executionContext.getRequest() != null) {
            if (executionContext.getServiceInterfaceMethod() == null) {
                response.setError(JsonRpcResponse.Error.methodNotFound());
            } else {
                List<Object> methodArgs;
                try {
                    methodArgs = jsonProcessor.processParamsIntoMethodArguments(executionContext);
                    if (methodArgs == null) {
                        log.error("Expected params of type JSON array or object, but was: {}", executionContext.getRequest().getParams());
                        returnResponse = true;
                        response.setError(JsonRpcResponse.Error.invalidRequest());
                    }
                } catch (Exception ex) {
                    methodArgs = null;
                    log.error("An error has occurred while creating arguments for method: {}", executionContext.getServiceInterfaceMethod(), ex);
                    response.setError(JsonRpcResponse.Error.invalidParams());
                }

                if (methodArgs != null) {
                    if (methodArgs.size() != executionContext.getServiceInterfaceMethod().getParameterCount()) {
                        log.error("Expecting params of length {} as arguments for method {}, but was {}",
                                executionContext.getServiceInterfaceMethod().getParameterCount(),
                                executionContext.getServiceInterfaceMethod(),
                                methodArgs.size());
                        response.setError(JsonRpcResponse.Error.invalidParams());
                    } else {
                        try {
                            response.setResult(executionContext.getServiceInterfaceMethod().invoke(service, methodArgs.toArray()));
                        } catch (InvocationTargetException ex) {
                            JsonRpcResponse.Error error = exceptionHandler.handleException(ex.getCause(), executionContext);
                            if (error.getCode() <= -32000 && error.getCode() >= -32768) {
                                log.error("Exception [{}] was converted into Error object using a reserved error code: {}",
                                        ex.getCause(),
                                        error.getCode());

                                error = JsonRpcResponse.Error.internalError();
                            }
                            response.setError(error);
                        } catch (IllegalAccessException | RuntimeException ex) {
                            log.error("Failed to call method: {}", executionContext.getRequest().getMethod(), ex);
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

    private static class ServiceMethod {

        private final Method interfaceMethod;
        private final Method implementationMethod;

        public ServiceMethod(Method interfaceMethod, Method implementationMethod) {
            this.interfaceMethod = interfaceMethod;
            this.implementationMethod = implementationMethod;
        }
    }
}

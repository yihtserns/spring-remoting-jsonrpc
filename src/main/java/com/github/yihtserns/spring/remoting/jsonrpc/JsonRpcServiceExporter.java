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
import org.springframework.web.HttpRequestHandler;

import javax.annotation.Nullable;
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
        try {
            JsonRpcRequest<?> request = readJsonRpcRequest(httpRequest);
            executionContext.request = request;

            ServiceMethod serviceMethod = getServiceMethod(request);
            executionContext.serviceInterfaceMethod = serviceMethod.interfaceMethod;
            executionContext.serviceImplementationMethod = serviceMethod.implementationMethod;

            Object result = executeMethod(convertParamsIntoMethodArguments(executionContext), executionContext);

            writeJsonRpcResponse(JsonRpcResponse.success(result, request), httpResponse);
        } catch (ExecutionException ex) {
            log.error("Execution failed with error: {} - {}", ex.error.getCode(), ex.error.getMessage(), ex);

            writeJsonRpcResponse(JsonRpcResponse.failure(ex.error, executionContext.getRequest()), httpResponse);
        } catch (RuntimeException ex) {
            log.error("Execution failed with unexpected error", ex);

            writeJsonRpcResponse(
                    JsonRpcResponse.failure(JsonRpcResponse.Error.internalError(), executionContext.getRequest()),
                    httpResponse);
        }
    }

    private JsonRpcRequest<?> readJsonRpcRequest(HttpServletRequest httpRequest) throws ExecutionException {
        JsonRpcRequest<?> request;
        try {
            ServletInputStream inputStream = httpRequest.getInputStream();

            request = jsonProcessor.processRequest(inputStream);
        } catch (Exception ex) {
            throw new ExecutionException(JsonRpcResponse.Error.parseError(), "An error occurred when trying to read the request body", ex);
        }
        return request;
    }

    private ServiceMethod getServiceMethod(JsonRpcRequest<?> request) throws ExecutionException {
        if (request.getMethod() == null) {
            throw new ExecutionException(JsonRpcResponse.Error.invalidRequest(), "Request has empty 'method' field");
        }

        ServiceMethod serviceMethod = name2Method.get(request.getMethod());
        if (serviceMethod == null) {
            throw new ExecutionException(
                    JsonRpcResponse.Error.methodNotFound(),
                    String.format("Service Interface [%s] does not have method named: %s",
                            serviceInterface,
                            request.getMethod()));
        }

        return serviceMethod;
    }

    private List<Object> convertParamsIntoMethodArguments(ExecutionContext executionContext) throws ExecutionException {
        List<Object> methodArgs;
        try {
            methodArgs = jsonProcessor.processParamsIntoMethodArguments(executionContext);
        } catch (Exception ex) {
            throw new ExecutionException(
                    JsonRpcResponse.Error.invalidParams(),
                    "An error has occurred while creating arguments for method: " + executionContext.getServiceInterfaceMethod(),
                    ex);
        }

        if (methodArgs == null) {
            throw new ExecutionException(
                    JsonRpcResponse.Error.invalidRequest(),
                    "Expected params of type JSON array or object, but was: " + executionContext.getRequest().getParams());
        }
        return methodArgs;
    }

    private Object executeMethod(List<Object> methodArgs, ExecutionContext executionContext) throws ExecutionException {
        Method method = executionContext.getServiceInterfaceMethod();

        if (methodArgs.size() != method.getParameterCount()) {
            throw new ExecutionException(
                    JsonRpcResponse.Error.invalidParams(),
                    String.format("Expecting params of length %s as arguments for method %s, but was %s",
                            method.getParameterCount(),
                            method,
                            methodArgs.size()));
        }

        try {
            return method.invoke(service, methodArgs.toArray());
        } catch (InvocationTargetException ex) {
            JsonRpcResponse.Error error = exceptionHandler.handleException(ex.getCause(), executionContext);
            if (error.getCode() <= -32000 && error.getCode() >= -32768) {
                throw new ExecutionException(
                        JsonRpcResponse.Error.internalError(),
                        String.format("Exception [%s] was converted into Error object using a reserved error code: %s",
                                ex.getCause(),
                                error));
            }
            throw new ExecutionException(error, "Failed to call method: " + executionContext.getServiceImplementationMethod(), ex);
        } catch (IllegalAccessException | RuntimeException ex) {
            throw new ExecutionException(
                    JsonRpcResponse.Error.internalError(),
                    "Failed to call method: " + executionContext.getServiceImplementationMethod(),
                    ex);
        }
    }

    private void writeJsonRpcResponse(@Nullable JsonRpcResponse response, HttpServletResponse httpResponse) {
        if (response == null) {
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

    private static class ExecutionException extends Exception {

        final JsonRpcResponse.Error error;

        public ExecutionException(JsonRpcResponse.Error error, String message, Throwable cause) {
            super(message, cause);
            this.error = error;
        }

        public ExecutionException(JsonRpcResponse.Error error, String message) {
            super(message);
            this.error = error;
        }
    }
}

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
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.MediaType;
import org.springframework.web.HttpRequestHandler;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JsonRpcServiceExporter implements HttpRequestHandler, InitializingBean {

    private ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, Method> name2Method = new HashMap<>();

    @Setter
    private Class<?> serviceInterface;
    @Setter
    private Object service;

    @Override
    public void afterPropertiesSet() {
        // TODO: serviceInterface != null
        // TODO: service != null
        // TODO: service implements serviceInterface

        for (Method method : serviceInterface.getMethods()) {
            if (name2Method.containsKey(method.getName())) {
                // TODO: throw
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

        try {
            Method method = name2Method.get(request.getMethod());
            if (method == null) {
                response.setError(JsonRpcResponse.Error.methodNotFound());
            } else {
                Object result = method.invoke(service, request.getParams().toArray());
                response.setResult(result);
            }
        } catch (IllegalArgumentException ex) {
            response.setError(JsonRpcResponse.Error.invalidParams());
        } catch (InvocationTargetException ex) {
            // TODO:
            throw new IllegalArgumentException(ex);
        } catch (IllegalAccessException | RuntimeException ex) {
            log.error("Failed to call method: " + request.getMethod(), ex);
            response.setError(JsonRpcResponse.Error.internalError());
        }

        httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(httpResponse.getOutputStream(), response);
    }
}

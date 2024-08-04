/*
 * Copyright (C) RevTech Lab Sdn Bhd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.github.yihtserns.spring.remoting.jsonrpc.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.yihtserns.spring.remoting.jsonrpc.ExecutionContext;
import com.github.yihtserns.spring.remoting.jsonrpc.JsonProcessor;
import com.github.yihtserns.spring.remoting.jsonrpc.JsonRpcRequest;
import com.github.yihtserns.spring.remoting.jsonrpc.JsonRpcResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * @author yihtserns
 */
public class JacksonJsonProcessor implements JsonProcessor {

    private static final TypeReference<JsonRpcRequest<JsonNode>> JSON_NODE_PARAMS_REQUEST_TYPE_REF = new TypeReference<JsonRpcRequest<JsonNode>>() {
    };

    private final ObjectReader reader;
    private final ObjectWriter writer;

    /**
     * @see #from(ObjectMapper)
     */
    private JacksonJsonProcessor(ObjectReader reader, ObjectWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    @Override
    public JsonRpcRequest<JsonNode> processRequest(InputStream inputStream) throws IOException {
        JsonParser parser = reader.createParser(inputStream);

        return reader.readValue(parser, JSON_NODE_PARAMS_REQUEST_TYPE_REF);
    }

    @Override
    public List<Object> processParamsIntoMethodArguments(ExecutionContext executionContext) {
        List<JsonNode> requestParams;
        JsonNode params = (JsonNode) executionContext.getRequest().getParams();
        if (params == null || params.isNull()) {
            requestParams = emptyList();
        } else if (params.isArray()) {
            requestParams = stream(params.spliterator(), false).collect(toList());
        } else if (params.isObject()) {
            requestParams = singletonList(params);
        } else { // Unsupported params type
            requestParams = null;
        }

        if (requestParams == null) {
            return null;
        }

        Method method = executionContext.getServiceInterfaceMethod();
        List<Object> methodArguments = new ArrayList<>();
        for (int i = 0; i < requestParams.size(); i++) {
            JsonNode param = requestParams.get(i);

            if (i < method.getParameterCount()) {
                Parameter methodParameter = method.getParameters()[i];

                try {
                    methodArguments.add(reader.treeToValue(
                            param,
                            reader.getTypeFactory().constructType(methodParameter.getParameterizedType())));
                } catch (JsonProcessingException ex) {
                    throw new IllegalArgumentException(
                            String.format("Failed to convert params #%s to argument [%s]", i, methodParameter),
                            ex);
                }
            } else {
                methodArguments.add(param.toString());
            }
        }

        return methodArguments;
    }

    @Override
    public void processResponse(JsonRpcResponse response, OutputStream outputStream) throws IOException {
        writer.writeValue(outputStream, response);
    }

    public static JacksonJsonProcessor from(ObjectMapper objectMapperPrototype) {
        ObjectMapper objectMapper = objectMapperPrototype.copy()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) // KLUDGE: Spring/Boot set FAIL_ON_UNKNOWN_PROPERTIES to false by default
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return new JacksonJsonProcessor(objectMapper.reader(), objectMapper.writer());
    }
}

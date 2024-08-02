/*
 * Copyright (C) RevTech Lab Sdn Bhd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.github.yihtserns.spring.remoting.jsonrpc;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author yihtserns
 */
public interface JsonProcessor {

    JsonRpcRequest<?> processRequest(InputStream inputStream) throws Exception;

    /**
     * @return either one of:
     * <ol>
     *     <li>Method arguments of the correct type & count.</li>
     *     <li>Method arguments less than the method parameter count, because the {@code params} JSON array did not provide
     *     enough entries - will be validated & rejected.</li>
     *     <li>Method arguments more than the method parameter count, because the {@code params} JSON array provided
     *     extra entries - will be validated & rejected.</li>
     *     <li>{@code null} if the {@code params} is not of the correct JSON type (i.e. neither {@code null}, nor JSON array,
     *     nor JSON object).</li>
     * </ol>
     */
    List<Object> processParamsIntoMethodArguments(JsonRpcRequest<?> request, Method method) throws Exception;

    void processResponse(JsonRpcResponse response, OutputStream outputStream) throws Exception;
}

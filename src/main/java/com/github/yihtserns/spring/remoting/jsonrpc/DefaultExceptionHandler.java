/*
 * Copyright (C) RevTech Lab Sdn Bhd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.github.yihtserns.spring.remoting.jsonrpc;

import lombok.extern.slf4j.Slf4j;

/**
 * @author yihtserns
 */
@Slf4j
public class DefaultExceptionHandler implements ExceptionHandler {

    @Override
    public JsonRpcResponse.Error handleException(Throwable exception, ExecutionContext executionContext) {
        log.error("Error thrown by method: {}", executionContext.getServiceImplementationMethod(), exception);

        return JsonRpcResponse.Error.internalError();
    }
}

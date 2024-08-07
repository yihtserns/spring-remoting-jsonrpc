/*
 * Copyright (C) RevTech Lab Sdn Bhd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.github.yihtserns.spring.remoting.jsonrpc;

/**
 * @author yihtserns
 */
public interface ExceptionHandler {

    JsonRpcResponse.Error handleException(Throwable exception, ExecutionContext executionContext);
}

/*
 * Copyright (C) RevTech Lab Sdn Bhd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.github.yihtserns.spring.remoting.jsonrpc;

import lombok.Getter;

import java.lang.reflect.Method;

/**
 * @author yihtserns
 */
@Getter
public class ExecutionContext {

    JsonRpcRequest<?> request;
    Method serviceInterfaceMethod;
    Method serviceImplementationMethod;
}

/*
 * Copyright (C) RevTech Lab Sdn Bhd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.github.yihtserns.spring.remoting.jsonrpc.function;

/**
 * @author yihtserns
 */
@FunctionalInterface
public interface ThrowableFunction<F, T, E extends Exception> {

    T apply(F from) throws E;
}

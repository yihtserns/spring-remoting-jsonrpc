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
public interface ThrowableSupplier<T, E extends Exception> {

    T get() throws E;
}

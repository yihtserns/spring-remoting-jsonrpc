/*
 * Copyright (C) RevTech Lab Sdn Bhd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.github.yihtserns.spring.remoting.jsonrpc.util;

/**
 * @author yihtserns
 */
public class Either<L, R> {

    private L left;
    private R right;

    /**
     * @see #left(Object)
     * @see #right(Object)
     */
    private Either() {
    }

    public <T, E extends Exception> T map(ThrowableFunction<L, T, E> leftMapper,
                                          ThrowableFunction<R, T, E> rightMapper) throws E {

        if (left != null) {
            return leftMapper.apply(left);
        }
        return rightMapper.apply(right);
    }

    public static <L, R> Either<L, R> left(L value) {
        Either<L, R> either = new Either<>();
        either.left = value;

        return either;
    }

    public static <L, R> Either<L, R> right(R value) {
        Either<L, R> either = new Either<>();
        either.right = value;

        return either;
    }
}

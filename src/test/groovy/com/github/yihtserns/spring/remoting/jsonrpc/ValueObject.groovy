package com.github.yihtserns.spring.remoting.jsonrpc

import com.fasterxml.jackson.annotation.JsonValue

/**
 * @author yihtserns
 */
class ValueObject {

    @JsonValue
    int intValue

    static ValueObject valueOf(int value) {
        return new ValueObject(intValue: value)
    }
}

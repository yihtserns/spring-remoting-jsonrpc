package com.github.yihtserns.spring.remoting.jsonrpc

import org.springframework.core.convert.converter.Converter

class CustomApplicationExceptionToError implements Converter<CustomApplicationException, JsonRpcResponse.Error> {

    @Override
    JsonRpcResponse.Error convert(CustomApplicationException ex) {
        return new JsonRpcResponse.Error(ex.errorCode, "Custom Application Error")
    }
}

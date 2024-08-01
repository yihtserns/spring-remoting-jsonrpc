package com.github.yihtserns.spring.remoting.jsonrpc

import java.lang.reflect.Method

class CustomApplicationExceptionToError extends DefaultExceptionHandler {

    @Override
    JsonRpcResponse.Error handleException(Throwable exception, Method method) {
        if (exception instanceof CustomApplicationException) {
            return new JsonRpcResponse.Error(exception.errorCode, "Custom Application Error")
        }
        return super.handleException(exception, method)
    }
}

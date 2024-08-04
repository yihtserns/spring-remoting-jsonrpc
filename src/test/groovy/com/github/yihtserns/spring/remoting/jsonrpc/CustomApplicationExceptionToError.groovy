package com.github.yihtserns.spring.remoting.jsonrpc

class CustomApplicationExceptionToError extends DefaultExceptionHandler {

    @Override
    JsonRpcResponse.Error handleException(Throwable exception, ExecutionContext executionContext) {
        if (exception instanceof CustomApplicationException) {
            return new JsonRpcResponse.Error(
                    exception.errorCode,
                    "Custom Application Error",
                    [data1: "val1", data2: "val2"])
        }
        return super.handleException(exception, executionContext)
    }
}

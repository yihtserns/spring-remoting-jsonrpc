package com.github.yihtserns.spring.remoting.jsonrpc

class CustomApplicationException extends Exception {

    int errorCode

    CustomApplicationException(int errorCode) {
        this.errorCode = errorCode
    }
}

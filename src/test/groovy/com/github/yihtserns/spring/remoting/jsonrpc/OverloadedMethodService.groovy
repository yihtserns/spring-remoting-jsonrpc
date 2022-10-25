package com.github.yihtserns.spring.remoting.jsonrpc

interface OverloadedMethodService {

    int overloaded(String value)

    int overloaded(int value)
}

package com.github.yihtserns.spring.remoting.jsonrpc

class OverloadedMethodServiceImpl implements OverloadedMethodService {

    @Override
    int overloaded(String value) {
        return 0
    }

    @Override
    int overloaded(int value) {
        return 0
    }
}

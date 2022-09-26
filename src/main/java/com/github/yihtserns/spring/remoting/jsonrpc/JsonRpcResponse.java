/*
 * Copyright 2022 yihtserns.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.yihtserns.spring.remoting.jsonrpc;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JsonRpcResponse {

    private String jsonrpc = "2.0";
    private String id;
    private Object result;
    private Error error;

    @Getter
    @Setter
    public static class Error {

        private int code;
        private String message;
        private Object data;

        public static Error methodNotFound() {
            Error error = new Error();
            error.code = -32601;
            error.message = "Method not found";

            return error;
        }

        public static Error invalidParams() {
            Error error = new Error();
            error.code = -32602;
            error.message = "Invalid params";

            return error;
        }

        public static Error internalError() {
            Error error = new Error();
            error.code = -32603;
            error.message = "Internal error";

            return error;
        }
    }
}

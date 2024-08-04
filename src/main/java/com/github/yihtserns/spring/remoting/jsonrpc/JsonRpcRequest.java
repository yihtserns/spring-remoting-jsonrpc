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

import javax.annotation.Nullable;
import java.util.Optional;

@Getter
@Setter
public class JsonRpcRequest<P> {

    private String jsonrpc;
    @Nullable
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<String> id;
    private String method;
    private P params;

    public boolean isNotification() {
        //noinspection OptionalAssignedToNull
        return id == null;
    }
}

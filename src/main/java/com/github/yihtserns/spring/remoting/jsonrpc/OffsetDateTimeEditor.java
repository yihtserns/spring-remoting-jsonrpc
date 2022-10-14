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

import org.springframework.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

class OffsetDateTimeEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.hasText(text)) {
            try {
                setValue(OffsetDateTime.parse(text));
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Failed to parse: " + text, ex);
            }
        } else {
            setValue(null);
        }
    }
}

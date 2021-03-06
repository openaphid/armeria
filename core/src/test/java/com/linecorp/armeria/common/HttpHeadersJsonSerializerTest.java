/*
 * Copyright 2017 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.linecorp.armeria.common;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.util.AsciiString;

public class HttpHeadersJsonSerializerTest {

    private static final AsciiString NAME = HttpHeaderNames.of("a");

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void singleValue() {
        assertThatJson(mapper.valueToTree(HttpHeaders.of(NAME, "0"))).isEqualTo("{\"a\":\"0\"}");
    }

    @Test
    public void multipleValues() {
        final HttpHeaders headers = HttpHeaders.of(NAME, "0", NAME, "1");
        assertThatJson(mapper.valueToTree(headers)).isEqualTo("{\"a\":[\"0\",\"1\"]}");
    }

    @Test
    public void requestHeaders() {
        assertThatJson(mapper.valueToTree(RequestHeaders.of(HttpMethod.GET, "/")))
                .isEqualTo("{ \":method\": \"GET\", \":path\": \"/\" }");
    }

    @Test
    public void responseHeaders() {
        assertThatJson(mapper.valueToTree(ResponseHeaders.of(200)))
                .isEqualTo("{ \":status\": \"200\" }");
    }
}

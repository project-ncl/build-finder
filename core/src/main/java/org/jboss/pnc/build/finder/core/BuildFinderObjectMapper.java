/*
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.build.finder.core;

import java.io.Serial;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.redhat.red.build.koji.model.json.util.KojiObjectMapper;

public final class BuildFinderObjectMapper extends KojiObjectMapper {
    @Serial
    private static final long serialVersionUID = -6336680901444277747L;

    public BuildFinderObjectMapper() {
        enable(SerializationFeature.INDENT_OUTPUT);
        enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        registerModule(new Jdk8Module());
        registerModule(new JavaTimeModule());
        registerModule(new BuildFinderModule());
    }
}

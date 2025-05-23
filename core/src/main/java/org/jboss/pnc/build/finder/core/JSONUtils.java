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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JSONUtils {
    private static final ObjectMapper MAPPER = new BuildFinderObjectMapper();

    private JSONUtils() {

    }

    public static String dumpString(Object obj) throws JsonProcessingException {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

    public static void dumpObjectToFile(Object obj, Path path) throws IOException {
        dumpObjectToFile(obj, path, MAPPER);
    }

    public static void dumpObjectToFile(Object obj, Path path, ObjectMapper mapper) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), obj);
        Files.write(path, Collections.singletonList(""), StandardOpenOption.APPEND);
    }

    public static Map<String, Collection<LocalFile>> loadChecksumsFile(Path path) throws IOException {
        TypeReference<Map<String, Collection<LocalFile>>> typeReference = new ChecksumsMapTypeReference();
        return MAPPER.readValue(path.toFile(), typeReference);
    }

    public static Map<String, List<String>> loadLicenseMapping(InputStream in) throws IOException {
        TypeReference<Map<String, List<String>>> typeReference = new LicensesMapTypeReference();
        return MAPPER.readValue(in, typeReference);
    }

    private static final class LicensesMapTypeReference extends TypeReference<Map<String, List<String>>> {

    }

    private static final class ChecksumsMapTypeReference extends TypeReference<Map<String, Collection<LocalFile>>> {

    }
}

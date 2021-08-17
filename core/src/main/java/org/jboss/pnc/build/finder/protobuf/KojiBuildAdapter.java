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
package org.jboss.pnc.build.finder.protobuf;

import org.infinispan.protostream.annotations.ProtoAdapter;
import org.infinispan.protostream.annotations.ProtoFactory;
import org.infinispan.protostream.annotations.ProtoField;
import org.jboss.pnc.build.finder.koji.KojiBuild;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.red.build.koji.model.json.util.KojiObjectMapper;

/**
 * Protostream adapter to be able to properly marshall / unmarshall KojiArchiveInfo.
 *
 * This class uses several classes defined in Kojiji and can't be easily modified to add Proto annotations. Instead, the
 * marshalling process involves converting KojiBuild object into JSON and stored in Protobuf as a string. The
 * unmarshalling process involves reading the JSON string from Protobuf and converting it back to a KojiBuild object.
 */
@ProtoAdapter(KojiBuild.class)
public class KojiBuildAdapter {
    private static final ObjectMapper OBJECT_MAPPER = new KojiObjectMapper();

    @ProtoFactory
    KojiBuild create(String jsonData) {
        try {
            return OBJECT_MAPPER.readValue(jsonData, KojiBuild.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @ProtoField(number = 1, required = true)
    String getJsonData(KojiBuild kojiBuild) {
        try {
            return OBJECT_MAPPER.writeValueAsString(kojiBuild);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

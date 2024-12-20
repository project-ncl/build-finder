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

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoSchema;
import org.jboss.pnc.build.finder.core.LocalFile;

@ProtoSchema(
        includeClasses = {
                LocalFile.class,
                MultiValuedMapProtobufWrapper.class,
                KojiArchiveInfoAdapter.class,
                KojiBuildAdapter.class,
                PncArtifactAdapter.class,
                ArtifactStaticRemoteCollection.class,
                ListKojiArchiveInfoProtobufWrapper.class },
        schemaFileName = "build-finder.proto",
        schemaFilePath = "proto/",
        schemaPackageName = "org.jboss.pnc.build.finder")
public interface ProtobufSerializer extends GeneratedSchema {
}

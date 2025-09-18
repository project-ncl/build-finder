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
package org.jboss.pnc.build.finder.pnc.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import java.util.Collections;

import org.jboss.pnc.client.RemoteCollection;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.BuildPushReport;
import org.jboss.pnc.dto.ProductVersion;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * @author Jakub Bartecek
 */
@TestMethodOrder(MethodOrderer.MethodName.class)
class HashMapCachingClientTest {
    private static final DummyPncClient DUMMY_PNC_CLIENT = new DummyPncClient();

    private static final PncClient HASH_MAP_CACHING_PNC_CLIENT = new CachingPncClient(DUMMY_PNC_CLIENT, null);

    @Test
    void testM1GetArtifactsByMd5FromClient() {
        DUMMY_PNC_CLIENT.getArtifactsByMd5("md5");
        assertThat(DUMMY_PNC_CLIENT.getGetArtifactsByMd5Counter()).isEqualTo(1);
    }

    @Test
    void testM2GetArtifactsByMd5FromCache() throws RemoteResourceException {
        RemoteCollection<Artifact> md5 = HASH_MAP_CACHING_PNC_CLIENT.getArtifactsByMd5("md5");
        assertThat(md5).hasSize(1);
    }

    private static class DummyPncClient implements PncClient {
        private final Collection<Artifact> artifacts;

        private int getArtifactsByMd5Counter;

        DummyPncClient() {
            artifacts = Collections.singletonList(Artifact.builder().id("1").build());
        }

        int getGetArtifactsByMd5Counter() {
            return getArtifactsByMd5Counter;
        }

        @Override
        public RemoteCollection<Artifact> getArtifactsByMd5(String md5) {
            getArtifactsByMd5Counter++;
            return new StaticRemoteCollection<>(artifacts);
        }

        @Override
        public RemoteCollection<Artifact> getArtifactsBySha1(String sha1) {
            return null;
        }

        @Override
        public RemoteCollection<Artifact> getArtifactsBySha256(String sha256) {
            return null;
        }

        @Override
        public BuildPushReport getBuildPushReport(String buildId) {
            return null;
        }

        @Override
        public ProductVersion getProductVersion(String productMilestoneId) {
            return null;
        }

        @Override
        public void close() {
            // No need to close
        }
    }
}

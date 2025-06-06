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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BuildFinderTest {
    private static List<String> files;

    @BeforeAll
    static void setTarget() throws IOException {
        Path path = TestUtils.resolveFileResource();
        assertThat(path).isDirectory();
        Path parent = path.getParent();
        assertThat(parent).isDirectory();
        Path grandparent = parent.getParent();
        assertThat(grandparent).isDirectory();
        Path target = grandparent.resolve("pom.xml");
        assertThat(target).isRegularFile().isReadable();
        files = Collections.singletonList(target.toAbsolutePath().toString());
    }

    @Test
    void testDirectory(@TempDir Path folder) throws IOException {
        ChecksumType checksumType = ChecksumType.sha1;
        BuildConfig config = new BuildConfig();

        config.setChecksumTypes(EnumSet.of(checksumType));
        config.setOutputDirectory(folder.toAbsolutePath().toString());

        DistributionAnalyzer da = new DistributionAnalyzer(files, config);
        da.checksumFiles();
        da.outputToFile(checksumType);

        try (Stream<Path> stream = Files.list(folder)) {
            List<Path> paths = stream.toList();
            assertThat(paths).hasSize(1);
            Path path = paths.get(0);
            assertThat(path).isRegularFile().isEqualTo(da.getChecksumFile(checksumType));
        }
    }

    @Test
    void testLoadChecksumsFile(@TempDir Path folder) throws IOException {
        ChecksumType checksumType = ChecksumType.md5;
        BuildConfig config = new BuildConfig();

        config.setChecksumTypes(EnumSet.of(checksumType));
        config.setOutputDirectory(folder.toAbsolutePath().toString());

        DistributionAnalyzer da = new DistributionAnalyzer(files, config);
        da.checksumFiles();
        da.outputToFile(checksumType);

        Map<String, Collection<LocalFile>> checksums = JSONUtils.loadChecksumsFile(da.getChecksumFile(checksumType));

        assertThat(checksums).hasSize(1);
    }
}

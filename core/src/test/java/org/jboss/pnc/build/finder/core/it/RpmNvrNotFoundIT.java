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
package org.jboss.pnc.build.finder.core.it;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MultiValuedMap;
import org.jboss.pnc.build.finder.core.BuildFinder;
import org.jboss.pnc.build.finder.core.BuildSystemInteger;
import org.jboss.pnc.build.finder.core.Checksum;
import org.jboss.pnc.build.finder.core.ChecksumType;
import org.jboss.pnc.build.finder.core.DistributionAnalyzer;
import org.jboss.pnc.build.finder.core.FileError;
import org.jboss.pnc.build.finder.koji.KojiBuild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RpmNvrNotFoundIT extends AbstractRpmIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpmNvrNotFoundIT.class);

    @Override
    protected List<String> getFiles() {
        return Collections.singletonList(
                "https://kojipkgs.fedoraproject.org//packages/java-11-openjdk/11.0.8.10/1.fc33/x86_64/java-11-openjdk-11.0.8.10-1.fc33.x86_64.rpm");
    }

    @Override
    protected void verify(DistributionAnalyzer analyzer, BuildFinder finder) {
        Collection<FileError> fileErrors = analyzer.getFileErrors();
        Map<String, Collection<Checksum>> files = analyzer.getFiles();
        Map<Checksum, Collection<String>> foundChecksums = finder.getFoundChecksums();
        Map<Checksum, Collection<String>> notFoundChecksums = finder.getNotFoundChecksums();
        List<KojiBuild> buildsFound = finder.getBuildsFound();
        Map<ChecksumType, MultiValuedMap<String, String>> checksums = analyzer.getChecksums();
        Map<BuildSystemInteger, KojiBuild> builds = finder.getBuildsMap();

        assertThat(checksums, is(aMapWithSize(3)));
        assertThat(builds, is(aMapWithSize(1)));
        assertThat(fileErrors, is(empty()));
        assertThat(
                files,
                allOf(
                        aMapWithSize(1),
                        hasEntry(
                                is("java-11-openjdk-11.0.8.10-1.fc33.x86_64.rpm"),
                                contains(hasProperty("value", is("aa585b870f59ef457f26fa32a0daf923"))))));
        assertThat(
                analyzer.getChecksums(ChecksumType.md5),
                hasEntry(
                        is("aa585b870f59ef457f26fa32a0daf923"),
                        contains("java-11-openjdk-11.0.8.10-1.fc33.x86_64.rpm")));
        assertThat(
                notFoundChecksums,
                allOf(
                        is(aMapWithSize(1)),
                        hasEntry(
                                hasProperty("value", is("aa585b870f59ef457f26fa32a0daf923")),
                                contains("java-11-openjdk-11.0.8.10-1.fc33.x86_64.rpm"))));
        assertThat(foundChecksums, is(anEmptyMap()));
        assertThat(buildsFound, is(empty()));
        assertThat(
                builds.get(new BuildSystemInteger(0)).getArchives(),
                contains(
                        allOf(
                                hasProperty("filenames", contains("java-11-openjdk-11.0.8.10-1.fc33.x86_64.rpm")),
                                hasProperty(
                                        "checksums",
                                        contains(hasProperty("value", is("aa585b870f59ef457f26fa32a0daf923")))))));

        LOGGER.info("Checksums size: {}", checksums.size());
        LOGGER.info("Builds size: {}", builds.size());
        LOGGER.info("File errors: {}", fileErrors.size());
    }
}

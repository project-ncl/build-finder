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
package org.jboss.pnc.build.finder.koji;

import java.util.Collection;
import java.util.TreeSet;

import org.jboss.pnc.build.finder.core.Checksum;
import org.jboss.pnc.build.finder.core.LicenseInfo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiBtype;
import com.redhat.red.build.koji.model.xmlrpc.KojiRpmInfo;

public class KojiLocalArchive {
    private KojiArchiveInfo archive;

    private KojiRpmInfo rpm;

    @JsonProperty("files")
    private Collection<String> filenames;

    private Collection<Checksum> checksums;

    @JsonProperty("unmatchedFiles")
    private Collection<String> unmatchedFilenames;

    private Collection<LicenseInfo> licenses;

    public KojiLocalArchive() {
        this.filenames = new TreeSet<>();
        this.checksums = new TreeSet<>();
        this.unmatchedFilenames = new TreeSet<>();
        this.licenses = new TreeSet<>();
    }

    public KojiLocalArchive(KojiArchiveInfo archive, Collection<String> filenames, Collection<Checksum> checksums) {
        this.archive = archive;
        this.filenames = new TreeSet<>(filenames);
        this.checksums = new TreeSet<>(checksums);
        this.unmatchedFilenames = new TreeSet<>();
        this.licenses = new TreeSet<>();
    }

    public KojiLocalArchive(KojiRpmInfo rpm, Collection<String> filenames, Collection<Checksum> checksums) {
        this.rpm = rpm;
        this.filenames = new TreeSet<>(filenames);
        this.checksums = new TreeSet<>(checksums);
        this.unmatchedFilenames = new TreeSet<>();
        this.licenses = new TreeSet<>();
    }

    public static boolean isMissingBuildTypeInfo(KojiArchiveInfo archive) {
        if (archive == null) {
            return false;
        }

        KojiBtype archiveBuildType = archive.getBuildType();

        if (archiveBuildType == null) {
            return false;
        }

        return switch (archiveBuildType) {
            case maven -> archive.getGroupId() == null;
            case win -> archive.getPlatforms() == null;
            case image -> archive.getArch() == null;
            default -> false;
        };
    }

    public KojiArchiveInfo getArchive() {
        return archive;
    }

    public void setArchive(KojiArchiveInfo archive) {
        this.archive = archive;
    }

    public KojiRpmInfo getRpm() {
        return rpm;
    }

    public void setRpm(KojiRpmInfo rpm) {
        this.rpm = rpm;
    }

    public Collection<String> getFilenames() {
        return filenames;
    }

    public void setFilenames(Collection<String> filenames) {
        this.filenames = new TreeSet<>(filenames);
    }

    public Collection<Checksum> getChecksums() {
        return checksums;
    }

    public void setChecksums(Collection<Checksum> checksums) {
        this.checksums = new TreeSet<>(checksums);
    }

    @JsonIgnore
    public boolean isBuiltFromSource() {
        return unmatchedFilenames.isEmpty();
    }

    public Collection<String> getUnmatchedFilenames() {
        return unmatchedFilenames;
    }

    public void setUnmatchedFilenames(Collection<String> unmatchedFilenames) {
        this.unmatchedFilenames = new TreeSet<>(unmatchedFilenames);
    }

    public Collection<LicenseInfo> getLicenses() {
        return licenses;
    }

    public void setLicenses(Collection<LicenseInfo> licenses) {
        this.licenses = licenses;
    }

    @Override
    public String toString() {
        return "KojiLocalArchive [archive=" + archive + ", rpm=" + rpm + ", filenames=" + filenames + ", checksums="
                + checksums + ", unmatchedFilenames=" + unmatchedFilenames + ", licenses=" + licenses + "]";
    }
}

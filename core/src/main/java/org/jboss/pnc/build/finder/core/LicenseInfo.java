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

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.maven.model.License;

public class LicenseInfo implements Comparable<LicenseInfo> {
    private String comments;

    private String distribution;

    private String name;

    private String url;

    private String spdxLicenseId;

    private String source;

    public LicenseInfo(FileObject fileObject, License license) {
        comments = license.getComments();
        distribution = license.getDistribution();
        name = license.getName();
        url = license.getUrl();
        this.spdxLicenseId = SpdxLicenseUtils.getSPDXLicenseId(name, url);
        source = relativize(fileObject);
    }

    public LicenseInfo(FileObject fileObject, String name, String url) {
        comments = null;
        distribution = null;
        this.name = name;
        this.url = url;
        this.spdxLicenseId = SpdxLicenseUtils.getSPDXLicenseId(name, url);
        source = relativize(fileObject);
    }

    private static String relativize(FileObject fileObject) {
        String friendlyURI = fileObject.getName().getFriendlyURI();
        int index = friendlyURI.lastIndexOf("!/");

        if (index == -1) {
            return fileObject.getName().getBaseName();
        }

        return friendlyURI.substring(index + 2);
    }

    public String getComments() {
        return comments;
    }

    public String getDistribution() {
        return distribution;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getSpdxLicenseId() {
        return spdxLicenseId;
    }

    public void setSpdxLicense(String spdxLicenseId) {
        this.spdxLicenseId = spdxLicenseId;
    }

    public String getSource() {
        return source;
    }

    @Override
    public int compareTo(LicenseInfo o) {
        if (o == null) {
            return 1;
        }

        int compare = StringUtils.compare(spdxLicenseId, o.spdxLicenseId);

        if (compare != 0) {
            return compare;
        }

        compare = StringUtils.compare(name, o.name);

        if (compare != 0) {
            return compare;
        }

        compare = StringUtils.compare(source, o.source);

        if (compare != 0) {
            return compare;
        }

        compare = StringUtils.compare(distribution, o.distribution);

        if (compare != 0) {
            return compare;
        }

        return StringUtils.compare(comments, o.comments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LicenseInfo that = (LicenseInfo) o;
        return Objects.equals(comments, that.comments) && Objects.equals(distribution, that.distribution)
                && Objects.equals(name, that.name) && Objects.equals(url, that.url)
                && Objects.equals(spdxLicenseId, that.spdxLicenseId) && Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hash(comments, distribution, name, url, spdxLicenseId, source);
    }

    @Override
    public String toString() {
        return "LicenseInfo: " + ", comments: '" + comments + '\'' + ", distribution: '" + distribution + '\''
                + ", name: '" + name + '\'' + ", url: '" + url + '\'' + ", spdxLicenseId: '" + spdxLicenseId + '\''
                + ", source: " + source;
    }
}

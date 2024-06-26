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

import static org.jboss.pnc.build.finder.core.LicenseSource.UNKNOWN;
import static org.jboss.pnc.build.finder.core.LicenseUtils.NONE;

import java.util.Objects;

import org.apache.maven.model.License;

public class LicenseInfo implements Comparable<LicenseInfo> {
    private final String comments;

    private final String distribution;

    private final String name;

    private final String url;

    private String spdxLicenseId;

    private final LicenseSource source;

    public LicenseInfo() {
        comments = null;
        distribution = null;
        name = null;
        url = null;
        spdxLicenseId = NONE;
        source = UNKNOWN;
    }

    public LicenseInfo(LicenseSource source) {
        comments = null;
        distribution = null;
        this.name = null;
        this.url = null;
        this.spdxLicenseId = null;
        this.source = source;
    }

    public LicenseInfo(License license, LicenseSource source) {
        comments = license.getComments();
        distribution = license.getDistribution();
        name = license.getName();
        url = license.getUrl();
        this.spdxLicenseId = LicenseUtils.getSPDXLicenseId(name, url);
        this.source = source;
    }

    public LicenseInfo(String name, String url, LicenseSource source) {
        comments = null;
        distribution = null;
        this.name = name;
        this.url = url;
        this.spdxLicenseId = LicenseUtils.getSPDXLicenseId(name, url);
        this.source = source;
    }

    public LicenseInfo(String name, String url, String spdxLicenseId, LicenseSource source) {
        comments = null;
        distribution = null;
        this.name = name;
        this.url = url;
        this.spdxLicenseId = spdxLicenseId;
        this.source = source;
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

    public void setSpdxLicenseId(String spdxLicenseId) {
        this.spdxLicenseId = spdxLicenseId;
    }

    public LicenseSource getSource() {
        return source;
    }

    @Override
    public int compareTo(LicenseInfo o) {
        if (spdxLicenseId != null && o.spdxLicenseId != null) {
            return spdxLicenseId.compareTo(o.spdxLicenseId);
        }

        return 0;
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
        return Objects.hash(comments, distribution, name, url, spdxLicenseId);
    }

    @Override
    public String toString() {
        return "LicenseInfo: " + ", comments: '" + comments + '\'' + ", distribution: '" + distribution + '\''
                + ", name: '" + name + '\'' + ", url: '" + url + '\'' + ", spdxLicenseId: '" + spdxLicenseId + '\''
                + ", source: " + source;
    }
}

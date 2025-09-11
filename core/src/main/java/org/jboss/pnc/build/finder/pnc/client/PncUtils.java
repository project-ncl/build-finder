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

import static com.redhat.red.build.koji.model.json.KojiJsonConstants.BUILD_SYSTEM;
import static com.redhat.red.build.koji.model.json.KojiJsonConstants.EXTERNAL_BUILD_ID;
import static com.redhat.red.build.koji.model.xmlrpc.KojiBtype.maven;
import static com.redhat.red.build.koji.model.xmlrpc.KojiBtype.npm;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;
import static org.jboss.pnc.api.constants.Attributes.BREW_TAG_PREFIX;
import static org.jboss.pnc.api.constants.Attributes.BUILD_BREW_NAME;
import static org.jboss.pnc.api.constants.Attributes.BUILD_BREW_VERSION;
import static org.jboss.pnc.build.finder.core.BuildFinderUtils.BUILD_ID_ZERO;
import static org.jboss.pnc.build.finder.core.BuildFinderUtils.isBuildIdZero;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.MapUtils;
import org.jboss.pnc.build.finder.koji.KojiBuild;
import org.jboss.pnc.build.finder.pnc.PncBuild;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.ArtifactRef;
import org.jboss.pnc.dto.Build;
import org.jboss.pnc.enums.BuildType;

import com.redhat.red.build.koji.model.xmlrpc.KojiArchiveInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiBtype;
import com.redhat.red.build.koji.model.xmlrpc.KojiBuildInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiBuildState;
import com.redhat.red.build.koji.model.xmlrpc.KojiChecksumType;
import com.redhat.red.build.koji.model.xmlrpc.KojiTagInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiTaskInfo;
import com.redhat.red.build.koji.model.xmlrpc.KojiTaskRequest;

public final class PncUtils {
    public static final String EXTERNAL_ARCHIVE_ID = "external_archive_id";

    public static final String EXTERNAL_BREW_BUILD_ID = "external_brew_build_id";

    public static final String EXTERNAL_BREW_BUILD_URL = "external_brew_build_url";

    public static final String EXTERNAL_BUILD_CONFIGURATION_ID = "external_build_configuration_id";

    public static final String EXTERNAL_PRODUCT_ID = "external_product_id";

    public static final String EXTERNAL_PROJECT_ID = "external_project_id";

    public static final String EXTERNAL_VERSION_ID = "external_version_id";

    public static final String EXTERNAL_BUILD_TYPE = "external_build_type";

    public static final String GRADLE = "gradle";

    public static final String MAVEN = "maven";

    public static final String NPM = "npm";

    public static final String SBT = "sbt";

    public static final String UNKNOWN = "unknown";

    public static final String NO_BUILD_BREW_NAME = "NO_BUILD_BREW_NAME";

    public static final String PNC = "PNC";

    private PncUtils() {
        throw new IllegalArgumentException("This is a utility class and cannot be instantiated");
    }

    private static void setMavenBuildInfoFromBuildRecord(PncBuild build, KojiBuildInfo buildInfo) {
        // PNC brew name for maven builds is in the format "G:A"
        String brewName = getBrewName(build);
        String[] ga = brewName.split("-", 2);
        if (ga.length >= 2) {
            buildInfo.setMavenGroupId(ga[0]);
            buildInfo.setMavenArtifactId(ga[1]);
        }

        // PNC brew version for maven builds is in the format "V"
        String brewVersion = getBrewVersion(build);
        buildInfo.setMavenVersion(brewVersion);
    }

    private static String getBrewName(PncBuild build) {
        String buildBrewName = MapUtils.getObject(build.getBuild().getAttributes(), BUILD_BREW_NAME);
        if (buildBrewName == null) {
            return getBrewNameFromArtifacts(build);
        } else {
            return buildBrewName.replace(':', '-');
        }
    }

    private static String getBrewVersion(PncBuild build) {
        String buildBrewVersion = MapUtils.getObject(build.getBuild().getAttributes(), BUILD_BREW_VERSION);
        if (buildBrewVersion == null) {
            return getBrewVersionFromArtifacts(build);
        } else {
            return buildBrewVersion;
        }
    }

    private static String getBrewNameFromArtifacts(PncBuild build) {
        String[] identSplit = getIdentifierPartsFromArtifact(build);

        if (identSplit.length == 0) {
            return NO_BUILD_BREW_NAME;
        }

        BuildType buildType = build.getBuild().getBuildConfigRevision().getBuildType();

        // need at least G:A
        if (buildType != BuildType.NPM) {
            if (identSplit.length < 2) {
                return NO_BUILD_BREW_NAME;
            }

            return identSplit[0] + "-" + identSplit[1];
        }

        // should always be N:V
        if (identSplit.length != 2) {
            return NO_BUILD_BREW_NAME;
        }

        return identSplit[0];
    }

    private static String getBrewVersionFromArtifacts(PncBuild build) {
        String[] identSplit = getIdentifierPartsFromArtifact(build);

        if (identSplit.length == 0) {
            return BUILD_ID_ZERO;
        }

        BuildType buildType = build.getBuild().getBuildConfigRevision().getBuildType();

        // needs to be at least G:A:P:V to extract V
        if (buildType != BuildType.NPM) {
            if (identSplit.length < 4) {
                return NO_BUILD_BREW_NAME;
            }

            return identSplit[3];
        }

        // should always be N:V
        if (identSplit.length != 2) {
            return NO_BUILD_BREW_NAME;
        }

        return identSplit[1];
    }

    private static String[] getIdentifierPartsFromArtifact(PncBuild build) {
        Optional<Artifact> optionalArtifact = build.getBuiltArtifacts()
                .stream()
                .filter((art) -> art.getArtifact().isPresent())
                .map(art -> art.getArtifact().get())
                .min(Comparator.comparing(ArtifactRef::getId));
        if (optionalArtifact.isEmpty()) {
            // SHOULD NEVER HAPPEN
            return EMPTY_STRING_ARRAY;
        }

        Artifact firstArtifact = optionalArtifact.get();

        return firstArtifact.getIdentifier().split(":");
    }

    public static String getNVRFromBuildRecord(PncBuild build) {
        return getBrewName(build) + "-" + getBrewVersion(build) + "-1";
    }

    public static KojiBuild pncBuildToKojiBuild(PncBuild pncBuild) {
        KojiBuild kojiBuild = new KojiBuild();
        Build build = pncBuild.getBuild();

        KojiBuildInfo buildInfo = new KojiBuildInfo();
        setMavenBuildInfoFromBuildRecord(pncBuild, buildInfo);

        try {
            buildInfo.setId(Integer.parseInt(build.getId()));
        } catch (NumberFormatException e) {
            buildInfo.setId(-1);
        }

        buildInfo.setName(getBrewName(pncBuild));
        buildInfo.setVersion(getBrewVersion(pncBuild));
        buildInfo.setRelease("1");
        buildInfo.setNvr(
                buildInfo.getName() + "-" + buildInfo.getVersion().replace('-', '_') + "-"
                        + buildInfo.getRelease().replace('-', '_'));
        buildInfo.setCreationTime(Date.from(build.getStartTime()));
        buildInfo.setCompletionTime(Date.from(build.getEndTime()));
        buildInfo.setBuildState(KojiBuildState.COMPLETE);
        buildInfo.setOwnerName(build.getUser().getUsername());
        buildInfo.setSource(
                (build.getScmRepository().getInternalUrl().startsWith("http") ? "git+" : "")
                        + build.getScmRepository().getInternalUrl()
                        + (build.getScmRevision() != null ? "#" + build.getScmRevision() : ""));

        Map<String, Object> extra = new HashMap<>(9, 1.0f);

        extra.put(BUILD_SYSTEM, PNC);
        extra.put(EXTERNAL_BUILD_ID, build.getId());
        // These aren't used by Koji, but we need them to create the hyperlinks for the HTML report
        extra.put(EXTERNAL_PROJECT_ID, build.getProject().getId());
        extra.put(EXTERNAL_BUILD_CONFIGURATION_ID, build.getBuildConfigRevision().getId());

        pncBuild.getBuildPushResult().ifPresent(buildPushResult -> {
            extra.put(EXTERNAL_BREW_BUILD_ID, buildPushResult.getBrewBuildId());
            extra.put(EXTERNAL_BREW_BUILD_URL, buildPushResult.getBrewBuildUrl());
        });

        pncBuild.getProductVersion().ifPresent(productVersion -> {
            // These aren't used by Koji, but we need them to create the hyperlinks for the HTML report
            extra.put(EXTERNAL_PRODUCT_ID, productVersion.getProduct().getId());
            extra.put(EXTERNAL_VERSION_ID, productVersion.getId());

            KojiTagInfo tag = new KojiTagInfo();

            try {
                tag.setId(Integer.parseInt(productVersion.getId()));
            } catch (NumberFormatException e) {
                tag.setId(-1);
            }

            tag.setArches(Collections.singletonList("noarch"));

            String brewName = MapUtils.getObject(productVersion.getAttributes(), BREW_TAG_PREFIX);
            String tagName = brewName != null ? brewName : productVersion.getProduct().getName();

            tag.setName(tagName);

            kojiBuild.setTags(Collections.singletonList(tag));

            KojiTaskInfo taskInfo = new KojiTaskInfo();
            KojiTaskRequest taskRequest = new KojiTaskRequest();
            List<Object> request = Collections.unmodifiableList(Arrays.asList(pncBuild.getSource(), tagName));

            taskRequest.setRequest(request);

            taskInfo.setMethod("build");
            taskInfo.setRequest(request);

            kojiBuild.setTaskInfo(taskInfo);
            kojiBuild.setTaskRequest(taskRequest);
        });

        addPncBuildTypeToExtra(pncBuild.getBuild().getBuildConfigRevision().getBuildType(), extra);
        buildInfo.setExtra(extra);
        buildInfo.setTypeNames(List.of(getBuildType(pncBuild)));

        kojiBuild.setBuildInfo(buildInfo);

        return kojiBuild;
    }

    private static void addPncBuildTypeToExtra(BuildType buildType, Map<String, Object> extra) {
        switch (buildType) {
            case NPM -> extra.put(EXTERNAL_BUILD_TYPE, NPM);
            case GRADLE -> extra.put(EXTERNAL_BUILD_TYPE, GRADLE);
            case SBT -> extra.put(EXTERNAL_BUILD_TYPE, SBT);
            case MVN -> extra.put(EXTERNAL_BUILD_TYPE, MAVEN);
            default -> extra.put(EXTERNAL_BUILD_TYPE, UNKNOWN);
        }
    }

    private static KojiBtype getBuildType(PncBuild pncBuild) {
        BuildType buildType = pncBuild.getBuild().getBuildConfigRevision().getBuildType();

        return switch (buildType) {
            case GRADLE, MVN, MVN_RPM, SBT -> maven;
            case NPM -> npm;
        };
    }

    public static KojiArchiveInfo artifactToKojiArchiveInfo(PncBuild pncBuild, ArtifactRef artifact) {
        Build build = pncBuild.getBuild();
        KojiArchiveInfo archiveInfo = new KojiArchiveInfo();

        try {
            archiveInfo.setBuildId(Integer.parseInt(build.getId()));
        } catch (NumberFormatException e) {
            archiveInfo.setBuildId(-1);
        }

        try {
            archiveInfo.setArchiveId(Integer.parseInt(artifact.getId()));
        } catch (NumberFormatException e) {
            archiveInfo.setArchiveId(-1);
        }

        Map<String, Object> extra = new HashMap<>(3, 1.0f);
        extra.put(EXTERNAL_BUILD_ID, build.getId());
        extra.put(EXTERNAL_ARCHIVE_ID, artifact.getId());
        BuildType buildType = pncBuild.getBuild().getBuildConfigRevision().getBuildType();
        addPncBuildTypeToExtra(buildType, extra);
        archiveInfo.setExtra(extra);

        archiveInfo.setArch("noarch");
        archiveInfo.setFilename(artifact.getFilename());

        archiveInfo.setBuildType(getBuildType(pncBuild));
        archiveInfo.setChecksumType(KojiChecksumType.md5);
        archiveInfo.setChecksum(artifact.getMd5());

        try {
            archiveInfo.setSize(Math.toIntExact(artifact.getSize()));
        } catch (ArithmeticException e) {
            archiveInfo.setSize(-1);
        }

        // TODO: How do we set ArtifactInfo for NPM builds?
        if (buildType != BuildType.NPM) {
            String[] gaecv = artifact.getIdentifier().split(":");

            if (gaecv.length >= 3) {
                archiveInfo.setGroupId(gaecv[0]);
                archiveInfo.setArtifactId(gaecv[1]);
                archiveInfo.setExtension(gaecv[2]);
                archiveInfo.setVersion(gaecv[3]);
                archiveInfo.setClassifier(gaecv.length > 4 ? gaecv[4] : null);
            }
        }

        return archiveInfo;
    }

    public static void fixNullVersion(KojiBuild kojibuild, KojiArchiveInfo archiveInfo) {
        KojiBuildInfo buildInfo = kojibuild.getBuildInfo();
        String buildVersion = buildInfo.getVersion();
        String version = archiveInfo.getVersion();

        if (buildVersion == null || isBuildIdZero(buildVersion)) {
            buildInfo.setVersion(version);
            buildInfo.setNvr(buildInfo.getName() + "-" + 0 + "-" + buildInfo.getRelease().replace('-', '_'));
        }
    }
}

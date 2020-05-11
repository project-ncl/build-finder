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
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Utils {
    private static final Logger LOGGER = LoggerFactory.getLogger(Utils.class);

    private static final String PROPERTIES_FILE = "build-finder.properties";

    private static Properties properties;

    private Utils() {

    }

    public static String normalizePath(FileObject fo, String root) {
        String friendlyURI = fo.getName().getFriendlyURI();
        return friendlyURI.substring(friendlyURI.indexOf(root) + root.length());
    }

    public static void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown();

        try {
            if (!pool.awaitTermination(10000, TimeUnit.MILLISECONDS)) {
                pool.shutdownNow();

                if (!pool.awaitTermination(10000, TimeUnit.MILLISECONDS)) {
                    LOGGER.error("Pool did not terminate");
                }
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();

            Thread.currentThread().interrupt();
        }
    }

    public static String getBuildFinderVersion() {
        return getProperty("version");
    }

    public static String getBuildFinderScmRevision() {
        return getProperty("Scm-Revision");
    }

    private static String getProperty(String name) {
        final String unknown = "unknown";

        if (properties == null) {
            try {
                try (InputStream is = Utils.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
                    if (is == null) {
                        return unknown;
                    }

                    properties = new Properties();

                    properties.load(is);
                }
            } catch (IOException e) {
                return unknown;
            }
        }

        Object value = properties.get(name);

        if (value == null) {
            return unknown;
        }

        return String.valueOf(value);
    }
}

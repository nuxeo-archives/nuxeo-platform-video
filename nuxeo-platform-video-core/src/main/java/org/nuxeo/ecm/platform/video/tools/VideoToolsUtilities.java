/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Thibaud Arguillere
 *     Ricardo Dias
 */
package org.nuxeo.ecm.platform.video.tools;

import org.nuxeo.ecm.core.api.Blob;

import java.io.File;

/**
 * @since 8.4
 */
public class VideoToolsUtilities {

    protected Blob blob;

    private static final File VIDEOTOOLS_TEMP_DIR = new File(System.getProperty("java.io.tmpdir") + "/NuxeoVideoTools");

    private static String VIDEOTOOLS_TEMP_DIR_PATH;

    /**
     * Build a filename, inserting the suffix between the file extension.
     * <p>
     * If the fileName has no extension, the suffix is just added.
     * <p>
     * (if the suffix is null or empty, nothing happens)
     * 
     * @param filename
     * @param suffix
     * @return
     */
    public static String addSuffixToFileName(String filename, String suffix) {
        if (filename == null || filename.isEmpty() || suffix == null
                || suffix.isEmpty()) {
            return filename;
        }

        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            return filename + suffix;
        }

        return filename.substring(0, dotIndex) + suffix
                + filename.substring(dotIndex);
    }

    public static String getTempDirectoryPath() {

        if(VIDEOTOOLS_TEMP_DIR_PATH == null) {
            synchronized (VIDEOTOOLS_TEMP_DIR) {
                if(VIDEOTOOLS_TEMP_DIR_PATH == null) {
                    VIDEOTOOLS_TEMP_DIR.mkdir();
                    VIDEOTOOLS_TEMP_DIR_PATH = VIDEOTOOLS_TEMP_DIR.getAbsolutePath();
                }
            }
        }

        return VIDEOTOOLS_TEMP_DIR_PATH;
    }
}

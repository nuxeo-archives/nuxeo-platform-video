/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thibaud Arguillere
 */
package org.nuxeo.ecm.platform.video.tools;

public class VideoToolsConstants {

    public static final String SCHEMA_VIDEO_CLOSED_CAPTIONS = "VideoClosedCaptions";

    public static final String SCHEMA_PREFIX_VIDEO_CLOSED_CAPTIONS = "videocc";

    public static final String CLOSED_CAPTIONS_BLOB_XPATH = SCHEMA_PREFIX_VIDEO_CLOSED_CAPTIONS
            + ":" + "content";

    public static final String CLOSED_CAPTIONS_FILENAME_XPATH = SCHEMA_PREFIX_VIDEO_CLOSED_CAPTIONS
            + ":" + "fileName";

    // public static final String FACET_VIDEO_CLOSED_CAPTIONS =
    // "VideoClosedCaptions";
}

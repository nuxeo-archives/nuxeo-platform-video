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
 *     Ricardo Dias
 */
package org.nuxeo.ecm.platform.video.tools;

import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;

/**
 * The {@link VideoTool} for slicing video blobs.
 *
 * @since 8.4
 */
public interface VideoSlicer extends VideoTool {

    /**
     * Slices a video blob in parts with approximately the same duration.
     * @param blob the video blob
     * @param duration the duration of each
     * @return
     * @throws NuxeoException
     */
    BlobList slice(Blob blob, String duration) throws NuxeoException;

    /**
     * Slices a video blob with a certain duration and from a specified point in time.
     * @param blob the video blob
     * @param startAt when to start slicing the video blob
     * @param duration the duration of the slice
     * @param encode indicate if the output videos should be re-encoded
     * @return
     * @throws NuxeoException
     */
    Blob slice(Blob blob, String startAt, String duration, boolean encode) throws NuxeoException;
}

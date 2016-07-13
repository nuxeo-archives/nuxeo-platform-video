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

/**
 * Service that allows the execution of different operations in video blobs through the contribution of {@link VideoTool}.
 *
 * @since 8.4
 */
public interface VideoToolsService {

    /**
     * Extracts the closed captions from a video blob.
     * 
     * @param video the input blob
     * @param outputFormat the outformat of the captions (srt, txt, ttxt is the default)
     * @param startAt the start time in format "xx:xx"
     * @param endAt the end time in format "xx:xx"
     * @return
     */
    Blob extractClosedCaptions(Blob video, String outputFormat, String startAt, String endAt);

    /**
     * Concat the input video blobs into a single video blob.
     * 
     * @param videos the input videos
     * @return
     */
    Blob concat(Blob... videos);

    /**
     * Concat the input video blobs into a single video blob.
     * 
     * @param videos
     * @return
     */
    Blob concat(BlobList videos);

    /**
     * Slice a video blob from a start time and the input duration.
     * 
     * @param video the input blob
     * @param startAt the start time in "xx:xx" format
     * @param duration the duration of the sliced blob in seconds
     * @param encode option to re-encode the ouptut video blob
     * @return
     */
    Blob slice(Blob video, String startAt, String duration, boolean encode);

    /**
     * Slice a video blob in n-parts with similar duration.
     * 
     * @param video the input blob
     * @param duration the approximate duration of each part
     * @return
     */
    BlobList slice(Blob video, String duration);

    /**
     * Add a watermark to a video blob.
     * 
     * @param video the input blob
     * @param picture the picture blob to be used as the watermark
     * @param x the x offset starting from the left
     * @param y the y offset starting from the top
     * @return
     */
    Blob watermark(Blob video, Blob picture, String x, String y);
}

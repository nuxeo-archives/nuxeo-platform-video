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

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;

/**
 * @since 8.4
 */
public interface VideoWatermarker extends VideoTool {

    /**
     * Adds a watermark picture to a video blob.
     * @param blob the video blob
     * @param watermark the watermark picture
     * @param x the x position of the watermark, starting from the left
     * @param y the y position of the watermark picture, starting from the top
     * @param outputFilename the filename of the watermarked video
     * @return
     * @throws NuxeoException
     */
    Blob watermark(Blob blob,Blob watermark, String x, String y, String outputFilename) throws NuxeoException;
}

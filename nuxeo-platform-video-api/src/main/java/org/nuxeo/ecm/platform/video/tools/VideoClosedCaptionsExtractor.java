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
public interface VideoClosedCaptionsExtractor extends VideoTool {

    /**
     * Extract Closed Captions from the input video blob.
     * @param outputFormat the output format of the closed captions
     * @param video the input video blob
     * @param startAt the start time from which closed captions should be extract
     * @param endAt the end time from which closed captions should be extract
     * @return
     * @throws NuxeoException
     */
    Blob extract(Blob video, String startAt, String endAt, String outputFormat) throws NuxeoException;
}

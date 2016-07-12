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
 * @since 8.4
 */
public interface VideoConcat extends VideoTool {

    /**
     * Concat the a set of video blobs into a single one.
     * @param blobs the input video blobs
     * @return
     * @throws NuxeoException
     */
    Blob concat(BlobList blobs) throws NuxeoException;

    /**
     * Concat the array of videos into a single one.
     * @param blobs
     * @return
     * @throws NuxeoException
     */
    Blob concat(Blob... blobs) throws NuxeoException;
}

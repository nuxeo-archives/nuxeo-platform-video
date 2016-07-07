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
package org.nuxeo.ecm.platform.video.tools.operations;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.video.tools.VideoSlicer;

@Operation(id = SliceInParts.ID, category = Constants.CAT_CONVERSION, label = "Slice a Video in Parts with equally duration.", description = "Slices the video in n parts of <code>duration</code> each. USing ffmpeg -segment switch with few arguments: Each part will probably not be exactly <code>duration</code> long, this is normal behavior.", aliases = {
        "Video.SliceInParts" })
public class SliceInParts {

    public static final String ID = "Video.SliceInParts";

    @Param(name = "duration", required = false)
    protected long duration;

    @Param(name = "xpath", required = false, values = { "file:content" })
    protected String xpath;

    @OperationMethod(collector = DocumentModelCollector.class)
    public BlobList run(DocumentModel input) throws IOException, CommandNotAvailable {
        String blobPath = (!StringUtils.isEmpty(xpath))? xpath : "file:content";
        return run((Blob) input.getPropertyValue(blobPath));
    }

    @OperationMethod(collector = BlobCollector.class)
    public BlobList run(Blob input) throws NuxeoException {
        VideoSlicer videoSlicer = new VideoSlicer(input);
        try {
            return videoSlicer.slice(String.valueOf(duration));
        } catch (IOException | CommandNotAvailable e) {
            throw new NuxeoException(e);
        }
    }

}

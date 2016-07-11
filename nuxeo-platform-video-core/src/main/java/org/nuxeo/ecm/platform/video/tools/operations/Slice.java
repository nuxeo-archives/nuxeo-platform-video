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
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.video.tools.FFMpegVideoSlicer;
import org.nuxeo.ecm.platform.video.tools.VideoSlicer;

/**
 * @since 8.4
 */
@Operation(id = Slice.ID, category = Constants.CAT_CONVERSION, label = "Slice the Video for a given duration and start time.", description = "Slice the input blob starting at <code>start</code>, for <code>duration</code>. A specific converter can be used. For example, use videoSlicerByCopy for very fast cut (because ffmpeg does not re-encode the video) if you know there will be no frame or timestamp issue in the sliced video", aliases = {
        "Video.Slice" })
public class Slice {

    public static final String ID = "Video.Slice";

    @Param(name = "start", required = false)
    protected String start;

    @Param(name = "duration", required = false)
    protected String duration;

    @Param(name = "commandLine", required = false, values = { "videoSlicer" })
    protected String commandLine;

    @Param(name = "xpath", required = false, values = { "file:content" })
    protected String xpath;

    @OperationMethod
    public Blob run(DocumentModel input) throws OperationException {
        String blobPath = (!StringUtils.isEmpty(xpath)) ? xpath : "file:content";
        return run((Blob) input.getPropertyValue(blobPath));
    }

    @OperationMethod(collector = BlobCollector.class)
    public Blob run(Blob input) throws OperationException {
        VideoSlicer videoSlicer = new FFMpegVideoSlicer();
        if (commandLine != null && !commandLine.isEmpty()) {
            videoSlicer.setCommandLineName(commandLine);
        }
        try {
            return videoSlicer.slice(input, start, duration);
        } catch(NuxeoException e) {
            throw new OperationException(e.getMessage());
        }
    }

}

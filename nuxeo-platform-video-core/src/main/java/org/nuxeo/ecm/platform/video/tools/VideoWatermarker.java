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

import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CloseableFile;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.ecm.platform.video.Video;
import org.nuxeo.runtime.api.Framework;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * The {@link VideoTool} for adding a watermark to a video blob.
 * @since 8.4
 */
public class VideoWatermarker extends VideoTool {

    @Override
    public CmdParameters setupParameters(BlobHolder input, Map<String, Object> parameters) {
        Blob video = input.getBlob();
        String x = (String) parameters.get("x");
        String y = (String) parameters.get("y");
        Blob watermark = (Blob) parameters.get("watermark");

        // Prepare parameters
        String outputFilename = VideoToolsUtilities.addSuffixToFileName(video.getFilename(), "-WM");
        String overlay = "overlay=" + x + ":" + y;

        // Prepare command line parameters
        CmdParameters params = new CmdParameters();
        params.addNamedParameter("sourceFilePath", video.getFile().getAbsolutePath());
        params.addNamedParameter("pictureFilePath", watermark.getFile().getAbsolutePath());
        params.addNamedParameter("filterComplex", overlay);
        params.addNamedParameter("outFilePath", parameters.get("tempDir") + "/" + outputFilename);
        return params;
    }

    @Override
    public BlobHolder buildResult(BlobHolder input, CmdParameters cmdParams, List<String> cmdOutput) {
        Blob result = new FileBlob(new File(cmdParams.getParameter("outFilePath")));
        result.setMimeType(input.getBlob().getMimeType());
        result.setFilename(cmdParams.getParameter("outFilePath"));
        return new SimpleBlobHolder(result);
    }
}
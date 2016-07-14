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

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * The {@link VideoTool} that extracts closed captions.
 *
 * @since 8.4
 */
public class VideoClosedCaptionsExtractor extends VideoTool {

    @Override
    public CmdParameters setupParameters(BlobHolder blobHolder, Map<String, Object> parameters) {
        Blob video = blobHolder.getBlob();
        String outputFormat = (String) parameters.get("outFormat");
        String startAt = (String) parameters.get("startAt");
        String endAt = (String) parameters.get("endAt");

        CmdParameters params = new CmdParameters();
        params.addNamedParameter("sourceFilePath", video.getFile().getAbsolutePath());
        params.addNamedParameter("outFormat", outputFormat);
        if (!StringUtils.isBlank(startAt) && !StringUtils.isBlank(endAt)) {
            params.addNamedParameter("startAt", startAt);
            params.addNamedParameter("endAt", endAt);
        }

        String outputFilename = VideoToolsUtilities.addSuffixToFileName(video.getFilename(), "-CC");
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
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
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.runtime.api.Framework;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * The {@link VideoTool} for slicing video blobs.
 *
 * @since 8.4
 */
public class VideoSlicer extends VideoTool {

    @Override
    public CmdParameters setupParameters(BlobHolder blobHolder, Map<String, Object> parameters) {
        Blob video = blobHolder.getBlob();
        String startAt = (String) parameters.get("startAt");
        String duration = (String) parameters.get("duration");

        CmdParameters params = new CmdParameters();
        params.addNamedParameter("sourceFilePath", video.getFile().getAbsolutePath());
        params.addNamedParameter("duration", duration);

        if (StringUtils.isEmpty(startAt) && !StringUtils.isEmpty(duration)) {
            File folder = new File(parameters.get("tempDir") + "/Segments" + System.currentTimeMillis());
            folder.mkdirs();
            String outFilePattern = folder.getAbsolutePath() + "/"
                    + VideoToolsUtilities.addSuffixToFileName(video.getFilename(), "-%03d");
            params.addNamedParameter("outFilePath", outFilePattern);
            params.addNamedParameter("segmentsPath", folder.getAbsolutePath());
        } else {

            String finalFilename = VideoToolsUtilities.addSuffixToFileName(video.getFilename(),
                    "-" + startAt.replaceAll(":", "") + "-" + duration.replaceAll(":", ""));

            params.addNamedParameter("startAt", startAt);
            params.addNamedParameter("outFilePath", parameters.get("tempDir") + "/" + finalFilename);
        }

        return params;
    }

    @Override
    public BlobHolder buildResult(BlobHolder input, CmdParameters cmdParams, List<String> cmdOutput) {

        String startAt = cmdParams.getParameter("startAt");
        String duration = cmdParams.getParameter("duration");
        if (StringUtils.isEmpty(startAt) && !StringUtils.isEmpty(duration)) {
            BlobList parts = new BlobList();
            File segments = new File(cmdParams.getParameter("segmentsPath"));
            for (File oneFile : segments.listFiles()) {
                FileBlob fb = new FileBlob(oneFile);
                fb.setFilename(oneFile.getName());
                fb.setMimeType(input.getBlob().getMimeType());
                Framework.trackFile(oneFile, parts);
                parts.add(fb);
            }

            return new SimpleBlobHolder(parts);

        } else {
            Blob result = new FileBlob(new File(cmdParams.getParameter("outFilePath")));
            result.setMimeType(input.getBlob().getMimeType());
            result.setFilename(cmdParams.getParameter("outFilePath"));
            return new SimpleBlobHolder(result);
        }
    }
}

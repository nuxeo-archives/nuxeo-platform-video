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
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CloseableFile;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.runtime.api.Framework;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The {@link VideoTool} that joins two or more video blots.
 *
 * @since 8.4
 */
public class VideoConcat extends VideoTool {

    @Override
    public CmdParameters setupParameters(BlobHolder blobHolder, Map<String, Object> parameters) {

        List<Blob> videos = blobHolder.getBlobs();
        String outputFilename = VideoToolsUtilities.addSuffixToFileName(blobHolder.getBlobs().get(0).getFilename(), "-concat");

        List<CloseableFile> sourceClosableFiles = new ArrayList<>();
        File tempFile = null;

        try {
            String list = "";
            CloseableFile cf;
            for (Blob b : videos) {
                cf = b.getCloseableFile();
                sourceClosableFiles.add(cf);
                list += "file '" + cf.getFile().getAbsolutePath() + "'\n";
            }

            tempFile = Framework.createTempFile("NxVTcv-", ".txt");
            Files.write(tempFile.toPath(), list.getBytes());

            // Run the command line
            CmdParameters params = new CmdParameters();
            params.addNamedParameter("listFilePath", tempFile.getAbsolutePath());
            params.addNamedParameter("outFilePath", parameters.get("tempDir") + "/" + outputFilename);

            return params;

        }catch (IOException e) {
            throw new NuxeoException("Could not prepare video concat parameters. " + e.getMessage());
        }
    }

    @Override
    public BlobHolder buildResult(BlobHolder input, CmdParameters cmdParams, List<String> cmdOutput) {
        Blob result = new FileBlob(new File(cmdParams.getParameter("outFilePath")));
        result.setMimeType(input.getBlobs().get(0).getMimeType());
        result.setFilename(cmdParams.getParameter("outFilePath"));
        return new SimpleBlobHolder(result);
    }
}
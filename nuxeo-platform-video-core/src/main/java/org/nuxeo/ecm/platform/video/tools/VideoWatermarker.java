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
package org.nuxeo.ecm.platform.video.tools;

import java.io.IOException;

import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CloseableFile;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.commandline.executor.api.ExecResult;
import org.nuxeo.runtime.api.Framework;

/**
 * @since 8.4
 */
public class VideoWatermarker extends BaseVideoTools {

    protected static final String COMMAND_WATERMARK_WITH_PICTURE = "videoWatermarkWithPicture";

    public VideoWatermarker(Blob inBlob) {
        super(inBlob);
    }

    /* The command line is:
     * ffmpeg -y -i #{sourceFilePath} -i #{pictureFilePath} -filter_complex #{filterComplex} #{outFilePath}
     * 
     * filterComplex will be replaced with "overlay=10:10" (with 10:10 s an example)
     * 
     */
    public Blob watermarkWithPicture(String inFinalFileName, Blob inWatermark, String x, String y) throws IOException,
            CommandNotAvailable, NuxeoException {

        Blob result = null;
        String originalMimeType;
        originalMimeType = blob.getMimeType();

        // Prepare parameters
        if (inFinalFileName == null || inFinalFileName.isEmpty()) {
            inFinalFileName = VideoToolsUtilities.addSuffixToFileName(
                    blob.getFilename(), "-WM");
        }
        String overlay = "overlay=" + x + ":" + y;
        
        CloseableFile sourceBlobFile = null, pictBlobFile = null;
        try {
            sourceBlobFile = blob.getCloseableFile();
            pictBlobFile = inWatermark.getCloseableFile();

            // Prepare command line parameters
            CmdParameters params = new CmdParameters();
            params.addNamedParameter("sourceFilePath", sourceBlobFile.getFile().getAbsolutePath());
            params.addNamedParameter("pictureFilePath", pictBlobFile.getFile().getAbsolutePath());
            params.addNamedParameter("filterComplex", overlay);
            
            String ext = FileUtils.getFileExtension(inFinalFileName);
            result = Blobs.createBlobWithExtension("." + ext);
            params.addNamedParameter("outFilePath", result.getFile().getAbsolutePath());

            // Run and get results
            CommandLineExecutorService cles = Framework.getService(CommandLineExecutorService.class);
            ExecResult clResult = cles.execCommand(COMMAND_WATERMARK_WITH_PICTURE,
                    params);
            
            if (clResult.getError() != null) {
                throw new NuxeoException("Failed to execute the command <"
                        + COMMAND_WATERMARK_WITH_PICTURE + ">", clResult.getError());
            }
            if (!clResult.isSuccessful()) {
                throw new NuxeoException("Failed to execute the command <"
                        + COMMAND_WATERMARK_WITH_PICTURE + ">. Final command [ "
                        + clResult.getCommandLine() + " ] returned with error "
                        + clResult.getReturnCode());
            }
            
            // Build the final blob
            result.setFilename(inFinalFileName);
            result.setMimeType(originalMimeType);
            
            
        } finally {
            if (sourceBlobFile != null) {
                sourceBlobFile.close();
            }
            if (pictBlobFile != null) {
                pictBlobFile.close();
            }
        }

        return result;
    }

}

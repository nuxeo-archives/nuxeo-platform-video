/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Thibaud Arguillere
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
 * 
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

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

import java.io.File;
import java.io.IOException;

import org.nuxeo.common.Environment;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CloseableFile;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.commandline.executor.api.ExecResult;
import org.nuxeo.ecm.platform.video.VideoHelper;
import org.nuxeo.ecm.platform.video.VideoInfo;
import org.nuxeo.runtime.api.Framework;

/**
 * Default implementation of the {@link VideoSlicer} using the ffmpeg command line contribution.
 *
 * @since 8.4
 */
public class FFMpegVideoSlicer implements VideoSlicer {

    private static final String COMMAND_SLICER_DEFAULT = "videoSlicer";

    private static final String COMMAND_SLICER_BY_COPY = "videoSlicerByCopy";

    private static final String COMMAND_SLICER_SEGMENTS = "videoSlicerSegments";

    protected String basePath = Environment.getDefault().getTemp().getPath() + "/NuxeoVideoTools";

    public FFMpegVideoSlicer() {
        File tempDir = new File(basePath);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
    }

    public Blob slice(Blob input, String duration, String startAt, boolean encode) throws NuxeoException {

        Blob sliced = null;
        String COMMAND_SLICER = (encode) ? COMMAND_SLICER_DEFAULT : COMMAND_SLICER_BY_COPY;

        try {
            // Get the final name, adding startAt/duration to the original name
            String finalFilename = VideoToolsUtilities.addSuffixToFileName(input.getFilename(),
                    "-" + startAt.replaceAll(":", "") + "-" + duration.replaceAll(":", ""));

            CloseableFile sourceBlobFile = null;
            try {
                sourceBlobFile = input.getCloseableFile();

                CmdParameters params = new CmdParameters();
                params.addNamedParameter("sourceFilePath", sourceBlobFile.getFile().getAbsolutePath());
                params.addNamedParameter("start", startAt);
                params.addNamedParameter("duration", duration);

                String ext = FileUtils.getFileExtension(finalFilename);
                sliced = Blobs.createBlobWithExtension("." + ext);
                params.addNamedParameter("outFilePath", sliced.getFile().getAbsolutePath());

                CommandLineExecutorService cles = Framework.getService(CommandLineExecutorService.class);
                ExecResult clResult = cles.execCommand(COMMAND_SLICER, params);

                // Get the result, and first, handle errors.
                if (clResult.getError() != null) {
                    throw new NuxeoException("Failed to execute the command <" + COMMAND_SLICER + ">",
                            clResult.getError());
                }

                if (!clResult.isSuccessful()) {
                    throw new NuxeoException("Failed to execute the command <" + COMMAND_SLICER + ">. Final command [ "
                            + clResult.getCommandLine() + " ] returned with error " + clResult.getReturnCode());
                }

                // Build the Blob
                sliced.setFilename(finalFilename);
                sliced.setMimeType(input.getMimeType());

            } finally {
                if (sourceBlobFile != null) {
                    sourceBlobFile.close();
                }
            }
        } catch (IOException e) {
            throw new NuxeoException("Could not slice the video." + e.getMessage());
        } catch (CommandNotAvailable e) {
            throw new NuxeoException("SliceVideo command is not available. " + e.getMessage());
        }

        return sliced;
    }

    public BlobList slice(Blob input, String duration) throws NuxeoException {

        BlobList parts = new BlobList();

        VideoInfo vi = VideoHelper.getVideoInfo(input);
        if (Double.valueOf(duration) >= vi.getDuration()) {
            parts.add(input);
        } else {
            try {
                String mimeType = input.getMimeType();
                CloseableFile sourceBlobFile = null;
                try {
                    sourceBlobFile = input.getCloseableFile();

                    CmdParameters params = new CmdParameters();
                    params.addNamedParameter("sourceFilePath", sourceBlobFile.getFile().getAbsolutePath());
                    params.addNamedParameter("duration", duration);

                    File folder = new File(basePath + "/Segments" + System.currentTimeMillis());
                    folder.mkdir();
                    String outFilePattern = folder.getAbsolutePath() + "/"
                            + VideoToolsUtilities.addSuffixToFileName(input.getFilename(), "-%03d");
                    params.addNamedParameter("outFilePath", outFilePattern);

                    CommandLineExecutorService cles = Framework.getService(CommandLineExecutorService.class);
                    ExecResult clResult = cles.execCommand(COMMAND_SLICER_SEGMENTS, params);
                    // Get the result, and first, handle errors.
                    if (clResult.getError() != null) {
                        System.out.println("Failed to execute the command <" + COMMAND_SLICER_SEGMENTS + "> : "
                                + clResult.getError());
                        throw new NuxeoException("Failed to execute the command <" + COMMAND_SLICER_SEGMENTS + ">",
                                clResult.getError());
                    }

                    if (!clResult.isSuccessful()) {
                        throw new NuxeoException("Failed to execute the command <" + COMMAND_SLICER_SEGMENTS
                                + ">. Final command [ " + clResult.getCommandLine() + " ] returned with error "
                                + clResult.getReturnCode());
                    }

                    for (File oneFile : folder.listFiles()) {
                        FileBlob fb = new FileBlob(oneFile);

                        fb.setFilename(oneFile.getName());
                        fb.setMimeType(mimeType);
                        Framework.trackFile(oneFile, parts);

                        parts.add(fb);
                    }

                } finally {
                    if (sourceBlobFile != null) {
                        sourceBlobFile.close();
                    }
                }
            } catch (IOException e) {
                throw new NuxeoException("Could not slice the video." + e.getMessage());
            } catch (CommandNotAvailable e) {
                throw new NuxeoException("SliceVideo command is not available. " + e.getMessage());
            }

        }

        return parts.size() == 0 ? null : parts;
    }
}

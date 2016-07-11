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

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

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

public class FFMpegVideoSlicer implements VideoSlicer {

    public static final String COMMAND_SLICER_DEFAULT = "videoSlicer";

    public static final String COMMAND_SLICER_BY_COPY = "videoSlicerByCopy";

    public static final String COMMAND_SLICER_SEGMENTS = "videoSlicerSegments";

    protected DecimalFormat s_msFormat = new DecimalFormat("#.###");

    protected String commandLineName = COMMAND_SLICER_DEFAULT;

    /**
     * Slices the video at start for duration and returns a new blob
     *
     * @param input
     * @param duration
     * @param start
     * @return Blob, slice of the original
     * @throws NuxeoException
     * @since 8.4
     */
    public Blob slice(Blob input, String duration, String start) throws NuxeoException {

        Blob sliced = null;

        try {
            // Get the final name, adding start/duration to the original name
            String finalFileName = VideoToolsUtilities.addSuffixToFileName(input.getFilename(),
                    "-" + start.replaceAll(":", "") + "-" + duration.replaceAll(":", ""));

            CloseableFile sourceBlobFile = null;
            try {
                sourceBlobFile = input.getCloseableFile();

                CmdParameters params = new CmdParameters();
                params.addNamedParameter("sourceFilePath", sourceBlobFile.getFile().getAbsolutePath());

                params.addNamedParameter("start", start);
                params.addNamedParameter("duration", duration);

                String ext = FileUtils.getFileExtension(finalFileName);
                sliced = Blobs.createBlobWithExtension("." + ext);
                params.addNamedParameter("outFilePath", sliced.getFile().getAbsolutePath());

                CommandLineExecutorService cles = Framework.getService(CommandLineExecutorService.class);
                ExecResult clResult = cles.execCommand(commandLineName, params);

                // Get the result, and first, handle errors.
                if (clResult.getError() != null) {
                    throw new NuxeoException("Failed to execute the command <" + commandLineName + ">",
                            clResult.getError());
                }

                if (!clResult.isSuccessful()) {
                    throw new NuxeoException("Failed to execute the command <" + commandLineName + ">. Final command [ "
                            + clResult.getCommandLine() + " ] returned with error " + clResult.getReturnCode());
                }

                // Build the Blob
                sliced.setFilename(finalFileName);
                sliced.setMimeType(input.getMimeType());

            } finally {
                if (sourceBlobFile != null) {
                    sourceBlobFile.close();
                }
            }
        } catch(IOException e) {
            throw new NuxeoException("Could not slice the video." + e.getMessage());
        } catch (CommandNotAvailable e) {
            throw new NuxeoException("Slice command is not available. "+ e.getMessage());
        }

        return sliced;
    }

    /**
     * Slices the video in n segments of inDuration each (with possible approximations)
     *
     * @param input
     * @param duration
     * @return 1-n blobs of same duration (with the last one adjusted)
     * @throws NuxeoException
     * @since 7.1
     */
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

                    File folder = new File(VideoToolsUtilities.getTempDirectoryPath() + "/" + "Segments-"
                            + java.util.UUID.randomUUID().toString().replace("-", ""));
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
            } catch(IOException e) {
                throw new NuxeoException("Could not slice the video." + e.getMessage());
            } catch (CommandNotAvailable e) {
                throw new NuxeoException("Slice command is not available. "+ e.getMessage());
            }

        }

        return parts.size() == 0 ? null : parts;
    }

    public void setCommandLineName(String inCommandLineName) {
        commandLineName = inCommandLineName;
    }

    public String getCommandLineName() {
        return commandLineName;
    }
}

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.Blobs;
import org.nuxeo.ecm.core.api.CloseableFile;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandAvailability;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.commandline.executor.api.ExecResult;
import org.nuxeo.runtime.api.Framework;

/**
 *
 * Default implementation of the {@link VideoClosedCaptionsExtractor} for extracting closed captions from video blobs.
 * The closed captions are extracted in a text file (depending on the requests output format).
 *
 * @since 8.4
 */
public class FFMpegCCExtractor implements VideoClosedCaptionsExtractor {

    public static final String COMMAND_FULL_VIDEO = "videoClosedCaptionsExtractor";

    public static final String COMMAND_SLICED_VIDEO = "videoPartClosedCaptionsExtractor";

    public static final String DEFAULT_OUTFORMAT = "ttxt";

    public static final List<String> TEXT_OUTFORMATS = Collections.unmodifiableList(
            Arrays.asList("srt", "txt", "ttxt"));

    public Blob extract(Blob blob, String startAt, String endAt) throws CommandNotAvailable, IOException {
        return extract(blob, startAt, endAt, DEFAULT_OUTFORMAT);
    }

    public Blob extract(Blob blob, String startAt, String endAt, String outputFormat) throws NuxeoException {

        if (blob == null) return null;

        Blob closedCaptions = null;
        try {
            CloseableFile sourceBlobFile = null;
            try {
                sourceBlobFile = blob.getCloseableFile();

                // set the command line parameters
                CmdParameters params = new CmdParameters();
                params.addNamedParameter("sourceFilePath", sourceBlobFile.getFile().getAbsolutePath());

                if (StringUtils.isBlank(outputFormat)) {
                    outputFormat = DEFAULT_OUTFORMAT;
                }
                params.addNamedParameter("outFormat", outputFormat);

                String commandLineName = COMMAND_FULL_VIDEO;
                if (!StringUtils.isBlank(startAt) && !StringUtils.isBlank(endAt)) {
                    commandLineName = COMMAND_SLICED_VIDEO;
                    params.addNamedParameter("startAt", startAt);
                    params.addNamedParameter("endAt", endAt);
                }

                closedCaptions = Blobs.createBlobWithExtension("." + outputFormat);
                params.addNamedParameter("outFilePath", closedCaptions.getFile().getAbsolutePath());

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

                // ccextractor always create a file, even if there is no captions.
                // We must check if the file is empty or not, while handling BOMs of
                // Unicode files.
                // Let's say that less than 5 bytes, we don't have a caption.
                File resultFile = closedCaptions.getFile();
                if (resultFile.exists()) {
                    if (resultFile.length() > 5) {
                        closedCaptions.setFilename(blob.getFilename() + "." + outputFormat);
                        if (isTextOutFormat(outputFormat)) {
                            closedCaptions.setMimeType("text/plain");
                        }
                    }
                }

            } finally {
                if (sourceBlobFile != null) {
                    sourceBlobFile.close();
                }
            }
        } catch(IOException e) {
            throw new NuxeoException("Could not extract closed captions from video blob. " + e.getMessage());
        } catch(CommandNotAvailable e) {
            throw new NuxeoException("Command for closed captions extraction is not available. " + e.getMessage());
        }

        return closedCaptions;
    }

    protected boolean isTextOutFormat(String inFormat) {
        return TEXT_OUTFORMATS.contains(inFormat);
    }

    public static boolean ccextractorIsAvailable() {
        CommandLineExecutorService cles = Framework.getService(CommandLineExecutorService.class);
        CommandAvailability ca = cles.getCommandAvailability(FFMpegCCExtractor.COMMAND_FULL_VIDEO);
        return ca.isAvailable();
    }
}

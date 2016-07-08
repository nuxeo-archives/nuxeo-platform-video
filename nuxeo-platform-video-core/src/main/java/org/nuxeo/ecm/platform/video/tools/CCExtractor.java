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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * The closed captions are extracted in a text file (depending on the requests
 * output format)
 *
 * @since 8.4
 */
public class CCExtractor {

    @SuppressWarnings("unused")
    private static final Log log = LogFactory.getLog(CCExtractor.class);

    public static final String COMMAND_FULL_VIDEO = "videoClosedCaptionsExtractor";

    public static final String COMMAND_SLICED_VIDEO = "videoPartClosedCaptionsExtractor";

    public static final String DEFAULT_OUTFORMAT = "ttxt";

    public static final List<String> TEXT_OUTFORMATS = Collections.unmodifiableList(Arrays.asList(
            "srt", "txt", "ttxt"));

    protected String startAt;

    protected String endAt;
    
    protected static int ccextractorIsAvailable = -1;

    protected Blob blob;

    public CCExtractor(Blob blob) {
        this.blob = blob;

    }

    public CCExtractor(Blob blob, String inStartAt, String inEndAt) {
        this(blob);

        setStartAt(inStartAt);
        setEndAt(inEndAt);
        
    }
    
    public static boolean ccextractorIsAvailable() {
        
        if(ccextractorIsAvailable == -1) {
            CommandLineExecutorService cles = Framework.getService(CommandLineExecutorService.class);
            CommandAvailability ca = cles.getCommandAvailability(CCExtractor.COMMAND_FULL_VIDEO);
            ccextractorIsAvailable = ca.isAvailable() ? 1 : 0;
        }
        
        return ccextractorIsAvailable == 1;
    }

    public Blob extractCC() throws CommandNotAvailable, IOException {
        return extractCC(null);
    }

    protected boolean isTextOutFormat(String inFormat) {
        return TEXT_OUTFORMATS.contains(inFormat);
    }

    public Blob extractCC(String theOutFormat) throws CommandNotAvailable,
            IOException {

        Blob blobCC = null;

        if (blob == null) {
            return null;
        }

        CloseableFile sourceBlobFile = null;
        try {
        CmdParameters params = new CmdParameters();
        
        sourceBlobFile = blob.getCloseableFile();
        params.addNamedParameter("sourceFilePath", sourceBlobFile.getFile().getAbsolutePath());

        if (StringUtils.isBlank(theOutFormat)) {
            theOutFormat = DEFAULT_OUTFORMAT;
        }
        params.addNamedParameter("outFormat", theOutFormat);

        String commandLineName = COMMAND_FULL_VIDEO;
        if (!StringUtils.isBlank(startAt) && !StringUtils.isBlank(endAt)) {
            commandLineName = COMMAND_SLICED_VIDEO;
            params.addNamedParameter("startAt", startAt);
            params.addNamedParameter("endAt", endAt);
        }
        
        blobCC = Blobs.createBlobWithExtension("." + theOutFormat);
        params.addNamedParameter("outFilePath", blobCC.getFile().getAbsolutePath());

        CommandLineExecutorService cles = Framework.getService(CommandLineExecutorService.class);
        ExecResult clResult = cles.execCommand(commandLineName, params);

        // Get the result, and first, handle errors.
        if (clResult.getError() != null) {
            throw new NuxeoException("Failed to execute the command <"
                    + commandLineName + ">", clResult.getError());
        }

        if (!clResult.isSuccessful()) {
            throw new NuxeoException("Failed to execute the command <"
                    + commandLineName + ">. Final command [ "
                    + clResult.getCommandLine() + " ] returned with error "
                    + clResult.getReturnCode());
        }
        
        // ccextractor always create a file, even if there is no captions.
        // We must check if the file is empty or not, while handling BOMs of
        // Unicode files.
        // Let's say that less than 5 bytes, we don't have a caption.
        File resultFile = blobCC.getFile();
        if(resultFile.exists()) {
            if (resultFile.length() > 5) {
                blobCC.setFilename(blob.getFilename() + "." + theOutFormat);
                if (isTextOutFormat(theOutFormat)) {
                    blobCC.setMimeType("text/plain");
                }
            }
        }
        
        } finally {
            if(sourceBlobFile != null) {
                sourceBlobFile.close();
            }
        }
        
        return blobCC;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getEndAt() {
        return endAt;
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }
}

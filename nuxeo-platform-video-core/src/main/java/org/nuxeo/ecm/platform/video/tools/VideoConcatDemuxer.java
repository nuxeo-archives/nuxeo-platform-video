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
import java.nio.file.Files;
import java.util.ArrayList;

import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.core.util.BlobList;
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
 * We use ffmpeg demuxer (see https://trac.ffmpeg.org/wiki/Concatenate) Syntax is: ffmpeg -f concat -i mylist.txt -c
 * copy output
 * <p>
 * With mylist.txt: <code>
 * file '/path/to/file1'
 * file '/path/to/file2'
 * file '/path/to/file3'
 * </code>
 * <p>
 * Quote from ffmpeg doc:
 * <p>
 * <quote> The timestamps in the files are adjusted so that the first file starts at 0 and each next file starts where
 * the previous one finishes. Note that it is done globally and may cause gaps if all streams do not have exactly the
 * same length. All files must have the same streams (same codecs, same time base, etc.). The duration of each file is
 * used to adjust the timestamps of the next file: if the duration is incorrect (because it was computed using the
 * bit-rate or because the file is truncated, for example), it can cause artifacts. The duration directive can be used
 * to override the duration stored in each file. [snip] The concat demuxer can support variable frame rate, but it
 * currently requires that all files have the same time base for the corresponding files. </quote> So basically, this
 * will work with movies of the same type and same timestamps.
 * 
 * @since 7.1
 */
public class VideoConcatDemuxer {

    protected static final String COMMAND_CONCAT_VIDEOS_DEMUXER = "concatVideos-demuxer";

    public Blob concat(BlobList blobs) throws IOException, CommandNotAvailable, NuxeoException {
        return concat(blobs, null);
    }

    /*
     * The command line is: ffmpeg -f concat -i #{listFilePath} -c copy #{outFilePath}
     */
    public Blob concat(BlobList blobs, String outputFilename) throws IOException, CommandNotAvailable, NuxeoException {

        Blob result = null;
        String originalMimeType;

        if (blobs.size() == 0) {
            return null;
        }

        if (outputFilename == null || outputFilename.isEmpty()) {
            outputFilename = VideoToolsUtilities.addSuffixToFileName(blobs.get(0).getFilename(), "-concat");
        }

        originalMimeType = blobs.get(0).getMimeType();

        ArrayList<CloseableFile> sourceClosableFiles = new ArrayList<CloseableFile>();
        File tempFile = null;
        try {

            String list = "";
            CloseableFile cf;
            for (Blob b : blobs) {
                cf = b.getCloseableFile();
                sourceClosableFiles.add(cf);
                list += "file '" + cf.getFile().getAbsolutePath() + "'\n";

            }

            tempFile = Framework.createTempFile("NxVTcv-", ".txt");
            Files.write(tempFile.toPath(), list.getBytes());

            String ext = FileUtils.getFileExtension(outputFilename);
            result = Blobs.createBlobWithExtension("." + ext);
            String outputFilePath = result.getFile().getAbsolutePath();

            // Run the command line
            CmdParameters params = new CmdParameters();
            params.addNamedParameter("listFilePath", tempFile.getAbsolutePath());
            params.addNamedParameter("outFilePath", outputFilePath);

            CommandLineExecutorService cles = Framework.getService(CommandLineExecutorService.class);
            ExecResult clResult = cles.execCommand(COMMAND_CONCAT_VIDEOS_DEMUXER, params);

            // Get the result, and first, handle errors.
            if (clResult.getError() != null) {
                throw new NuxeoException("Failed to execute the command <" + COMMAND_CONCAT_VIDEOS_DEMUXER + ">",
                        clResult.getError());
            }

            if (!clResult.isSuccessful()) {
                throw new NuxeoException("Failed to execute the command <" + COMMAND_CONCAT_VIDEOS_DEMUXER
                        + ">. Final command [ " + clResult.getCommandLine() + " ] returned with error "
                        + clResult.getReturnCode());
            }

            // Update the Blob
            result.setFilename(outputFilename);
            result.setMimeType(originalMimeType);

        } finally {
            for (CloseableFile cf : sourceClosableFiles) {
                if (cf != null) {
                    cf.close();
                }
            }

            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }

        return result;
    }

}

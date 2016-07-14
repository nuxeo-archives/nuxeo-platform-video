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
package org.nuxeo.ecm.platform.video.tools.service;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.Environment;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandAvailability;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandLineExecutorService;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.commandline.executor.api.ExecResult;
import org.nuxeo.ecm.platform.video.tools.VideoClosedCaptionsExtractor;
import org.nuxeo.ecm.platform.video.tools.VideoConcat;
import org.nuxeo.ecm.platform.video.tools.VideoSlicer;
import org.nuxeo.ecm.platform.video.tools.VideoTool;
import org.nuxeo.ecm.platform.video.tools.VideoToolsService;
import org.nuxeo.ecm.platform.video.tools.VideoWatermarker;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@link VideoToolsService} default implementation for handling video blobs. It provides extension points for
 * handling video operations, such as concat, slice, watermark and extract closed captions.
 *
 * @since 8.4
 */
public class VideoToolsServiceImpl extends DefaultComponent implements VideoToolsService {

    protected static final Log log = LogFactory.getLog(VideoToolsServiceImpl.class);

    public static final String DEFAULT_OUTFORMAT = "ttxt";

    public static final List<String> TEXT_OUTFORMATS = Collections.unmodifiableList(
            Arrays.asList("srt", "txt", "ttxt"));

    protected String tempDir = Environment.getDefault().getTemp().getPath() + "/NuxeoVideoTools";

    protected Map<String, String> videoToolsCommandLines;

    protected Map<String, Class> videoTools;

    /* Tools Names */
    public String WATERMARKER_TOOL = "watermarkerTool";

    public String SLICER_TOOL = "slicerTool";

    public String CONCAT_TOOL = "concatTool";

    public String CLOSED_CAPTIONS_EXTRACTOR_TOOL = "ccExtractorTool";

    /* Commandline Tools Available */
    protected static final String COMMAND_WATERMARK_WITH_PICTURE = "videoWatermarkWithPicture";

    protected static final String COMMAND_CONCAT_VIDEOS = "videoConcat";

    protected static final String COMMAND_SLICER_DEFAULT = "videoSlicer";

    protected static final String COMMAND_SLICER_BY_COPY = "videoSlicerByCopy";

    protected static final String COMMAND_SLICER_SEGMENTS = "videoSlicerSegments";

    protected static final String COMMAND_FULL_VIDEO = "videoClosedCaptionsExtractor";

    protected static final String COMMAND_SLICED_VIDEO = "videoPartClosedCaptionsExtractor";

    @Override
    public void activate(ComponentContext context) {
        super.activate(context);

        File tempDir = new File(this.tempDir);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        videoToolsCommandLines = new HashMap();
        videoToolsCommandLines.put(WATERMARKER_TOOL, COMMAND_WATERMARK_WITH_PICTURE);
        videoToolsCommandLines.put(CLOSED_CAPTIONS_EXTRACTOR_TOOL, COMMAND_FULL_VIDEO);
        videoToolsCommandLines.put(SLICER_TOOL, COMMAND_SLICED_VIDEO);
        videoToolsCommandLines.put(CONCAT_TOOL, COMMAND_CONCAT_VIDEOS);

        videoTools = new HashMap<>();
        videoTools.put(WATERMARKER_TOOL, VideoWatermarker.class);
        videoTools.put(SLICER_TOOL, VideoSlicer.class);
        videoTools.put(CONCAT_TOOL, VideoConcat.class);
        videoTools.put(CLOSED_CAPTIONS_EXTRACTOR_TOOL, VideoClosedCaptionsExtractor.class);
    }

    @Override
    public void deactivate(ComponentContext context) {
        super.deactivate(context);
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
    }

    @Override
    public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
    }

    @Override
    public Blob extractClosedCaptions(Blob video, String outputFormat, String startAt, String endAt) {

        Map<String, Object> parameters = new HashMap<>();
        String outFormat = outputFormat;
        if (StringUtils.isBlank(outFormat)) {
            outFormat = DEFAULT_OUTFORMAT;
        }
        parameters.put("outFormat", outFormat);
        parameters.put("startAt", startAt);
        parameters.put("endAt", endAt);

        String commandLine = (!StringUtils.isBlank(startAt) && !StringUtils.isBlank(endAt)) ? COMMAND_SLICED_VIDEO
                : COMMAND_FULL_VIDEO;
        BlobHolder result = execute(CLOSED_CAPTIONS_EXTRACTOR_TOOL, commandLine, new SimpleBlobHolder(video),
                parameters);
        return result.getBlob();
    }

    @Override
    public Blob concat(BlobList videos) {
        Map<String, Object> parameters = new HashMap<>();

        BlobHolder blobHolder = execute(CONCAT_TOOL, COMMAND_CONCAT_VIDEOS, new SimpleBlobHolder(videos), parameters);
        return blobHolder.getBlob();
    }

    @Override
    public Blob slice(Blob video, String startAt, String duration, boolean encode) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("duration", duration);
        parameters.put("startAt", startAt);

        String COMMAND_SLICER = (encode) ? COMMAND_SLICER_DEFAULT : COMMAND_SLICER_BY_COPY;
        BlobHolder result = execute(SLICER_TOOL, COMMAND_SLICER, new SimpleBlobHolder(video), parameters);
        return result.getBlob();
    }

    @Override
    public BlobList slice(Blob video, String duration) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("duration", duration);

        BlobHolder result = execute(SLICER_TOOL, COMMAND_SLICER_SEGMENTS, new SimpleBlobHolder(video), parameters);
        return new BlobList(result.getBlobs());
    }

    @Override
    public Blob watermark(Blob video, Blob picture, String x, String y) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("watermark", picture);
        parameters.put("x", x);
        parameters.put("y", y);

        BlobHolder result = execute(WATERMARKER_TOOL, COMMAND_WATERMARK_WITH_PICTURE, new SimpleBlobHolder(video),
                parameters);
        return result.getBlob();
    }

    private BlobHolder execute(String toolName, String commandLineName, BlobHolder blobHolder,
            Map<String, Object> parameters) {
        BlobHolder result = null;

        if (isToolAvailable(toolName)) {
            Class toolClass = videoTools.get(toolName);
            try {
                // inject the temporary dir
                parameters.put("tempDir", tempDir);

                VideoTool tool = (VideoTool) toolClass.newInstance();
                CmdParameters cmdParams = tool.setupParameters(blobHolder, parameters);

                CommandLineExecutorService cles = Framework.getService(CommandLineExecutorService.class);
                ExecResult clResult = cles.execCommand(commandLineName, cmdParams);

                // Get the result, and first, handle errors.
                if (clResult.getError() != null) {
                    throw new NuxeoException("Failed to execute the command <" + commandLineName + ">",
                            clResult.getError());
                }

                if (!clResult.isSuccessful()) {
                    throw new NuxeoException("Failed to execute the command <" + commandLineName + ">. Final command [ "
                            + clResult.getCommandLine() + " ] returned with error " + clResult.getReturnCode());
                }

                result = tool.buildResult(blobHolder, cmdParams, clResult.getOutput());

            } catch (CommandNotAvailable e) {
                throw new NuxeoException("The video tool is not available. " + e.getMessage());
            } catch (InstantiationException e) {
                throw new NuxeoException("The video tool is not available. " + e.getMessage());
            } catch (IllegalAccessException e) {
                throw new NuxeoException("The video tool is not available. " + e.getMessage());
            }
        }

        return result;
    }

    public boolean isToolAvailable(String toolName) {
        String commandLine = videoToolsCommandLines.get(toolName);
        CommandAvailability ca = Framework.getService(CommandLineExecutorService.class)
                                          .getCommandAvailability(commandLine);
        return ca.isAvailable();
    }

}

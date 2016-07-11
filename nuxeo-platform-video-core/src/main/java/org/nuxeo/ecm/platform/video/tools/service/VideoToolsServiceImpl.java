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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.platform.video.tools.VideoClosedCaptionsExtractor;
import org.nuxeo.ecm.platform.video.tools.VideoConcat;
import org.nuxeo.ecm.platform.video.tools.VideoSlicer;
import org.nuxeo.ecm.platform.video.tools.VideoToolDescriptor;
import org.nuxeo.ecm.platform.video.tools.VideoToolsService;
import org.nuxeo.ecm.platform.video.tools.VideoWatermarker;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

import java.util.HashMap;
import java.util.Map;

/**
 * @since 8.4
 */
public class VideoToolsServiceImpl extends DefaultComponent implements VideoToolsService {

    protected static final Log log = LogFactory.getLog(VideoToolsServiceImpl.class);

    protected static final String WATERMARK_TOOL = "watermarkTool";
    protected static final String SLICER_TOOL = "sliceTool";
    protected static final String CONCAT_TOOL = "concatTool";
    protected static final String CC_EXTRACTER_TOOL = "ccExtractorTool";

    protected final Map<String, VideoToolDescriptor> toolDescriptors = new HashMap<>();

    @Override
    public void activate(ComponentContext context) {
        super.activate(context);
    }

    @Override
    public void deactivate(ComponentContext context) {
        super.deactivate(context);
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        VideoToolDescriptor desc = (VideoToolDescriptor) contribution;
        toolDescriptors.put(desc.getName(), desc);
        log.info("Tool " + desc.getName() + " was successfully registered.");
    }

    @Override
    public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        VideoToolDescriptor desc = (VideoToolDescriptor) contribution;
        if (toolDescriptors.containsKey(desc.getName())) {
            toolDescriptors.remove(desc.getName());
            log.info("Tool " + desc.getName() + " was successfully unregistered.");
        }
    }

    /**
     * Default implementation of the VideoToolsService.
     **/

    @Override
    public Blob extractClosedCaptions(Blob video, String outputFormat, String startAt, String endAt) {
        if (toolDescriptors.containsKey(CC_EXTRACTER_TOOL)) {
            VideoToolDescriptor desc = toolDescriptors.get(CC_EXTRACTER_TOOL);
            VideoClosedCaptionsExtractor ccExtratorTool = (VideoClosedCaptionsExtractor) desc.getVideoTool();
            return ccExtratorTool.extract(outputFormat, video, startAt, endAt);
        }
        return null;
    }

    @Override
    public Blob concat(BlobList videos) {
        if (toolDescriptors.containsKey(CONCAT_TOOL)) {
            VideoToolDescriptor desc = toolDescriptors.get(CONCAT_TOOL);
            VideoConcat videoConcat = (VideoConcat) desc.getVideoTool();
            return videoConcat.concat(videos);
        }
        return null;
    }

    @Override
    public Blob concat(Blob... videos) {
        BlobList blobs = new BlobList();
        for (int i = 0; i < videos.length; i++) {
            blobs.add(videos[i]);
        }
        return concat(blobs);
    }

    @Override
    public Blob convert(Blob video, long height, String scale, String converter) {
        //TODO - what do do here with the convert?
        return null;
    }

    @Override
    public Blob slice(Blob video, String startAt, String duration) {
        if (toolDescriptors.containsKey(SLICER_TOOL)) {
            VideoToolDescriptor desc = toolDescriptors.get(SLICER_TOOL);
            VideoSlicer slicer = (VideoSlicer) desc.getVideoTool();
            return slicer.slice(video, startAt, duration);
        }
        return null;
    }

    @Override
    public BlobList slice(Blob video, String duration) {
        if (toolDescriptors.containsKey(SLICER_TOOL)) {
            VideoToolDescriptor desc = toolDescriptors.get(SLICER_TOOL);
            VideoSlicer slicer = (VideoSlicer) desc.getVideoTool();
            return slicer.slice(video, duration);
        }
        return null;
    }

    @Override
    public Blob watermark(Blob video, Blob picture, String x, String y) {
        if (toolDescriptors.containsKey(WATERMARK_TOOL)) {
            VideoToolDescriptor desc = toolDescriptors.get(WATERMARK_TOOL);
            VideoWatermarker watermarker = (VideoWatermarker) desc.getVideoTool();
            return watermarker.watermark(video, null, picture, x, y);
        }
        return null;
    }
}

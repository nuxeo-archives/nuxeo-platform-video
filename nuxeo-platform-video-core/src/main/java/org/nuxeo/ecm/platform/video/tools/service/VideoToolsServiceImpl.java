package org.nuxeo.ecm.platform.video.tools.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.video.tools.VideoToolsService;
import org.nuxeo.ecm.platform.video.tools.VideoWatermarker;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

import java.io.IOException;

public class VideoToolsServiceImpl extends DefaultComponent implements VideoToolsService {

    protected static final Log log = LogFactory.getLog(VideoToolsServiceImpl.class);

    @Override
    public void activate(ComponentContext context) {
    }

    @Override
    public void deactivate(ComponentContext context) {
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
    }

    @Override
    public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
    }

    /**
     * Default implementation of the VideoToolsService.
     **/

    @Override
    public Blob extractClosedCaptions(Blob video, String outputFormat, String startAt, String endAt) {
        return null;
    }

    @Override
    public Blob concat(Blob... videos) {
        return null;
    }

    @Override
    public Blob convert(Blob video, long height, String scale, String converter) {
        return null;
    }

    @Override
    public Blob slice(Blob video, String start, String duration) {
        return null;
    }

    @Override
    public Blob sliceEqualParts(Blob video, long duration) {
        return null;
    }

    @Override
    public Blob watermark(Blob video, Blob picture, String x, String y) {
        try {
            return new VideoWatermarker().watermark(video, null, picture, x, y);
        } catch (IOException e) {
            throw new NuxeoException("Error while adding the watermark to the video. " + e.getMessage());
        } catch (CommandNotAvailable commandNotAvailable) {
            throw new NuxeoException(String.format("Command %s not available", commandNotAvailable.getMessage()));
        }

    }
}

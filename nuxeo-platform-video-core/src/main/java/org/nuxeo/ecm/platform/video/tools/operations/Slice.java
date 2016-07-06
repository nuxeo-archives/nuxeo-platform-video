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

package org.nuxeo.ecm.platform.video.tools.operations;

import java.io.IOException;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.video.tools.VideoSlicer;

/**
 * Slice the input blob starting at <code>start</code>, for
 * <code>duration</code>. A specific command line can be used. For example, use
 * videoSlicerByCopy for very fast cut (because ffmpeg does not re-encode the
 * video) if you know there will be no frame or timestamp issue in the sliced
 * video
 */
@Operation(id = Slice.ID, category = Constants.CAT_CONVERSION, label = "Video: Slice", description = "Slice the input blob starting at <code>start</code>, for <code>duration</code>. A specific converter can be used. For example, use videoSlicerByCopy for very fast cut (because ffmpeg does not re-encode the video) if you know there will be no frame or timestamp issue in the sliced video")
public class Slice {

    public static final String ID = "Video.Slice";

    @Param(name = "start", required = false)
    protected String start;

    @Param(name = "duration", required = false)
    protected String duration;

    @Param(name = "commandLine", required = false, values = { "videoSlicer" })
    protected String commandLine;

    @OperationMethod(collector = BlobCollector.class)
    public Blob run(Blob inBlob) throws NuxeoException {

        Blob result = null;

        VideoSlicer slicer = new VideoSlicer(inBlob);
        if (commandLine != null && !commandLine.isEmpty()) {
            slicer.setCommandLineName(commandLine);
        }
        try {
            result = slicer.slice(start, duration);
        } catch (IOException | CommandNotAvailable e) {
            throw new NuxeoException(e);
        }

        return result;
    }

}

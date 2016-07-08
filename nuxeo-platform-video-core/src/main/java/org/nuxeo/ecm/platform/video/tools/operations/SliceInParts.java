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
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.video.tools.VideoSlicer;

/**
 * Slices the video in n parts of <code>duration</code> each. USing ffmpeg
 * -segment switch with few arguments: Each part will probably not be exactly
 * <code>duration</code> long, this is normal behavior.
 */
@Operation(id = SliceInParts.ID, category = Constants.CAT_CONVERSION, label = "Video: Slice in Parts", description = "Slices the video in n parts of <code>duration</code> each. USing ffmpeg -segment switch with few arguments: Each part will probably not be exactly <code>duration</code> long, this is normal behavior.")
public class SliceInParts {

    public static final String ID = "Video.SliceInParts";

    @Param(name = "duration", required = false)
    protected long duration;

    @OperationMethod
    public BlobList run(Blob inBlob) throws NuxeoException {

        BlobList parts = null;

        VideoSlicer vs = new VideoSlicer(inBlob);
        try {
            parts = vs.slice("" + duration);
        } catch (IOException | CommandNotAvailable e) {
            throw new NuxeoException(e);
        }

        return parts;
    }

}

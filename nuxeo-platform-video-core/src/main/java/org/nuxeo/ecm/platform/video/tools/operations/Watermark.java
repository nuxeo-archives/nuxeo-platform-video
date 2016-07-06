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

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.video.tools.VideoWatermarker;

/**
 * Watermark a Video with the given Picture, at the given position.
 * @since 8.4
 */
@Operation(id = Watermark.ID, category = Constants.CAT_CONVERSION, label = "Video: Watermark with Picture", description = "Watermark the video with the picture stored in file:content of pictureDoc. x-y: Position of the left-top corner of the picture.")
public class Watermark {

    public static final String ID = "Video.WatermarkWithPicture";

    @Param(name = "pictureDoc", required = true)
    protected DocumentModel pictureDoc;

    @Param(name = "x", required = false)
    protected String x;

    @Param(name = "y", required = false)
    protected String y;

    @Param(name = "resultFileName", required = false)
    protected String resultFileName;

    @OperationMethod
    public Blob run(Blob inBlob) throws NuxeoException, IOException, CommandNotAvailable {

        Blob result = null;

        VideoWatermarker vw = new VideoWatermarker(inBlob);
        
        Blob picture = (Blob) pictureDoc.getPropertyValue("file:content");
        if(StringUtils.isBlank(x)) {
            x = "0";
        }
        if(StringUtils.isBlank(y)) {
            y = "0";
        }
        result = vw.watermarkWithPicture(resultFileName, picture, x, y);

        return result;
    }

}

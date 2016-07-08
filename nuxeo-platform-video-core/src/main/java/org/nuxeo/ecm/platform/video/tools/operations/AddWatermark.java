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
package org.nuxeo.ecm.platform.video.tools.operations;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.video.tools.VideoWatermarker;

/**
 * Watermark a Video with the given Picture, at the given position (from top-left).
 *
 * @since 8.4
 */
@Operation(id = AddWatermark.ID, category = Constants.CAT_CONVERSION, label = "Watermarks a Video with a Picture", description = "Watermark the video with the picture stored in file:content of watermark, at the position(x, y) from the left-top corner of the picture.", aliases = {"Video.AddWatermark"})
public class AddWatermark {

    public static final String ID = "Video.AddWatermark";

    @Param(name = "watermark", required = true)
    protected DocumentModel watermark;

    @Param(name = "x", required = false)
    protected String x = "0";

    @Param(name = "y", required = false)
    protected String y = "0";

    @Param(name = "outputFilename", required = false)
    protected String outputFilename;

    @Param(name = "xpath", required = false, values = { "file:content" })
    protected String xpath;

    @OperationMethod
    public Blob run(DocumentModel input) throws OperationException {
        String blobPath = (!StringUtils.isEmpty(xpath))? xpath : "file:content";
        return run((Blob) input.getPropertyValue(blobPath));
    }

    @OperationMethod
    public BlobList run(DocumentModelList input) throws OperationException {
        BlobList blobList = new BlobList();
        String blobPath = (!StringUtils.isEmpty(xpath))? xpath : "file:content";
        for (DocumentModel doc : input) {
            blobList.add(run((Blob) doc.getPropertyValue(blobPath)));
        }
        return blobList;
    }

    @OperationMethod(collector = BlobCollector.class)
    public Blob run(Blob input) throws OperationException {
        Blob watermarkBlob = (Blob) watermark.getPropertyValue("file:content");
        try {
            return new VideoWatermarker().watermark(input, outputFilename, watermarkBlob, x, y);
        } catch (IOException e) {
            throw new OperationException("Cannot add the watermark to the video. " + e.getMessage());
        } catch (CommandNotAvailable e) {
            throw new OperationException("The watermark command is not available. " + e.getMessage());
        }
    }
}

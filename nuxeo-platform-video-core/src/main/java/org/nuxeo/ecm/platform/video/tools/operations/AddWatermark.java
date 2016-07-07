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
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.video.tools.VideoWatermarker;

/**
 * Watermark a Video with the given Picture, at the given position.
 *
 * @since 8.4
 */
@Operation(id = AddWatermark.ID, category = Constants.CAT_CONVERSION, label = "Watermarks a Video with a Picture", description = "Watermark the video with the picture stored in file:content of pictureDoc, at the position(x, y) from the left-top corner of the picture.", aliases = {"Video.AddWatermark"})
public class AddWatermark {

    public static final String ID = "Video.AddWatermark";

    @Param(name = "pictureDoc", required = true)
    protected DocumentModel pictureDoc;

    @Param(name = "x", required = false)
    protected String x = "0";

    @Param(name = "y", required = false)
    protected String y = "0";

    @Param(name = "outputFilename", required = false)
    protected String outputFilename;

    @Param(name = "xpath", required = false, values = { "file:content" })
    protected String xpath;

    @OperationMethod(collector = DocumentModelCollector.class)
    public Blob run(DocumentModel input) throws IOException, CommandNotAvailable {
        String blobPath = (!StringUtils.isEmpty(xpath))? xpath : "file:content";
        return run((Blob) input.getPropertyValue(blobPath));
    }

    @OperationMethod(collector = BlobCollector.class)
    public Blob run(Blob input) throws NuxeoException, IOException, CommandNotAvailable {
        Blob watermark = (Blob) pictureDoc.getPropertyValue("file:content");
        return new VideoWatermarker().watermark(input, outputFilename, watermark, x, y);
    }
}

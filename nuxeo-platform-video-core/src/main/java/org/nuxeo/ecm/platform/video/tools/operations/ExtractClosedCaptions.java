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

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.collectors.BlobCollector;
import org.nuxeo.ecm.automation.core.collectors.DocumentModelCollector;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.video.tools.CCExtractor;
import org.nuxeo.runtime.api.Framework;

/**
 * @since 8.4
 */
@Operation(id = ExtractClosedCaptions.ID, category = Constants.CAT_CONVERSION, label = "Extracts Closed Captions from the Video.", description = "Returns a Blob containing the closed captions using <code>ccextractor</code> (see its documentation about <code>outFormat</code>). If <code>startAt</code>/<code>endAt</code> are empty, the whole movie is handled. If the input is a document, you can use <code>xpath</code> (ignored if the input is a blob)", aliases = {
        "Video.ExtractClosedCaptions" })
public class ExtractClosedCaptions {

    public static final String ID = "Video.ExtractClosedCaptions";

    @Param(name = "outFormat", required = false)
    protected String outFormat;

    @Param(name = "startAt", required = false)
    protected String startAt;

    @Param(name = "endAt", required = false)
    protected String endAt;

    @Param(name = "xpath", required = false, values = { "file:content" })
    protected String xpath;

    @OperationMethod
    public Blob run(DocumentModel input) throws OperationException {
        String blobPath = (!StringUtils.isEmpty(xpath)) ? xpath : "file:content";
        return run((Blob) input.getPropertyValue(blobPath));
    }

    @OperationMethod(collector = BlobCollector.class)
    public Blob run(Blob input) throws OperationException {
        try {
            CCExtractor cce = new CCExtractor(input, startAt, endAt);
            Blob result = cce.extractCC(outFormat);

            if (result == null) {
                File tempFile = Framework.createTempFile("NxVT-", "txt");
                tempFile.deleteOnExit();
                Framework.trackFile(tempFile, this);
                result = new FileBlob(tempFile);
                result.setMimeType("text/plain");
                result.setFilename(input.getFilename() + "-noCC.txt");
            }
            return result;
        } catch (IOException e) {
            throw new OperationException("Cannot extract closed captions from blob. " + e.getMessage());
        } catch (CommandNotAvailable e) {
            throw new OperationException(
                    "Cannot extract closed captions because the command is not available. " + e.getMessage());
        }
    }

}

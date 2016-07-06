/*
 * (C) Copyright ${year} Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     thibaud
 */

package org.nuxeo.ecm.platform.video.tools.operations;

import java.io.File;
import java.io.IOException;

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.video.tools.CCExtractor;
import org.nuxeo.runtime.api.Framework;

/**
 * Returns a Blob containing the closed captions using <code>ccextractor</code>
 * (see its documentation about <code>outFormat</code>). If <code>startAt</code>
 * /<code>endAt</code> are empty, the whole movie is handled. If the input is a
 * document, you can use <code>xpath</code> (ignored if the input is a blob)
 */
@Operation(id = ExtractClosedCaptions.ID, category = Constants.CAT_CONVERSION, label = "Video: Extract Closed Captions", description = "Returns a Blob containing the closed captions using <code>ccextractor</code> (see its documentation about <code>outFormat</code>). If <code>startAt</code>/<code>endAt</code> are empty, the whole movie is handled. If the input is a document, you can use <code>xpath</code> (ignored if the input is a blob)")
public class ExtractClosedCaptions {

    public static final String ID = "Video.ExtractClosedCaptions";

    @Param(name = "outFormat", required = false)
    protected String outFormat;

    @Param(name = "startAt", required = false)
    protected String startAt;

    @Param(name = "endAt", required = false)
    protected String endAt;

    @Param(name = "neverReturnNull", required = false, values = { "false" })
    protected boolean neverReturnNull;

    @Param(name = "xpath", required = false, values = { "file:content" })
    protected String xpath;

    @OperationMethod
    public Blob run(DocumentModel inDoc) throws IOException,
            CommandNotAvailable {

        return run((Blob) inDoc.getPropertyValue("file:content"));

    }

    @OperationMethod
    public Blob run(Blob inBlob) throws IOException, CommandNotAvailable {

        Blob result = null;

        CCExtractor cce = new CCExtractor(inBlob, startAt, endAt);
        result = cce.extractCC(outFormat);

        if (result == null && neverReturnNull) {
            File tempFile = File.createTempFile("NxVT-", "txt");
            tempFile.deleteOnExit();
            Framework.trackFile(tempFile, this);
            FileBlob fb = new FileBlob(tempFile);
            fb.setMimeType("text/plain");
            fb.setFilename(inBlob.getFilename() + "-noCC.txt");
            return fb;
        }

        return result;
    }

}

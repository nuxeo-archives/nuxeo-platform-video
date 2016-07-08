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
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;
import org.nuxeo.ecm.platform.video.tools.VideoConverter;

import java.io.IOException;

/**
 * @since 8.4
 */
@Operation(id = Convert.ID, category = Constants.CAT_CONVERSION, label = "Converts a Video using a video converter.", description = "Uses a video converter (declared in an XML extension) to transcode the video using a new height. Use either <code>height</code> <i>or</i> <code>scale</scale>. If both are > 0, the operation uses <code>height</code>. If the height is <= 0, then the video is just transcoded (not resized).", aliases = { "Video.Convert" })
public class Convert {

    public static final String ID = "Video.Convert";

    @Param(name = "height", required = false)
    protected long height;

    @Param(name = "scale", required = false)
    protected String scale;

    @Param(name = "converter", required = false)
    protected String converter;

    @Param(name = "xpath", required = false, values = { "file:content" })
    protected String xpath;

    @OperationMethod
    public Blob run(DocumentModel input) throws OperationException {
        String blobPath = (!StringUtils.isEmpty(xpath))? xpath : "file:content";
        return run((Blob) input.getPropertyValue(blobPath));
    }

    @OperationMethod(collector = BlobCollector.class)
    public Blob run(Blob inBlob) throws OperationException {
        VideoConverter videoConverter = new VideoConverter(inBlob);
        if (height > 0) {
            return videoConverter.convert(height, converter);
        } else {
            return videoConverter.convert(Double.parseDouble(scale), converter);
        }
    }

}

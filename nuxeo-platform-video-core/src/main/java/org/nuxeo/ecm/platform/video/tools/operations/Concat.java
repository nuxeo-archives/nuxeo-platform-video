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

import org.nuxeo.ecm.automation.core.Constants;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;

import org.nuxeo.ecm.platform.video.tools.FFMpegVideoConcat;

/**
 * Merge 2-n videos in one using ffmpeg demuxer. Please, see to ffmpeg documentation about efficiency, timestamps, ...
 * For example, mixing formats, sizes, frame rates ... will not create a nice final video. If
 * <code>resultFilename</code> is not used, the operation uses the first file and adds "-concat" (then the file
 * extension)
 *
 * @since 8.4
 */
@Operation(id = Concat.ID, category = Constants.CAT_CONVERSION, label = "Video concat using the ffmpeg demuxer.", description = "Merge 2-n videos in one using ffmpeg demuxer. Please, see to ffmpeg documentation about efficiency, timestamps, ... For example, mixing formats, sizes, frame rates ... will not create a nice final video. f <code>resultFilename</code> is not used, the operation uses the first file and adds -concat (then the file extension)", aliases = {
        "Video.Concat" })
public class Concat {

    public static final String ID = "Video.Concat";

    @Param(name = "resultFilename", required = false)
    protected String resultFilename;

    @OperationMethod
    public Blob run(BlobList inBlobs) throws NuxeoException, IOException, CommandNotAvailable {
        return new FFMpegVideoConcat().concat(inBlobs, resultFilename);
    }

    @OperationMethod
    public Blob run(DocumentModelList inDocs) throws NuxeoException, IOException, CommandNotAvailable {
        BlobList blobs = new BlobList();
        for (DocumentModel doc : inDocs) {
            blobs.add((Blob) doc.getPropertyValue("file:content"));
        }
        return run(blobs);
    }
}

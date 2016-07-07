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
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.platform.commandline.executor.api.CommandNotAvailable;

import jdk.nashorn.internal.objects.NativeArray;
import org.nuxeo.ecm.platform.video.tools.VideoConcatDemuxer;

/**
 * Merge 2-n videos in one using ffmpeg demuxer. Please, see to ffmpeg documentation about efficiency, timestamps, ...
 * For example, mixing formats, sizes, frame rates ... will not create a nice final video. If
 * <code>resultFileName</code> is not used, the operation uses the first file and adds "-concat" (then the file
 * extension)
 *
 * @since 8.4
 */
@Operation(id = Concat.ID, category = Constants.CAT_CONVERSION, label = "Video: Concat (ffmpeg demuxer)", description = "Merge 2-n videos in one using ffmpeg demuxer. Please, see to ffmpeg documentation about efficiency, timestamps, ... For example, mixing formats, sizes, frame rates ... will not create a nice final video. f <code>resultFileName</code> is not used, the operation uses the first file and adds -concat (then the file extension)")
public class Concat {

    public static final String ID = "Video.ConcatWithFfmpegDemuxer";

    @Param(name = "resultFileName", required = false)
    protected String resultFileName;

    @OperationMethod
    public Blob run(BlobList inBlobs) throws NuxeoException, IOException, CommandNotAvailable {

        Blob result = null;

        VideoConcatDemuxer vc = new VideoConcatDemuxer();
        vc.addBlobs(inBlobs);
        result = vc.concat(resultFileName);

        return result;
    }

    @OperationMethod
    public Blob run(DocumentModelList inDocs) throws NuxeoException, IOException, CommandNotAvailable {

        Blob result = null;

        VideoConcatDemuxer vc = new VideoConcatDemuxer();
        for (DocumentModel doc : inDocs) {
            vc.addBlob((Blob) doc.getPropertyValue("file:content"));
        }

        result = vc.concat(resultFileName);

        return result;
    }

    // To be called from Scripting Automation, with a JS array containing DocumentModels
    // This is kind of a workaround, waiting for 7.4 or higher to correctly convert
    // JS arrays.
    @OperationMethod
    public Blob run(NativeArray inDocs) throws NuxeoException, IOException, CommandNotAvailable {

        DocumentModelListImpl dml = new DocumentModelListImpl();
        for (int i = 0; i < inDocs.size(); ++i) {
            dml.add((DocumentModel) inDocs.get(i));
        }

        return run(dml);
    }

}

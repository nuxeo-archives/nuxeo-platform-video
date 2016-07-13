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
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.platform.video.tools.VideoToolsService;
import org.nuxeo.runtime.api.Framework;

/**
 * Slices the video to obtain a part of it.
 * @since 8.4
 */
@Operation(id = SliceVideo.ID, category = Constants.CAT_CONVERSION, label = "SliceVideo the ivdeo for a given duration and startAt time.", description = "SliceVideo the input blob starting at startAt, for a certain duration. A specific converter can be used.", aliases = {
        "Video.Slice" })
public class SliceVideo {

    public static final String ID = "Video.Slice";

    @Param(name = "startAt", required = false)
    protected String startAt;

    @Param(name = "duration", required = false)
    protected String duration;

    @Param(name = "encode", required = false)
    protected boolean encode = true;

    @Param(name = "xpath", required = false, values = { "file:content" })
    protected String xpath;

    @OperationMethod
    public Blob run(DocumentModel input) throws OperationException {
        String blobPath = (!StringUtils.isEmpty(xpath)) ? xpath : "file:content";
        return run((Blob) input.getPropertyValue(blobPath));
    }

    @OperationMethod
    public BlobList run(DocumentModelList input) throws OperationException {
        BlobList blobList = new BlobList();
        for (DocumentModel doc : input) {
            blobList.add(run(doc));
        }
        return blobList;
    }

    @OperationMethod(collector = BlobCollector.class)
    public Blob run(Blob input) throws OperationException {
        try {
            VideoToolsService videoService = Framework.getService(VideoToolsService.class);
            return videoService.slice(input, startAt, duration, encode);
        } catch(NuxeoException e) {
            throw new OperationException(e.getMessage());
        }
    }

}

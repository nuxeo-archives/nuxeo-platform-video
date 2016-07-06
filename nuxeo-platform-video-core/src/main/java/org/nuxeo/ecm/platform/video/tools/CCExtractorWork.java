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

package org.nuxeo.ecm.platform.video.tools;

import static org.nuxeo.ecm.core.api.CoreSession.ALLOW_VERSION_WRITE;
import static org.nuxeo.ecm.platform.video.tools.VideoToolsConstants.CLOSED_CAPTIONS_BLOB_XPATH;
import static org.nuxeo.ecm.platform.video.tools.VideoToolsConstants.CLOSED_CAPTIONS_FILENAME_XPATH;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.ecm.platform.video.Video;
import org.nuxeo.ecm.platform.video.VideoDocument;
import org.nuxeo.runtime.api.Framework;

/**
 * This class is a "Worker", contributed in the VideoToolsWorks.xml extension.
 * <p>
 * As a worker, it runs asynchronously in a separated thread.
 * <p>
 * The work itself is quite basic: Just call <code>CCExtractor</code> and get
 * the result, if any.
 *
 * @since 7.1
 */
public class CCExtractorWork extends AbstractWork {

    private static final long serialVersionUID = 1L;

    private static final Log log = LogFactory.getLog(CCExtractorWork.class);

    public static final String CATEGORY_VIDEO_CLOSED_CAPTIONS_EXTRACTOR = "videoClosedCaptionsExtractor";

    public static final String VIDEO_EXTRACT_CLOSED_CAPTIONS_DONE_EVENT = "videoClosedCaptionsExtractionDone";

    protected static String computeIdPrefix(String repositoryName, String docId) {
        return repositoryName + ':' + docId + ":closedCaptionsExtraction:";
    }

    public CCExtractorWork(String repositoryName, String docId) {
        super(computeIdPrefix(repositoryName, docId));
        setDocument(repositoryName, docId);
    }

    @Override
    public String getCategory() {
        return CATEGORY_VIDEO_CLOSED_CAPTIONS_EXTRACTOR;
    }

    @Override
    public String getTitle() {
        return "Video Closed Captions Extraction";
    }

    @Override
    public void work() {
        
        if(!CCExtractor.ccextractorIsAvailable()) {
            setStatus("ccextractor command not available, no extraction done");
            log.warn("ccextractor command not available, no extraction done");
            return;
        }
        
        setStatus("Extracting Closed Captions");
        setProgress(Progress.PROGRESS_INDETERMINATE);

        Video originalVideo = null;
        try {
            initSession();
            originalVideo = getVideoToConvert();
            commitOrRollbackTransaction();
        } finally {
            cleanUp(true, null);
        }

        Blob result = null;
        if (originalVideo != null) {
            CCExtractor cce = new CCExtractor(originalVideo.getBlob());
            try {
                result = cce.extractCC("ttxt");
            } catch (Exception e) {
                log.error("Cannot extract the closed captions", e);
                result = null;
            }
        }
        saveDocument(result);
    }

    protected void saveDocument(Blob inClosedCaptions) {

        if (inClosedCaptions == null) {
            setStatus("No Closed Captions, no video file, or an error occured");
        } else {
            setStatus("Saving Closed Captions");
        }
        startTransaction();
        initSession();
        DocumentModel doc = session.getDocument(new IdRef(docId));
        if (inClosedCaptions != null) {
            doc.setPropertyValue(CLOSED_CAPTIONS_BLOB_XPATH,
                    (Serializable) inClosedCaptions);
            doc.setPropertyValue(CLOSED_CAPTIONS_FILENAME_XPATH,
                    inClosedCaptions.getFilename());
        } else {
            doc.setPropertyValue(CLOSED_CAPTIONS_BLOB_XPATH, null);
            doc.setPropertyValue(CLOSED_CAPTIONS_FILENAME_XPATH, null);
        }

        // It may happen the async. job is done while, in the meantime, the user
        // created a version
        if (doc.isVersion()) {
            doc.putContextData(ALLOW_VERSION_WRITE, Boolean.TRUE);
        }
        session.saveDocument(doc);
        triggerClosedCaptionsExtractionDoneEvent(doc);
        setStatus("Done");

    }

    protected Video getVideoToConvert() throws NuxeoException {
        DocumentModel doc = session.getDocument(new IdRef(docId));
        VideoDocument videoDocument = doc.getAdapter(VideoDocument.class);
        Video video = videoDocument.getVideo();
        if (video == null) {
            log.warn("No video file available for: " + doc);
        }
        return video;
    }

    protected void triggerClosedCaptionsExtractionDoneEvent(DocumentModel doc) {
        WorkManager workManager = Framework.getLocalService(WorkManager.class);
        List<String> workIds = workManager.listWorkIds(
                CATEGORY_VIDEO_CLOSED_CAPTIONS_EXTRACTOR, null);
        String idPrefix = computeIdPrefix(repositoryName, docId);
        for (String workId : workIds) {
            if (workId.startsWith(idPrefix)) {
                // At least another work scheduled
                return;
            }
        }

        DocumentEventContext ctx = new DocumentEventContext(session,
                session.getPrincipal(), doc);
        Event event = ctx.newEvent(VIDEO_EXTRACT_CLOSED_CAPTIONS_DONE_EVENT);
        Framework.getLocalService(EventService.class).fireEvent(event);
    }

}

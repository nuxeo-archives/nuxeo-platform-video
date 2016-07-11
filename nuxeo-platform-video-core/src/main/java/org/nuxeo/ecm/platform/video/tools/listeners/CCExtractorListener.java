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
package org.nuxeo.ecm.platform.video.tools.listeners;

import static org.nuxeo.ecm.platform.video.VideoConstants.VIDEO_CHANGED_EVENT;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.PostCommitFilteringEventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.ecm.core.work.api.WorkManager.Scheduling;
import org.nuxeo.ecm.platform.video.tools.FFMpegCCExtractorWork;
import org.nuxeo.ecm.platform.video.tools.FFMpegCCExtractor;
import org.nuxeo.runtime.api.Framework;

/**
 * This is a postcommit, async. event. So, the Document is already saved and
 * committed, we just schedule the work on it. This class handles the
 * "videoChanged" event triggered by nuxeo.
 * 
 * Unfortunately, it is not triggered when the video file is removed from the
 * document, we must handle this in a dedicated event
 * 
 */
public class CCExtractorListener implements PostCommitFilteringEventListener {

    public static final Log log = LogFactory.getLog(CCExtractorListener.class);

    @Override
    public void handleEvent(EventBundle events) throws NuxeoException {
        for (Event event : events) {
            if (VIDEO_CHANGED_EVENT.equals(event.getName())) {
                handleEvent(event);
            }
        }
    }

    public void handleEvent(Event event) throws NuxeoException {
        
        if(!FFMpegCCExtractor.ccextractorIsAvailable()) {
            return;
        }
        
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }
        
        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();

        FFMpegCCExtractorWork work = new FFMpegCCExtractorWork(doc.getRepositoryName(),
                doc.getId());

        WorkManager workManager = Framework.getLocalService(WorkManager.class);
        workManager.schedule(work, Scheduling.IF_NOT_RUNNING_OR_SCHEDULED);

    }

    @Override
    public boolean acceptEvent(Event event) {
        return VIDEO_CHANGED_EVENT.equals(event.getName());
    }

}

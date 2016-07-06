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
        
        if(!CCExtractor.ccextractorIsAvailable()) {
            return;
        }
        
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }
        
        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();

        CCExtractorWork work = new CCExtractorWork(doc.getRepositoryName(),
                doc.getId());

        WorkManager workManager = Framework.getLocalService(WorkManager.class);
        workManager.schedule(work, Scheduling.IF_NOT_RUNNING_OR_SCHEDULED);

    }

    @Override
    public boolean acceptEvent(Event event) {
        return VIDEO_CHANGED_EVENT.equals(event.getName());
    }

}

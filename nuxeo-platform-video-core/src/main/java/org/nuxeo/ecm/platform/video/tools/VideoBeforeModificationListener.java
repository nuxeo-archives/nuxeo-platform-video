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

import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.ABOUT_TO_CREATE;
import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.BEFORE_DOC_UPDATE;
import static org.nuxeo.ecm.platform.video.tools.VideoToolsConstants.CLOSED_CAPTIONS_BLOB_XPATH;
import static org.nuxeo.ecm.platform.video.tools.VideoToolsConstants.CLOSED_CAPTIONS_FILENAME_XPATH;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.video.VideoConstants;

/**
 * Extraction of ClosedCaptions is done in another listener (listening to
 * "videoChanged"). Here, we just cleanup everything if the current document has
 * no video at all
 * 
 */
public class VideoBeforeModificationListener implements EventListener {

    @Override
    public void handleEvent(Event event) throws NuxeoException {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }

        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();
        if (ABOUT_TO_CREATE.equals(event.getName())
                || BEFORE_DOC_UPDATE.equals(event.getName())) {
            if (doc.hasFacet(VideoConstants.VIDEO_FACET)
                    && doc.getPropertyValue("file:content") == null) {
                doc.setPropertyValue(CLOSED_CAPTIONS_BLOB_XPATH, null);
                doc.setPropertyValue(CLOSED_CAPTIONS_FILENAME_XPATH, null);
                // No save here because we are in "beforeModification" or
                // "aboutToCreate" => nuxeo will save the doc later
            }
        }
    }
}

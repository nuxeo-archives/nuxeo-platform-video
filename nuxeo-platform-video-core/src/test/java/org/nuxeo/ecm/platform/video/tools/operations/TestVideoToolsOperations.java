/*
 * (C) Copyright 2016 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Ricardo Dias
 */

package org.nuxeo.ecm.platform.video.tools.operations;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.event.EventService;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;

@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class })
@Deploy({ "org.nuxeo.ecm.platform.commandline.executor", "org.nuxeo.ecm.platform.video.convert",
        "org.nuxeo.ecm.platform.picture.core" })
public class TestVideoToolsOperations {

    protected static final Log log = LogFactory.getLog(TestVideoToolsOperations.class);

    protected DocumentModel folder;

    @Inject
    CoreSession coreSession;

    @Inject
    AutomationService automationService;

    @Inject
    EventService eventService;

    protected DocumentModel createVideoDocumentFromFile(File inFile) {
        DocumentModel videoDoc = coreSession.createDocumentModel(folder.getPathAsString(), inFile.getName(), "Video");
        videoDoc.setPropertyValue("dc:title", inFile.getName());
        videoDoc.setPropertyValue("file:content", new FileBlob(inFile));
        videoDoc = coreSession.createDocument(videoDoc);
        videoDoc = coreSession.saveDocument(videoDoc);
        coreSession.save();
        return videoDoc;
    }

    @Before
    public void setUp() {
        folder = coreSession.createDocumentModel("/", "TestVideoTools", "Folder");
        folder.setPropertyValue("dc:title", "TestVideoTools");
        folder = coreSession.createDocument(folder);
        folder = coreSession.saveDocument(folder);
        coreSession.save();
    }

    @After
    public void cleanup() {
        coreSession.removeDocument(folder.getRef());
    }

    @Test
    public void testAddWatermark() {


    }
}

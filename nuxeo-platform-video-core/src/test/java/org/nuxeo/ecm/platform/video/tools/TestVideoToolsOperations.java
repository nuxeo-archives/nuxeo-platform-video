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
 *     Ricardo Dias
 */
package org.nuxeo.ecm.platform.video.tools;

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
@Deploy({ "org.nuxeo.ecm.automation.core", "org.nuxeo.ecm.platform.commandline.executor",
        "org.nuxeo.ecm.platform.video.convert", "org.nuxeo.ecm.platform.picture.core" })
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

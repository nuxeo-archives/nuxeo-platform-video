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
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.common.utils.FileUtils;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.util.BlobList;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.ecm.platform.video.VideoHelper;
import org.nuxeo.ecm.platform.video.VideoInfo;
import org.nuxeo.ecm.platform.video.tools.operations.AddWatermark;
import org.nuxeo.ecm.platform.video.tools.operations.Concat;
import org.nuxeo.ecm.platform.video.tools.operations.ExtractClosedCaptions;
import org.nuxeo.ecm.platform.video.tools.operations.Slice;
import org.nuxeo.ecm.platform.video.tools.operations.SliceInParts;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

import com.google.inject.Inject;
import org.nuxeo.runtime.test.runner.LocalDeploy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @since 8.4
 */
@RunWith(FeaturesRunner.class)
@Features({ PlatformFeature.class })
@Deploy({ "org.nuxeo.ecm.automation.core", "org.nuxeo.ecm.platform.commandline.executor",
        "org.nuxeo.ecm.platform.video.convert", "org.nuxeo.ecm.platform.picture.core" })
@LocalDeploy({ "org.nuxeo.ecm.platform.video.core:OSGI-INF/core-types-contrib.xml",
        "org.nuxeo.ecm.platform.video.core:OSGI-INF/video-tools-operations-contrib.xml",
        "org.nuxeo.ecm.platform.video.core:OSGI-INF/video-tools-commandlines-contrib.xml",
        "org.nuxeo.ecm.platform.video.core:OSGI-INF/video-tools-service.xml",
        "org.nuxeo.ecm.platform.video.core:OSGI-INF/video-tools-default-contrib.xml" })
public class TestVideoToolsOperations extends BaseVideoToolsTest {

    public static final String WATERMARK_PICTURE = "test-data/logo.jpeg";

    protected DocumentModel folder;

    @Inject
    CoreSession coreSession;

    @Inject
    AutomationService service;

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
    public void testAddWatermarkTool() throws OperationException, IOException {
        DocumentModel videoDoc = createVideoDocumentFromBlob(getTestVideo(TEST_VIDEO_SMALL));
        assertNotNull(videoDoc);

        DocumentModel watermarkDoc = createWatermarkDocument(FileUtils.getResourceFileFromContext(WATERMARK_PICTURE));
        assertNotNull(watermarkDoc);

        OperationContext ctx = new OperationContext(coreSession);
        ctx.setInput(videoDoc);
        OperationChain chain = new OperationChain("testAddWatermark");
        chain.add(AddWatermark.ID)
             .set("watermark", watermarkDoc)
             .set("xpath", "file:content")
             .set("x", "5")
             .set("y", "5");

        Blob watermarkedBlob = (Blob) service.run(ctx, chain);
        assertNotNull(watermarkedBlob);
    }

    @Test
    public void testConcatTool() throws IOException, OperationException {
        DocumentModel doc1 = createVideoDocumentFromBlob(getTestVideo(TEST_VIDEO_SMALL));
        DocumentModel doc2 = createVideoDocumentFromBlob(getTestVideo(TEST_VIDEO_SMALL));

        assertNotNull(doc1);
        assertNotNull(doc2);

        DocumentModelList docList = new DocumentModelListImpl(3);
        docList.add(doc1);
        docList.add(doc2);

        OperationContext ctx = new OperationContext(coreSession);
        ctx.setInput(docList);
        OperationChain chain = new OperationChain("testConcatTool");
        chain.add(Concat.ID);

        Blob resultBlob = (Blob) service.run(ctx, chain);
        assertNotNull(resultBlob);

        VideoInfo info = VideoHelper.getVideoInfo(resultBlob);
        assertEquals(info.getDuration(), 16.0, 1.0);
    }

    @Test
    public void testSliceTool() throws IOException, OperationException {
        DocumentModel doc = createVideoDocumentFromBlob(getTestVideo(TEST_VIDEO_SMALL));

        OperationContext ctx = new OperationContext(coreSession);
        ctx.setInput(doc);
        OperationChain chain = new OperationChain("testSliceTool");
        chain.add(Slice.ID).set("start", "00:02").set("duration", "00:04");

        Blob sliceVideo = (Blob) service.run(ctx, chain);

        assertNotNull(sliceVideo);
        assertTrue(sliceVideo.getLength() > 0);

        VideoInfo videoInfo = VideoHelper.getVideoInfo(sliceVideo);
        assertEquals(videoInfo.getDuration(), 4.0, 1.0);
    }

    @Test
    public void testSliceInPartsTool() throws IOException, OperationException {
        DocumentModel doc = createVideoDocumentFromBlob(getTestVideo(TEST_VIDEO_WITH_CC));

        OperationContext ctx = new OperationContext(coreSession);
        ctx.setInput(doc);
        OperationChain chain = new OperationChain("testSliceInPartsTool");
        chain.add(SliceInParts.ID).set("duration", "30");

        BlobList slices = (BlobList) service.run(ctx, chain);
        assertNotNull(slices);

        for (Blob blob : slices) {
            VideoInfo videoInfo = VideoHelper.getVideoInfo(blob);
            assertEquals(videoInfo.getDuration(), 25.0, 12.0);
        }
    }

    @Test
    public void testExtractClosedCaptions() throws IOException, OperationException {
        DocumentModel videoWithCC = createVideoDocumentFromBlob(getTestVideo(TEST_VIDEO_WITH_CC));

        OperationContext ctx = new OperationContext(coreSession);
        ctx.setInput(videoWithCC);
        OperationChain chain = new OperationChain("testExtractClosedCaptions");
        chain.add(ExtractClosedCaptions.ID).set("outFormat", "ttxt");

        Blob closedCaptions = (Blob) service.run(ctx, chain);
        assertNotNull(closedCaptions);

        assertTrue(closedCaptions instanceof FileBlob);
        String cc = fileBlobToString((FileBlob) closedCaptions);
        assertNotNull(cc);
        assertNotEquals("", cc);
    }

    @Test
    public void testExtractClosedCaptionsFromSlice() throws IOException, OperationException {
        DocumentModel videoWithCC = createVideoDocumentFromBlob(getTestVideo(TEST_VIDEO_WITH_CC));

        OperationContext ctx = new OperationContext(coreSession);
        ctx.setInput(videoWithCC);
        OperationChain chain = new OperationChain("testExtractClosedCaptions");
        chain.add(ExtractClosedCaptions.ID).set("outFormat", "ttxt").set("startAt", "00:10").set("endAt", "00:20");

        Blob closedCaptions = (Blob) service.run(ctx, chain);
        assertNotNull(closedCaptions);

        assertTrue(closedCaptions instanceof FileBlob);
        String cc = fileBlobToString((FileBlob) closedCaptions);
        assertNotNull(cc);
        assertNotEquals("", cc);
        // TODO - closed captions are not being extracted, cannot do it through command line too!
    }

    protected DocumentModel createVideoDocumentFromBlob(Blob blob) {
        DocumentModel videoDoc = coreSession.createDocumentModel(folder.getPathAsString(), blob.getFilename(), "Video");
        videoDoc.setPropertyValue("dc:title", blob.getFilename());
        videoDoc.setPropertyValue("file:content", (FileBlob) blob);
        videoDoc = coreSession.createDocument(videoDoc);
        videoDoc = coreSession.saveDocument(videoDoc);
        coreSession.save();
        return videoDoc;
    }

    protected DocumentModel createWatermarkDocument(File input) {
        DocumentModel pictureDoc = coreSession.createDocumentModel(folder.getPathAsString(), input.getName(),
                "Picture");
        pictureDoc.setPropertyValue("dc:title", input.getName());
        pictureDoc.setPropertyValue("file:content", new FileBlob(input));
        pictureDoc = coreSession.createDocument(pictureDoc);
        pictureDoc = coreSession.saveDocument(pictureDoc);
        coreSession.save();
        return pictureDoc;
    }
}

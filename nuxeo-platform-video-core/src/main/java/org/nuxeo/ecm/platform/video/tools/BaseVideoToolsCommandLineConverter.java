package org.nuxeo.ecm.platform.video.tools;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.convert.api.ConversionException;
import org.nuxeo.ecm.core.convert.cache.SimpleCachableBlobHolder;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;
import org.nuxeo.ecm.platform.convert.plugins.CommandLineBasedConverter;

/**
 * As of "today" (dec-2014) the XXXCommandLineCOnverter classes use all the same
 * methods except the one that ultimately does the job (fill the specific
 * parameters). So, instead of having the same code duplicated in all convertes,
 * lets have this umbrella class
 * <p>
 * Children must override getCmdStringParameters()
 *
 * @since 7.1
 */
public abstract class BaseVideoToolsCommandLineConverter extends
        CommandLineBasedConverter {

    @Override
    protected BlobHolder buildResult(List<String> cmdOutput,
                                     CmdParameters cmdParams) {

        String outputPath = cmdParams.getParameters().get("outDirPath").toString();
        File outputDir = new File(outputPath);
        File[] files = outputDir.listFiles();
        List<Blob> blobs = new ArrayList<Blob>();

        for (File file : files) {
            Blob blob = null;
            blob = new FileBlob(file);
            blob.setFilename(file.getName());
            blobs.add(blob);
        }

        return new SimpleCachableBlobHolder(blobs);
    }

    @Override
    protected Map<String, Blob> getCmdBlobParameters(BlobHolder blobHolder,
                                                     Map<String, Serializable> parameters) throws ConversionException {

        Map<String, Blob> cmdBlobParams = new HashMap<String, Blob>();
        try {
            cmdBlobParams.put("sourceFilePath", blobHolder.getBlob());
        } catch (NuxeoException e) {
            throw new ConversionException("Unable to get Blob for holder", e);
        }
        return cmdBlobParams;
    }
}

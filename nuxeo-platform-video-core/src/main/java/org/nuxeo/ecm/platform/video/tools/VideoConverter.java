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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.core.api.blobholder.SimpleBlobHolder;
import org.nuxeo.ecm.core.convert.api.ConversionException;
import org.nuxeo.ecm.core.convert.api.ConversionService;
import org.nuxeo.ecm.platform.video.VideoHelper;
import org.nuxeo.ecm.platform.video.VideoInfo;
import org.nuxeo.runtime.api.Framework;

/**
 * The class is a simple wrapper around <i>existing</i> Command Line Based
 * Converters. It requires only a new height (and the code of nuxeo
 * <code>BaseVideoConversionConverter</code> updates the width proportionally)
 * 
 * To keep the same dimensions, set the new height (or the scale factor) to a
 * value <= 0.
 * 
 * Typical existing converters: See nuxeo-platform-video-convert and the
 * converters/commandlines declared as XML extensions.
 * 
 * As of today (Dec-2014), available, default converters are:
 * <ul>
 * <li>convertToWebM</li>
 * <li>convertToMP4</li>
 * <li>convertToOgg</li>
 * </ul>
 * 
 * If you define you own, custom converter and you want to use this
 * VideoConverter class, then make sure to use the same approach than nuxeo
 * original code.
 *
 * @since 7.1
 */
public class VideoConverter /*extends BaseVideoTools */{

    private static final Log log = LogFactory.getLog(VideoConverter.class);

    long height = 0;

    Double scale = null;

    String converter = "";

    protected Blob blob;

    protected VideoInfo videoInfo;

    public VideoConverter(Blob blob) {
        this.blob = blob;
    }

    protected void loadVideoInfoIfNeeded() {

        if (videoInfo == null) {
            videoInfo = VideoHelper.getVideoInfo(blob);
        }
    }

    protected Blob convert() {

        Blob convertedBlob = null;

        if (blob == null) {
            return null;
        }

        if (converter == null || converter.isEmpty()) {
            throw new NuxeoException("Missing the name of a converter");
        }

        loadVideoInfoIfNeeded();
        if (height <= 0) {
            height = videoInfo.getHeight();
        }

        ConversionService conversionService = Framework.getService(ConversionService.class);
        BlobHolder source = new SimpleBlobHolder(blob);
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();

        // nuxeo default converters expect these 2 parameters
        parameters.put("height", height);
        parameters.put("videoInfo", videoInfo);

        BlobHolder result = null;
        try {
            result = conversionService.convert(converter, source, parameters);
        } catch (ConversionException e) {
            log.error("Error while converting the video", e);
            result = null;
        }

        if (result != null) {
            convertedBlob = result.getBlob();
        }

        return convertedBlob;
    }

    /**
     * Use the <code>inConverter</code> converter to transcode the video. If
     * <code>inHeight</code> is <= 0, the converted video will have the same
     * height as the original.
     * 
     * @param inHeight
     * @param inConverter
     * @return
     *
     * @since 7.1
     */
    public Blob convert(long inHeight, String inConverter) {

        height = inHeight;
        converter = inConverter;

        return convert();
    }

    /**
     * Use the <code>inConverter</code> converter to transcode the video. If
     * <code>inScale</code> is <= 0.0, the converted video will have the same
     * height as the original.
     *
     * @param inConverter
     * @return
     *
     * @since 7.1
     */
    public Blob convert(double inScale, String inConverter) {
        long newH = 0;
        if (inScale > 0) {
            loadVideoInfoIfNeeded();
            newH = (long) (((double) videoInfo.getHeight()) * inScale);
        }
        return convert(newH, inConverter);
    }

}

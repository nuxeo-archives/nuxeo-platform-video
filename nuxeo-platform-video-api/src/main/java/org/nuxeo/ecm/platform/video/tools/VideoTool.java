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

import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.blobholder.BlobHolder;
import org.nuxeo.ecm.platform.commandline.executor.api.CmdParameters;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Common interface to setup the video tools.
 *
 * @since 8.4
 */
public abstract class VideoTool {

    public abstract CmdParameters setupParameters(BlobHolder input, Map<String, Object> parameters);

    public abstract BlobHolder buildResult(BlobHolder input, CmdParameters cmdParams, List<String> cmdOutput);
}

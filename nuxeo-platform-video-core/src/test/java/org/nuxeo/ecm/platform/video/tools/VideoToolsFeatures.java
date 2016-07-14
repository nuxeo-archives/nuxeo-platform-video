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

import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.test.runner.SimpleFeature;

/**
 * @since 8.4
 */
@Features({ CoreFeature.class })
@RepositoryConfig(cleanup = Granularity.METHOD)
@Deploy({ "org.nuxeo.ecm.automation.core", "org.nuxeo.ecm.platform.commandline.executor",
        "org.nuxeo.ecm.platform.video.convert", "org.nuxeo.ecm.platform.picture.core" })
@LocalDeploy({ "org.nuxeo.ecm.platform.video.core:OSGI-INF/core-types-contrib.xml",
        "org.nuxeo.ecm.platform.video.core:OSGI-INF/video-tools-commandlines-contrib.xml",
        "org.nuxeo.ecm.platform.video.core:OSGI-INF/video-tools-operations-contrib.xml",
        "org.nuxeo.ecm.platform.video.core:OSGI-INF/video-tools-service.xml" })
public class VideoToolsFeatures extends SimpleFeature {
}
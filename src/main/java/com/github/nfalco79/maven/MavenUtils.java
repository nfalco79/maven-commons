/*
 * Copyright 2022 Falco Nikolas
 * Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.github.nfalco79.maven;

import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;

public final class MavenUtils {

    private MavenUtils() {
    }

    public static DefaultArtifact buildArtifact(String groupId, String artifacId, String version) {
        return new DefaultArtifact(groupId, artifacId, version, "compile", "jar", null, new DefaultArtifactHandler("jar"));
    }

    public static DefaultArtifact buildArtifact(String groupId, String artifacId, String version, String scope) {
        return new DefaultArtifact(groupId, artifacId, version, scope, "jar", null, new DefaultArtifactHandler("jar"));
    }

}

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
package com.github.nfalco79.maven.artifact.resolver.filter;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;

/**
 * An implementation of inclusion filter which includes artifacts that matches
 * the given type.
 */
public class TypeFiler implements ArtifactFilter {

    private String type;

    /**
     * Default constructor.
     *
     * @param type
     *            accept artifact of this type
     */
    public TypeFiler(String type) {
        this.type = type;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.maven.artifact.resolver.filter.ArtifactFilter#include(org.apache.maven.artifact.Artifact)
     */
    @Override
    public boolean include(Artifact artifact) {
        return artifact != null && type.equals(artifact.getType());
    }

}

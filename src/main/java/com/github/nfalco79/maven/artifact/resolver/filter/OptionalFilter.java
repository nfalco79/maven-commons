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
 * An implementation of inclusion filter which includes optional artifacts based on the value of a boolean parameter.
 */
public class OptionalFilter implements ArtifactFilter {

    private Boolean includeOptional;

    /**
     * The constructor of the inclusion filter which includes optional artifacts based on the value of a boolean parameter.
     * 
     * @param includeOptional
     *            the boolean parameter. If true, optional artifacts will be included
     */
    public OptionalFilter(boolean includeOptional) {
        this.includeOptional = includeOptional;
    }

    public boolean include(Artifact artifact) {
        return includeOptional ? true : !artifact.isOptional();
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 31 + includeOptional.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OptionalFilter)) {
            return false;
        }

        OptionalFilter other = (OptionalFilter) obj;

        return includeOptional.equals(other.includeOptional);
    }

}

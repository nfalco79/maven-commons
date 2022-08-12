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
package com.github.nfalco79.maven.artifact.filter.collection;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.shared.artifact.filter.PatternExcludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.collection.AbstractArtifactsFilter;
import org.apache.maven.shared.artifact.filter.collection.ArtifactFilterException;

/**
 * An implementation of an exclusion filter based on a collection of exclusion
 * patterns.
 */
public class ExcludeArtifactFilter extends AbstractArtifactsFilter {

    private final Collection<String> patterns;

    /**
     * The constructor of the exclusion filter based on a collection of
     * exclusion patterns.
     *
     * @param patterns
     *            the collection of exclusion patterns
     */
    public ExcludeArtifactFilter(Collection<String> patterns) {
        this.patterns = patterns; // NOSONAR
    }

    @Override
    public Set<Artifact> filter(Set<Artifact> artifacts) throws ArtifactFilterException {
        if (!patterns.isEmpty()) {
            PatternExcludesArtifactFilter excludeFilter = new PatternExcludesArtifactFilter(patterns);
            return artifacts.stream().filter(a -> excludeFilter.include(a)).collect(Collectors.toSet());
        }

        return artifacts;
    }

}
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
package com.github.nfalco79.maven.dependency.graph;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor;
import org.eclipse.aether.graph.DependencyVisitor;

/**
 * Custom implementation of {@link DependencyNodeVisitor} that allow to navigate
 * a dependency tree and store artifact dependencies in a reverse order, from
 * leaf to root.
 */
public class BottomUpDependencyVisitor implements DependencyNodeVisitor, DependencyVisitor {

    private class ArtifactKey {
        private String groupId;
        private String artifactId;

        public ArtifactKey(Artifact artifact) {
            groupId = artifact.getGroupId();
            artifactId = artifact.getArtifactId();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((artifactId == null) ? 0 : artifactId.hashCode());
            result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) { // NOSONAR generated
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            ArtifactKey other = (ArtifactKey) obj;
            if (artifactId == null) {
                if (other.artifactId != null) {
                    return false;
                }
            } else if (!artifactId.equals(other.artifactId)) {
                return false;
            }
            if (groupId == null) {
                if (other.groupId != null) {
                    return false;
                }
            } else if (!groupId.equals(other.groupId)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return groupId + ':' + artifactId;
        }
    }

    private Map<ArtifactKey, Artifact> artifacts = new LinkedHashMap<>();

    @Override
    public boolean visit(DependencyNode node) {
        return true;
    }

    @Override
    public boolean endVisit(DependencyNode node) {
        store(node.getArtifact());
        return true;
    }

    private void store(Artifact nodeArtifact) {
        ArtifactKey key = new ArtifactKey(nodeArtifact);
        if (!artifacts.containsKey(key)) {
            artifacts.put(key, nodeArtifact);
        } else {
            ArtifactVersion nodeVersion = new DefaultArtifactVersion(nodeArtifact.getVersion());
            ArtifactVersion storedVersion = new DefaultArtifactVersion(artifacts.get(key).getVersion());
            if (storedVersion.compareTo(nodeVersion) < 0) {
                artifacts.put(key, nodeArtifact);
            }
        }
    }

    public Collection<Artifact> getNodes() {
        return Collections.unmodifiableCollection(artifacts.values());
    }

    @Override
    public boolean visitEnter(org.eclipse.aether.graph.DependencyNode node) {
        return true;
    }

    @Override
    public boolean visitLeave(org.eclipse.aether.graph.DependencyNode node) {
        store(RepositoryUtils.toArtifact(node.getArtifact()));
        return true;
    }

}
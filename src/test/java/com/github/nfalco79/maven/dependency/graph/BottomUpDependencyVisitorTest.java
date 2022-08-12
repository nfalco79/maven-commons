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

import java.util.Arrays;
import java.util.Collections;

import org.apache.maven.shared.dependency.graph.internal.DefaultDependencyNode;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.github.nfalco79.maven.MavenUtils;

public class BottomUpDependencyVisitorTest {
    @Test
    public void verify_which_version_was_keept_by_visitor() {
        DefaultDependencyNode root = new DefaultDependencyNode(null, MavenUtils.buildArtifact("g1", "root", "1.0"), null, null, null);
        DefaultDependencyNode childLeft = new DefaultDependencyNode(root, MavenUtils.buildArtifact("g1", "child1", "1.0"), null, null, null);
        DefaultDependencyNode childRight = new DefaultDependencyNode(root, MavenUtils.buildArtifact("g1", "child2", "1.0"), null, null, null);
        root.setChildren(Arrays.asList(childLeft, childRight));
        DefaultDependencyNode leaf1 = new DefaultDependencyNode(childLeft, MavenUtils.buildArtifact("g1", "a1", "1.0"), null, null, null);
        leaf1.setChildren(Collections.emptyList());
        childLeft.setChildren(Arrays.asList(leaf1));
        DefaultDependencyNode leaf2 = new DefaultDependencyNode(childRight, MavenUtils.buildArtifact("g1", "a1", "2.0"), null, null, null);
        leaf2.setChildren(Collections.emptyList());
        childRight.setChildren(Arrays.asList(leaf2));

        BottomUpDependencyVisitor visitor = new BottomUpDependencyVisitor();
        root.accept(visitor);
        Assertions.assertThat(visitor.getNodes()).contains(leaf2.getArtifact()).doesNotContain(leaf1.getArtifact());
    }

}
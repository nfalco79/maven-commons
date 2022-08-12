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
package com.github.nfalco79.maven.dependency;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.dependency.graph.internal.DefaultDependencyNode;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.github.nfalco79.maven.MavenUtils;
import com.github.nfalco79.maven.artifact.resolver.filter.TypeFiler;

public class DependencyResolverTest {

    @Rule
    public TemporaryFolder fileRule = new TemporaryFolder();

    @Test
    public void test_resolve_dependencies() throws Exception {
        MavenProject mavenProject = buildMavenProject("g", "a", "1");

        Artifact artifact = buildArtifactAndJAR("com.acme", "a1", "1.0");

        DefaultDependencyNode rootNode = new DefaultDependencyNode(null, buildArtifactAndJAR("org.acme", "core", "1.0"), null, null, null);
        rootNode.setChildren(Arrays.asList(new DefaultDependencyNode(rootNode, artifact, null, null, null)));
        rootNode.getChildren().forEach(n -> ((DefaultDependencyNode) n).setChildren(Collections.emptyList()));

        DependencyResolver resolver = new DependencyResolver(buildMavenSession(mavenProject), mavenProject, buildGraphBuilder(rootNode), new TypeFiler("jar"),
                mock(Log.class));

        int maxAttempts = 1;
        DependencyNode resultNode = resolver.resolveDependencies(maxAttempts);
        Assertions.assertThat(resultNode).isEqualTo(rootNode);
    }

    @Test
    public void test_maxAttempts() throws Exception {
        MavenProject mavenProject = buildMavenProject("g", "a", "1");

        File artifactFolder = fileRule.newFolder();
        DefaultArtifact artifact = MavenUtils.buildArtifact("com.acme", "a1", "1.0");
        // create empty jar file
        artifact.setFile(File.createTempFile("com.acme", ".jar", artifactFolder));

        DefaultDependencyNode rootNode = new DefaultDependencyNode(null, buildArtifactAndJAR("org.acme", "core", "1.0"), null, null, null);
        rootNode.setChildren(Arrays.asList(new DefaultDependencyNode(rootNode, artifact, null, null, null)));
        rootNode.getChildren().forEach(n -> ((DefaultDependencyNode) n).setChildren(Collections.emptyList()));

        DependencyResolver resolver = spy(
                new DependencyResolver(buildMavenSession(mavenProject), mavenProject, buildGraphBuilder(rootNode), new TypeFiler("jar"), mock(Log.class)));
        doNothing().when(resolver).removeResolvedArtifact(artifact);

        int maxAttempts = 2;
        Assertions.assertThatThrownBy(() -> resolver.resolveDependencies(maxAttempts))
                .hasMessage("Fail to download artifact " + artifact.toString() + ", size is 0");
        verify(resolver, times(maxAttempts)).removeResolvedArtifact(artifact);
    }

    private File buildJar(Artifact artifact) throws IOException {
        File file = fileRule.newFile();
        String logicaFilePath = (artifact.getGroupId() + "/" + artifact.getArtifactId()).replace('.', '/');
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
            zos.putNextEntry(new ZipEntry(logicaFilePath + "/db.changelog.xml"));
            try (InputStream is = getClass().getResourceAsStream("merge/changelog_" + artifact.getArtifactId() + ".xml")) {
                if (is != null) {
                    IOUtils.copy(is, zos);
                }
            }
        }
        return file;
    }

    private MavenProject buildMavenProject(String groupId, String artifactId, String version) {
        MavenProject mavenProject = new MavenProject();
        mavenProject.setGroupId(groupId);
        mavenProject.setArtifactId(artifactId);
        mavenProject.setVersion(version);
        mavenProject.setPackaging("jar");
        return mavenProject;
    }

    private MavenSession buildMavenSession(MavenProject mavenProject) {
        MavenSession mavenSession = mock(MavenSession.class);
        when(mavenSession.getCurrentProject()).thenReturn(mavenProject);
        when(mavenSession.getProjectBuildingRequest()).thenReturn(mock(ProjectBuildingRequest.class));
        when(mavenSession.getProjects()).thenReturn(Collections.emptyList());
        return mavenSession;
    }

    private DependencyGraphBuilder buildGraphBuilder(DefaultDependencyNode rootNode) throws Exception {
        DependencyGraphBuilder graphBuilder = mock(DependencyGraphBuilder.class);
        when(graphBuilder.buildDependencyGraph(any(ProjectBuildingRequest.class), any(ArtifactFilter.class))) //
                .thenReturn(rootNode);
        return graphBuilder;
    }

    private Artifact buildArtifactAndJAR(String groupId, String artifactId, String version) throws Exception {
        DefaultArtifact artifact = MavenUtils.buildArtifact(groupId, artifactId, version);
        artifact.setFile(buildJar(artifact));
        return artifact;
    }
}

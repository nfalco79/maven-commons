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

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilder;
import org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException;
import org.apache.maven.shared.dependency.graph.DependencyNode;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolver;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResolverException;
import org.apache.maven.shared.transfer.artifact.resolve.ArtifactResult;

import com.github.nfalco79.maven.dependency.graph.BottomUpDependencyVisitor;
import com.github.nfalco79.maven.dependency.graph.DependencyGraphSession;

/**
 * Utilities for validate resolved dependencies.
 */
public class DependencyResolver {

    private MavenSession session;
    private MavenProject project;
    private DependencyGraphBuilder dependencyGraphBuilder;
    private ArtifactFilter filter;
    private Log logger;
    private ArtifactResolver artifactResolver;

    /**
     * Create a new instance for a maven project.
     *
     * @param session
     *            the Maven Session
     * @param project
     *            the Maven Project
     * @param dependencyGraphBuilder
     *            the builder of the dependency graph
     * @param filter
     *            the filter for the artifact in the dependency graph
     * @param logger
     *            the output logger
     * @param artifactResolver
     *            artifact file resolver
     */
    public DependencyResolver(MavenSession session, MavenProject project, DependencyGraphBuilder dependencyGraphBuilder, ArtifactFilter filter, Log logger, ArtifactResolver artifactResolver) {
        super();
        this.session = session;
        this.project = project;
        this.dependencyGraphBuilder = dependencyGraphBuilder;
        this.filter = filter;
        this.logger = logger;
        this.artifactResolver = artifactResolver;
    }

    /**
     * Resolve and validate all dependencies of the project attempting a given number of time
     *
     * @param maxAttempts
     *            the number of attempts to resolve dependencies in case of wrong artifact file format
     * @return the dependency node after applying the artifact filter
     * @throws DependencyGraphBuilderException
     *             in case of error building the dependency graph
     */
    public DependencyNode resolveDependencies(int maxAttempts) throws DependencyGraphBuilderException {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be greater than 1");
        }
        int count = maxAttempts;
        EmptyArtifactException failure = new EmptyArtifactException();
        while (count-- > 0) {
            try {
                return resolveDependencies();
            } catch (EmptyArtifactException e) {
                failure = e;
            }
        }
        throw failure;
    }

    /**
     * Resolve and validate all dependencies of the project.
     *
     * @return the dependency node after applying the artifact filter
     * @throws DependencyGraphBuilderException
     *             in case of error building the dependency graph
     */
    public DependencyNode resolveDependencies() throws DependencyGraphBuilderException {

        try {
            ProjectBuildingRequest buildingRequest = new DefaultProjectBuildingRequest(getSession().getProjectBuildingRequest());
            buildingRequest.setProject(getProject());
            buildingRequest.setRepositorySession(new DependencyGraphSession(getSession().getRepositorySession()));
            final DependencyNode rootNode = getDependencyGraphBuilder().buildDependencyGraph(buildingRequest, getFilter());

            BottomUpDependencyVisitor visitor = new BottomUpDependencyVisitor();
            rootNode.accept(visitor);

            Collection<Artifact> artifacts = visitor.getNodes().stream() //
                    .filter(a -> a != rootNode.getArtifact()) // remove project artifact
                    .collect(Collectors.toList());

            for (Artifact artifact : artifacts) {
                File artifactFile = artifact.getFile();
                if (artifactFile == null) {
                    resolveArtifact(artifact);
                    continue;
                }

                if (artifactFile.length() == 0) {
                    removeResolvedArtifact(artifact);
                    throw new EmptyArtifactException(artifact);
                }
            }
            return rootNode;
        } catch (IOException e) {
            throw new DependencyGraphBuilderException(e.getMessage(), e);
        }
    }

    private void resolveArtifact(Artifact artifact) throws DependencyGraphBuilderException {
        try {
            ArtifactResult result = artifactResolver.resolveArtifact(getSession().getProjectBuildingRequest(), artifact);
            artifact.setFile(result.getArtifact().getFile());
        } catch (IllegalArgumentException | ArtifactResolverException e) {
            throw new DependencyGraphBuilderException("can not resolve artifact " + artifact.toString(), e);
        }
    }

    protected void removeResolvedArtifact(Artifact artifact) throws IOException {
        File artifactFile = artifact.getFile();
        if (artifactFile != null) {
            // retry to download artifact next resolution time
            FileUtils.deleteDirectory(artifactFile.getParentFile());
        }
    }

    /**
     * The Maven session.
     *
     * @return the Maven session
     */
    public MavenSession getSession() {
        return session;
    }

    /**
     * Set the Maven session.
     *
     * @param session
     *            the Maven session to set
     */
    public void setSession(MavenSession session) {
        this.session = session;
    }

    /**
     * The Maven project.
     *
     * @return the Maven project
     */
    public MavenProject getProject() {
        return project;
    }

    /**
     * Set the Maven project.
     *
     * @param project
     *            the Maven project to set
     */
    public void setProject(MavenProject project) {
        this.project = project;
    }

    /**
     * The builder of the dependency graph.
     *
     * @return the builder of the dependency graph
     */
    public DependencyGraphBuilder getDependencyGraphBuilder() {
        return dependencyGraphBuilder;
    }

    /**
     * Set the builder of the dependency graph.
     *
     * @param dependencyGraphBuilder
     *            the builder of the dependency graph to set
     */
    public void setDependencyGraphBuilder(DependencyGraphBuilder dependencyGraphBuilder) {
        this.dependencyGraphBuilder = dependencyGraphBuilder;
    }

    /**
     * The filter for the artifact in the dependency graph.
     *
     * @return the filter for the artifact in the dependency graph
     */
    public ArtifactFilter getFilter() {
        return filter;
    }

    /**
     * Set the filter for the artifact in the dependency graph.
     *
     * @param filter
     *            the filter for the artifact in the dependency graph to set
     */
    public void setFilter(ArtifactFilter filter) {
        this.filter = filter;
    }

    /**
     * The output logger.
     *
     * @return the outputlogger
     */
    public Log getLogger() {
        return logger;
    }

    /**
     * Set the output logger.
     *
     * @param logger
     *            the output logger to set
     */
    public void setLogger(Log logger) {
        this.logger = logger;
    }

}
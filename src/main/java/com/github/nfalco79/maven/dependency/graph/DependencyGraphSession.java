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

import org.eclipse.aether.AbstractForwardingRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.DependencyGraphTransformer;

/**
 * Special repository session that return no dependency graph transformer. This
 * allow to obtain a raw full detailed dependency graph.
 *
 * @author Nikolas Falco
 */
public class DependencyGraphSession extends AbstractForwardingRepositorySystemSession {

    private RepositorySystemSession session;

    /**
     * Default constructor to proxying a given session.
     *
     * @param session
     *            to proxying
     */
    public DependencyGraphSession(RepositorySystemSession session) {
        this.session = session;
    }

    @Override
    protected RepositorySystemSession getSession() {
        return this.session;
    }

    @Override
    public DependencyGraphTransformer getDependencyGraphTransformer() {
        return null; // returns the untransformable dependency tree
    }
}

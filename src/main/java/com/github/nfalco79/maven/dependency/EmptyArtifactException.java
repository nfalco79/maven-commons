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

import org.apache.maven.artifact.Artifact;

/**
 * A generic exception raised when an error has been reached when the artifact file cannot be read.
 */
@SuppressWarnings("serial")
public class EmptyArtifactException extends RuntimeException {

    /**
     * Default constructor.
     */
    EmptyArtifactException() {
        // used internally, never throw to caller
    }

    /**
     * Constructs a new exception with the specified detail message. The cause is not initialized, and may subsequently be initialized by a call to
     * {@link #initCause}.
     *
     * @param artifact is not able to resolve.
     */
    public EmptyArtifactException(Artifact artifact) {
        super("Fail to download artifact " + artifact.toString() + ", size is 0");
    }

}

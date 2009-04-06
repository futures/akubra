/* $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2008,2009 by Fedora Commons Inc.
 * http://www.fedoracommons.org
 *
 * In collaboration with Topaz Inc.
 * http://www.topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fedoracommons.akubra.rmi.client;

import java.io.IOException;

import java.net.URI;

import java.util.Collections;
import java.util.Set;

import javax.transaction.Transaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.fedoracommons.akubra.BlobStoreConnection;
import org.fedoracommons.akubra.impl.AbstractBlobStore;
import org.fedoracommons.akubra.impl.StreamManager;
import org.fedoracommons.akubra.rmi.remote.RemoteStore;
import org.fedoracommons.akubra.rmi.server.Exporter;
import org.fedoracommons.akubra.rmi.server.ServerTransaction;

/**
 * A BlobStore that forwards calls to a RemoteStore.
 *
 * @author Pradeep Krishnan
 */
public class ClientStore extends AbstractBlobStore {
  private static final Log    log           = LogFactory.getLog(ClientStore.class);
  private final StreamManager streamManager = new StreamManager();
  private final RemoteStore   server;
  private Set<URI>            capabilities;
  private final Exporter      exporter;

  /**
   * Creates a new ClientStore object.
   *
   * @param localId the id of this local store
   * @param server the remote store stub
   * @param exporter the exporter to use for callbacks. If null, the exporter from server is used.
   *
   * @throws IOException on an error in talking to the remote
   */
  public ClientStore(URI localId, RemoteStore server, Exporter exporter)
              throws IOException {
    super(localId);
    this.server = server;

    try {
      capabilities = Collections.unmodifiableSet(server.getCapabilities());
    } catch (Exception e) {
      throw (IOException) new IOException("Failed to get server capabilites").initCause(e);
    }

    try {
      this.exporter = (exporter == null) ? server.getExporter() : exporter;
    } catch (Exception e) {
      throw (IOException) new IOException("Failed to get export config from server").initCause(e);
    }
  }

  public BlobStoreConnection openConnection(Transaction tx)
                                     throws UnsupportedOperationException, IOException {
    ServerTransaction stxn = (tx == null) ? null : new ServerTransaction(tx, getExporter());

    return new ClientConnection(this, streamManager, server.openConnection(stxn));
  }

  public boolean setQuiescent(boolean quiescent) throws IOException {
    return server.setQuiescent(quiescent);
  }

  @Override
  public Set<URI> getCapabilities() {
    try {
      capabilities = Collections.unmodifiableSet(server.getCapabilities());
    } catch (Exception e) {
      log.warn("Failed to refresh capabilities from server. Serving cached capabilities", e);
    }

    return capabilities;
  }

  /**
   * Gets the call-back exporter.
   *
   * @return the exporter
   */
  public Exporter getExporter() {
    return exporter;
  }
}
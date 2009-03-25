/* $HeadURL$
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
package org.fedoracommons.akubra.impl;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.fedoracommons.akubra.BlobStoreConnection;

/**
 * Wraps an <code>InputStream</code> to provide notification to a
 * <code>CloseListener</code> when closed.
 *
 * @author Pradeep Krishnan
 * @author Chris Wilper
 */
class ManagedInputStream extends FilterInputStream {
  private final CloseListener listener;
  private final BlobStoreConnection con;
  private boolean closed = false;

  /**
   * Creates an instance.
   *
   * @param listener the CloseListener to notify when closed.
   * @param stream the stream to wrap.
   * @param con the store connection
   */
  ManagedInputStream(CloseListener listener, InputStream stream, BlobStoreConnection con) {
    super(stream);
    this.listener = listener;
    this.con = con;
  }

  /**
   * Gets the store connection that this stream is part of.
   *
   * @return the store connection
   */
  public BlobStoreConnection getConnection() {
    return con;
  }

  /**
   * Closes the stream, then notifies the CloseListener.
   */
  @Override
  public void close() throws IOException {
    if (!closed) {
      super.close();
      closed = true;
      listener.notifyClosed(this);
    }
  }
}

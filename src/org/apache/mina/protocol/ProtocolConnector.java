/*
 *   @(#) $Id: ProtocolConnector.java 264677 2005-08-30 02:44:35Z trustin $
 *
 *   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.apache.mina.protocol;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * Connects to endpoint, communicates with the server, and fires events to
 * {@link ProtocolProvider}s.
 * <p>
 * Please refer to
 * <a href="../../../../../xref-examples/org/apache/mina/examples/sumup/Client.html">SumUp client</a>
 * example. 
 * <p>
 * You should connect to the desired socket address to start communication,
 * and then events for incoming connections will be sent to the
 * {@link ProtocolHandler} of the specified {@link ProtocolProvider}.
 * <p>
 * Threads connect to endpoint start automatically when
 * {@link #connect(SocketAddress, ProtocolProvider)} is invoked, and stop when
 * all connection attempts are finished.
 * 
 * @author Trustin Lee (trustin@apache.org)
 * @version $Rev: 264677 $, $Date: 2005-08-30 11:44:35 +0900 $
 */
public interface ProtocolConnector extends ProtocolSessionManager
{
    /**
     * Connects to the specified <code>address</code>.  If communication starts
     * successfully, events are fired to the specified
     * <code>protocolProvider</code>.  This method blocks.
     * 
     * @throws IOException if failed to connect
     */
    ProtocolSession connect( SocketAddress address,
                             ProtocolProvider protocolProvider ) throws IOException;

    /**
     * Connects to the specified <code>address</code>.  If communication starts
     * successfully, events are fired to the specified
     * <code>protocolProvider</code>.  This method blocks.
     * 
     * @param localAddress the local address the channel is bound to
     * @throws IOException if failed to connect
     */
    ProtocolSession connect( SocketAddress address, SocketAddress localAddress,
                             ProtocolProvider protocolProvider ) throws IOException;

    /**
     * Connects to the specified <code>address</code> with timeout.  If
     * communication starts successfully, events are fired to the specified
     * <code>protocolProvider</code>.  This method blocks.
     * 
     * @throws IOException if failed to connect
     */
    ProtocolSession connect( SocketAddress address, int timeout,
                             ProtocolProvider protocolProvider ) throws IOException;

    /**
     * Connects to the specified <code>address</code> with timeout.  If
     * communication starts successfully, events are fired to the specified
     * <code>protocolProvider</code>.  This method blocks.
     * 
     * @param localAddress the local address the channel is bound to
     * @throws IOException if failed to connect
     */
    ProtocolSession connect( SocketAddress address, SocketAddress localAddress,
                             int timeout, ProtocolProvider protocolProvider ) throws IOException;
}
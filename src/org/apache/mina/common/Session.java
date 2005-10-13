/*
 *   @(#) $Id: Session.java 264677 2005-08-30 02:44:35Z trustin $
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
package org.apache.mina.common;

import java.net.SocketAddress;
import java.util.Set;

/**
 * A handle which represents connection between two endpoints regardless of 
 * transport types.
 * <p>
 * Session provides user-defined attributes.  User-defined attributes are
 * application-specific data which is associated with a session.
 * It often contains objects that represents the state of a higher-level protocol
 * and becomes a way to exchange data between filters and handlers.
 *   
 * @author Trustin Lee (trustin@apache.org)
 * @version $Rev: 264677 $, $Date: 2005-08-30 11:44:35 +0900 $
 */
public interface Session {

    /**
     * Closes this session immediately.  Calling method is identical with
     * calling <tt>close( false )</tt>.
     */
    void close();

    /**
     * Closes this session immediately.
     * 
     * @param wait <tt>true</tt> if you want to wait until closing process is
     *             complete.
     */
    void close( boolean wait );
    
    /**
     * Returns an attachment of this session.
     * This method is identical with <tt>getAttribute( "" )</tt>.
     */
    Object getAttachment();

    /**
     * Sets an attachment of this session.
     * This method is identical with <tt>setAttribute( "", attachment )</tt>.
     * 
     * @return Old attachment.  <tt>null</tt> if it is new.
     */
    Object setAttachment( Object attachment );
    
    /**
     * Returns the value of user-defined attribute of this session.
     * 
     * @param key the key of the attribute
     * @return <tt>null</tt> if there is no attribute with the specified key
     */
    Object getAttribute( String key );
    
    /**
     * Sets a user-defined attribute.
     * 
     * @param key the key of the attribute
     * @param value the value of the attribute
     * @return The old value of the attribute.  <tt>null</tt> if it is new.
     */
    Object setAttribute( String key, Object value );
    
    /**
     * Removes a user-defined attribute with the specified key.
     * 
     * @return The old value of the attribute.  <tt>null</tt> if not found.
     */
    Object removeAttribute( String key );
    
    /**
     * Returns the set of keys of all user-defined attributes.
     */
    Set getAttributeKeys();
    
    /**
     * Returns transport type of this session.
     */
    TransportType getTransportType();

    /**
     * Returns <code>true</code> if this session is connected with remote peer.
     */
    boolean isConnected();

    /**
     * Returns the configuration of this session.
     */
    SessionConfig getConfig();

    /**
     * Returns the socket address of remote peer. 
     */
    SocketAddress getRemoteAddress();

    /**
     * Returns the socket address of local machine which is associated with this
     * session.
     */
    SocketAddress getLocalAddress();

    /**
     * Returns the total number of bytes which were read from this session.
     */
    long getReadBytes();

    /**
     * Returns the total number of bytes which were written to this session.
     */
    long getWrittenBytes();

    /**
     * Returns the total number of write requests which were written to this session.
     */
    long getWrittenWriteRequests();
    
    /**
     * Returns the number of write requests which are scheduled to be written
     * to this session.
     */
    int getScheduledWriteRequests();

    /**
     * Returns the time in millis when I/O occurred lastly.
     */
    long getLastIoTime();

    /**
     * Returns the time in millis when read operation occurred lastly.
     */
    long getLastReadTime();

    /**
     * Returns the time in millis when write operation occurred lastly.
     */
    long getLastWriteTime();

    /**
     * Returns <code>true</code> if this session is idle for the specified 
     * {@link IdleStatus}.
     */
    boolean isIdle( IdleStatus status );
}

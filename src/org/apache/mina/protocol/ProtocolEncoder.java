/*
 *   @(#) $Id: ProtocolEncoder.java 264677 2005-08-30 02:44:35Z trustin $
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

import org.apache.mina.common.ByteBuffer;

/**
 * Encodes higher-level message objects into binary or protocol-specific data.
 * MINA invokes {@link #encode(ProtocolSession, Object, ProtocolEncoderOutput)}
 * method with message which is popped from the session write queue, and then
 * the encoder implementation puts encoded {@link ByteBuffer}s into
 * {@link ProtocolEncoderOutput} by calling
 * {@link ProtocolEncoderOutput#write(ByteBuffer)}.
 * <p>
 * Please refer to
 * <a href="../../../../../xref-examples/org/apache/mina/examples/reverser/TextLineEncoder.html"><code>TextLineEncoder</code></a>
 * example. 
 * 
 * @author Trustin Lee (trustin@apache.org)
 * @version $Rev: 264677 $, $Date: 2005-08-30 11:44:35 +0900 $
 */
public interface ProtocolEncoder
{

    /**
     * Encodes higher-level message objects into binary or protocol-specific data.
     * MINA invokes {@link #encode(ProtocolSession, Object, ProtocolEncoderOutput)}
     * method with message which is popped from the session write queue, and then
     * the encoder implementation puts encoded {@link ByteBuffer}s into
     * {@link ProtocolEncoderOutput}.
     * 
     * @throws ProtocolViolationException if the message violated protocol
     *                                    specification
     */
    void encode( ProtocolSession session, Object message,
                 ProtocolEncoderOutput out ) throws ProtocolViolationException;
}
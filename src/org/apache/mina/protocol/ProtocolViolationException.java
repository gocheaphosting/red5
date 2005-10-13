/*
 *   @(#) $Id: ProtocolViolationException.java 264677 2005-08-30 02:44:35Z trustin $
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
 * An exception that is thrown when {@link ProtocolEncoder} cannot understand or
 * failed to validate the specified message, or when {@link ProtocolDecoder}
 * cannot understand or failed to validate the specified {@link ByteBuffer}
 * content.
 * 
 * @author Trustin Lee (trustin@apache.org)
 * @version $Rev: 264677 $, $Date: 2005-08-30 11:44:35 +0900 $
 */
public class ProtocolViolationException extends Exception
{
    private static final long serialVersionUID = 3545799879533408565L;

	private ByteBuffer buffer;

    /**
     * Constructs a new instance.
     */
    public ProtocolViolationException()
    {
    }

    /**
     * Constructs a new instance with the specified message.
     */
    public ProtocolViolationException( String message )
    {
        super( message );
    }

    /**
     * Constructs a new instance with the specified cause.
     */
    public ProtocolViolationException( Throwable cause )
    {
        super( cause );
    }

    /**
     * Constructs a new instance with the specified message and the specified
     * cause.
     */
    public ProtocolViolationException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * Returns the message and the hexdump of the unknown part.
     */
    public String getMessage()
    {
        String message = super.getMessage();

        if( message == null )
        {
            message = "";
        }

        if( buffer != null )
        {
            return message + ( ( message.length() > 0 ) ? " " : "" )
                   + "(Hexdump: " + buffer.getHexDump() + ')';
        }
        else
        {
            return message;
        }
    }

    /**
     * Returns unknown message part.
     */
    public ByteBuffer getBuffer()
    {
        return buffer;
    }

    /**
     * Sets unknown message part.
     */
    public void setBuffer( ByteBuffer buffer )
    {
        this.buffer = buffer;
    }
}
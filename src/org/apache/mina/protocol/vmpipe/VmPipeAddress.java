/*
 * @(#) $Id: VmPipeAddress.java 264677 2005-08-30 02:44:35Z trustin $
 */
package org.apache.mina.protocol.vmpipe;

import java.net.SocketAddress;

/**
 * A {@link SocketAddress} which represents in-VM pipe port number.
 * 
 * @author Trustin Lee (trustin@apache.org)
 * @version $Rev: 264677 $, $Date: 2005-08-30 11:44:35 +0900 $
 */
public class VmPipeAddress extends SocketAddress implements Comparable
{
    private static final long serialVersionUID = 3257844376976830515L;

	private final int port;

    /**
     * Creates a new instance with the specifid port number.
     */
    public VmPipeAddress( int port )
    {
        this.port = port;
    }

    /**
     * Returns the port number.
     */
    public int getPort()
    {
        return port;
    }

    public int hashCode()
    {
        return port;
    }

    public boolean equals( Object o )
    {
        if( o == null )
            return false;
        if( this == o )
            return true;
        if( o instanceof VmPipeAddress )
        {
            VmPipeAddress that = ( VmPipeAddress ) o;
            return this.port == that.port;
        }

        return false;
    }

    public int compareTo( Object o )
    {
        return this.port - ( ( VmPipeAddress ) o ).port;
    }

    public String toString()
    {
        return "vm:" + port;
    }
}
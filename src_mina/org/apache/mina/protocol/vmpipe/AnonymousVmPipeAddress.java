/*
 * @(#) $Id: AnonymousVmPipeAddress.java 327113 2005-10-21 06:59:15Z trustin $
 */
package org.apache.mina.protocol.vmpipe;

import java.net.SocketAddress;

/**
 * A {@link SocketAddress} which represents anonymous in-VM pipe port.
 * 
 * @author The Apache Directory Project (dev@directory.apache.org)
 * @version $Rev: 327113 $, $Date: 2005-10-21 15:59:15 +0900 $
 */
class AnonymousVmPipeAddress extends SocketAddress implements Comparable
{
    private static final long serialVersionUID = 3258135768999475512L;

	static final AnonymousVmPipeAddress INSTANCE = new AnonymousVmPipeAddress();

    /**
     * Creates a new instance with the specifid port number.
     */
    private AnonymousVmPipeAddress()
    {
    }

    public int hashCode()
    {
        return 1432482932;
    }

    public boolean equals( Object o )
    {
        if( o == null )
            return false;
        if( this == o )
            return true;
        return o instanceof AnonymousVmPipeAddress;
    }

    public int compareTo( Object o )
    {
        return this.hashCode() - ( ( AnonymousVmPipeAddress ) o ).hashCode();
    }

    public String toString()
    {
        return "vm:anonymous";
    }
}
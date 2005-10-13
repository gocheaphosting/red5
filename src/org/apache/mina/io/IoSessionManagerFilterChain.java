/*
 *   @(#) $Id: IoSessionManagerFilterChain.java 264677 2005-08-30 02:44:35Z trustin $
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
package org.apache.mina.io;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IdleStatus;

/**
 * An {@link IoFilterChain} that forwards all events
 * except <tt>filterWrite</tt> to the {@link IoSessionFilterChain}
 * of the recipient session.
 * <p>
 * This filter chain is used by implementations of {@link IoSessionManager}s.
 * 
 * @author The Apache Directory Project (dev@directory.apache.org)
 * @author Trustin Lee (trustin@apache.org)
 * @version $Rev: 264677 $, $Date: 2005-08-30 11:44:35 +0900 $
 */
public abstract class IoSessionManagerFilterChain extends AbstractIoFilterChain {

    private final IoSessionManager manager;

    protected IoSessionManagerFilterChain( IoSessionManager manager )
    {
        this.manager = manager;
    }
    
    public IoSessionManager getManager()
    {
        return manager;
    }
    
    protected IoFilter createTailFilter()
    {
        return new IoFilter()
        {
            public void sessionOpened( NextFilter nextFilter, IoSession session ) throws Exception
            {
                ( ( IoSessionFilterChain ) session.getFilterChain() ).sessionOpened( session );
            }

            public void sessionClosed( NextFilter nextFilter, IoSession session ) throws Exception
            {
                ( ( IoSessionFilterChain ) session.getFilterChain() ).sessionClosed( session );
            }

            public void sessionIdle( NextFilter nextFilter, IoSession session,
                                    IdleStatus status ) throws Exception
            {
                ( ( IoSessionFilterChain ) session.getFilterChain() ).sessionIdle( session, status );
            }

            public void exceptionCaught( NextFilter nextFilter,
                                        IoSession session, Throwable cause ) throws Exception
            {
                ( ( IoSessionFilterChain ) session.getFilterChain() ).exceptionCaught( session, cause );
            }

            public void dataRead( NextFilter nextFilter, IoSession session,
                                 ByteBuffer buf ) throws Exception
            {
                ( ( IoSessionFilterChain ) session.getFilterChain() ).dataRead( session, buf );
            }

            public void dataWritten( NextFilter nextFilter, IoSession session,
                                    Object marker ) throws Exception
            {
                ( ( IoSessionFilterChain ) session.getFilterChain() ).dataWritten( session, marker );
            }

            public void filterWrite( NextFilter nextFilter,
                                     IoSession session, ByteBuffer buf, Object marker ) throws Exception
            {
                nextFilter.filterWrite( session, buf, marker );
            }
        };
    }
}

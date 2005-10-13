/*
 *   @(#) $Id: SocketIoProcessor.java 264677 2005-08-30 02:44:35Z trustin $
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
package org.apache.mina.io.socket;

import java.io.IOException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.SessionConfig;
import org.apache.mina.io.WriteTimeoutException;
import org.apache.mina.util.Queue;

/**
 * Performs all I/O operations for sockets which is connected or bound.
 * This class is used by MINA internally.
 * 
 * @author Trustin Lee (trustin@apache.org)
 * @version $Rev: 264677 $, $Date: 2005-08-30 11:44:35 +0900 $,
 */
class SocketIoProcessor
{
    private static final SocketIoProcessor instance;

    static
    {
        SocketIoProcessor tmp;

        try
        {
            tmp = new SocketIoProcessor();
        }
        catch( IOException e )
        {
            InternalError error = new InternalError(
                                                     "Failed to open selector." );
            error.initCause( e );
            throw error;
        }

        instance = tmp;
    }

    private final Selector selector;

    private final Queue newSessions = new Queue();

    private final Queue removingSessions = new Queue();

    private final Queue flushingSessions = new Queue();

    private final Queue readableSessions = new Queue();

    private Worker worker;

    private long lastIdleCheckTime = System.currentTimeMillis();

    private SocketIoProcessor() throws IOException
    {
        selector = Selector.open();
    }

    static SocketIoProcessor getInstance()
    {
        return instance;
    }

    void addSession( SocketSession session )
    {
        synchronized( this )
        {
            synchronized( newSessions )
            {
                newSessions.push( session );
            }
            startupWorker();
        }

        selector.wakeup();
    }

    void removeSession( SocketSession session )
    {
        scheduleRemove( session );
        startupWorker();
        selector.wakeup();
    }

    private synchronized void startupWorker()
    {
        if( worker == null )
        {
            worker = new Worker();
            worker.start();
        }
    }

    void flushSession( SocketSession session )
    {
        scheduleFlush( session );
        selector.wakeup();
    }

    void addReadableSession( SocketSession session )
    {
        synchronized( readableSessions )
        {
            readableSessions.push( session );
        }
        selector.wakeup();
    }

    private void addSessions()
    {
        if( newSessions.isEmpty() )
            return;

        SocketSession session;

        for( ;; )
        {
            synchronized( newSessions )
            {
                session = ( SocketSession ) newSessions.pop();
            }

            if( session == null )
                break;

            SocketChannel ch = session.getChannel();
            boolean registered;

            try
            {
                ch.configureBlocking( false );
                session.setSelectionKey( ch.register( selector,
                                                      SelectionKey.OP_READ,
                                                      session ) );
                registered = true;
            }
            catch( IOException e )
            {
                registered = false;
                session.getManagerFilterChain().exceptionCaught( session, e );
            }

            if( registered )
            {
                session.getManagerFilterChain().sessionOpened( session );
            }
        }
    }

    private void removeSessions()
    {
        if( removingSessions.isEmpty() )
            return;

        for( ;; )
        {
            SocketSession session;

            synchronized( removingSessions )
            {
                session = ( SocketSession ) removingSessions.pop();
            }

            if( session == null )
                break;

            SocketChannel ch = session.getChannel();
            SelectionKey key = session.getSelectionKey();
            // Retry later if session is not yet fully initialized.
            // (In case that Session.close() is called before addSession() is processed)
            if( key == null )
            {
                scheduleRemove( session );
                break;
            }

            // skip if channel is already closed
            if( !key.isValid() )
            {
                continue;
            }

            try
            {
                key.cancel();
                ch.close();
            }
            catch( IOException e )
            {
                session.getManagerFilterChain().exceptionCaught( session, e );
            }
            finally
            {
                releaseWriteBuffers( session );

                session.getManagerFilterChain().sessionClosed( session );
                session.notifyClose();
            }
        }
    }

    private void processSessions( Set selectedKeys )
    {
        Iterator it = selectedKeys.iterator();

        while( it.hasNext() )
        {
            SelectionKey key = ( SelectionKey ) it.next();
            SocketSession session = ( SocketSession ) key.attachment();

            if( key.isReadable() )
            {
                read( session );
            }

            if( key.isWritable() )
            {
                scheduleFlush( session );
            }
        }

        selectedKeys.clear();
    }

    private void read( SocketSession session )
    {
        ByteBuffer buf = ByteBuffer.allocate(
                (( SocketSessionConfig ) session.getConfig()).getSessionReceiveBufferSize() ); 
        SocketChannel ch = session.getChannel();

        try
        {
            int readBytes = 0;
            int ret;

            buf.clear();

            try
            {
                while( ( ret = ch.read( buf.buf() ) ) > 0 )
                {
                    readBytes += ret;
                }
            }
            finally
            {
                buf.flip();
            }

            session.increaseReadBytes( readBytes );
            session.setIdle( IdleStatus.BOTH_IDLE, false );
            session.setIdle( IdleStatus.READER_IDLE, false );

            if( readBytes > 0 )
            {
                ByteBuffer newBuf = ByteBuffer.allocate( readBytes );
                newBuf.put( buf );
                newBuf.flip();
                session.getManagerFilterChain().dataRead( session, newBuf );
            }
            if( ret < 0 )
            {
                scheduleRemove( session );
            }
        }
        catch( Throwable e )
        {
            if( e instanceof IOException )
                scheduleRemove( session );
            session.getManagerFilterChain().exceptionCaught( session, e );
        }
        finally
        {
            buf.release();
        }
    }

    private void scheduleRemove( SocketSession session )
    {
        synchronized( removingSessions )
        {
            removingSessions.push( session );
        }
    }

    private void scheduleFlush( SocketSession session )
    {
        synchronized( flushingSessions )
        {
            flushingSessions.push( session );
        }
    }

    private void notifyIdleSessions()
    {
        Set keys = selector.keys();
        Iterator it;
        SocketSession session;

        // process idle sessions
        long currentTime = System.currentTimeMillis();

        if( ( keys != null ) && ( ( currentTime - lastIdleCheckTime ) >= 1000 ) )
        {
            lastIdleCheckTime = currentTime;
            it = keys.iterator();

            while( it.hasNext() )
            {
                SelectionKey key = ( SelectionKey ) it.next();
                session = ( SocketSession ) key.attachment();

                notifyIdleSession( session, currentTime );
            }
        }
    }

    private void notifyIdleSession( SocketSession session, long currentTime )
    {
        SessionConfig config = session.getConfig();

        notifyIdleSession0( session, currentTime, config
                .getIdleTimeInMillis( IdleStatus.BOTH_IDLE ),
                            IdleStatus.BOTH_IDLE, session.getLastIoTime() );
        notifyIdleSession0( session, currentTime, config
                .getIdleTimeInMillis( IdleStatus.READER_IDLE ),
                            IdleStatus.READER_IDLE, session.getLastReadTime() );
        notifyIdleSession0( session, currentTime, config
                .getIdleTimeInMillis( IdleStatus.WRITER_IDLE ),
                            IdleStatus.WRITER_IDLE, session.getLastWriteTime() );

        notifyWriteTimeoutSession( session, currentTime, config
                .getWriteTimeoutInMillis(), session.getLastWriteTime() );
    }

    private void notifyIdleSession0( SocketSession session, long currentTime,
                                    long idleTime, IdleStatus status,
                                    long lastIoTime )
    {
        if( idleTime > 0 && !session.isIdle( status ) && lastIoTime != 0
            && ( currentTime - lastIoTime ) >= idleTime )
        {
            session.setIdle( status, true );
            session.getManagerFilterChain().sessionIdle( session, status );
        }
    }

    private void notifyWriteTimeoutSession( SocketSession session,
                                           long currentTime,
                                           long writeTimeout, long lastIoTime )
    {
        if( writeTimeout > 0
            && ( currentTime - lastIoTime ) >= writeTimeout
            && session.getSelectionKey() != null
            && ( session.getSelectionKey().interestOps() & SelectionKey.OP_WRITE ) != 0 )
        {
            session
                    .getManagerFilterChain()
                    .exceptionCaught( session, new WriteTimeoutException() );
        }
    }

    private void flushSessions()
    {
        if( flushingSessions.size() == 0 )
            return;

        for( ;; )
        {
            SocketSession session;

            synchronized( flushingSessions )
            {
                session = ( SocketSession ) flushingSessions.pop();
            }

            if( session == null )
                break;

            if( !session.isConnected() )
            {
                releaseWriteBuffers( session );
                continue;
            }

            // If encountered write request before session is initialized, 
            // (In case that Session.write() is called before addSession() is processed)
            if( session.getSelectionKey() == null )
            {
                // Reschedule for later write
                scheduleFlush( session );
                break;
            }
            else
            {
                try
                {
                    flush( session );
                }
                catch( CancelledKeyException e )
                {
                    // Connection is closed unexpectedly.
                    scheduleRemove( session );
                }
                catch( IOException e )
                {
                    scheduleRemove( session );
                    session.getManagerFilterChain().exceptionCaught( session, e );
                }
            }
        }
    }
    
    private void releaseWriteBuffers( SocketSession session )
    {
        Queue writeBufferQueue = session.getWriteBufferQueue();
        session.getWriteMarkerQueue().clear();
        ByteBuffer buf;
        
        while( ( buf = (ByteBuffer) writeBufferQueue.pop() ) != null )
        {
            try
            {
                buf.release();
            }
            catch( IllegalStateException e )
            {
                session.getManagerFilterChain().exceptionCaught( session, e );
            }
        }
    }

    private void flush( SocketSession session ) throws IOException
    {
        SocketChannel ch = session.getChannel();

        Queue writeBufferQueue = session.getWriteBufferQueue();
        Queue writeMarkerQueue = session.getWriteMarkerQueue();

        ByteBuffer buf;
        Object marker;
        for( ;; )
        {
            synchronized( writeBufferQueue )
            {
                buf = ( ByteBuffer ) writeBufferQueue.first();
                marker = writeMarkerQueue.first();
            }

            if( buf == null )
                break;

            if( buf.remaining() == 0 )
            {
                synchronized( writeBufferQueue )
                {
                    writeBufferQueue.pop();
                    writeMarkerQueue.pop();
                }
                try
                {
                    buf.release();
                }
                catch( IllegalStateException e )
                {
                    session.getManagerFilterChain().exceptionCaught( session, e );
                }

                session.increaseWrittenWriteRequests();
                session.getManagerFilterChain().dataWritten( session, marker );
                continue;
            }

            int writtenBytes = 0;
            try
            {
                writtenBytes = ch.write( buf.buf() );
            }
            finally
            {
                if( writtenBytes > 0 )
                {
                    session.increaseWrittenBytes( writtenBytes );
                    session.setIdle( IdleStatus.BOTH_IDLE, false );
                    session.setIdle( IdleStatus.WRITER_IDLE, false );
                }

                SelectionKey key = session.getSelectionKey();
                if( buf.hasRemaining() )
                {
                    // Kernel buffer is full
                    key
                            .interestOps( key.interestOps()
                                          | SelectionKey.OP_WRITE );
                    break;
                }
                else
                {
                    key.interestOps( key.interestOps()
                                     & ( ~SelectionKey.OP_WRITE ) );
                }
            }
        }
    }

    private class Worker extends Thread
    {
        public Worker()
        {
            super( "SocketIoProcessor" );
        }

        public void run()
        {
            for( ;; )
            {
                try
                {
                    int nKeys = selector.select( 1000 );
                    addSessions();

                    if( nKeys > 0 )
                    {
                        processSessions( selector.selectedKeys() );
                    }

                    flushSessions();
                    removeSessions();
                    notifyIdleSessions();

                    if( selector.keys().isEmpty() )
                    {
                        synchronized( SocketIoProcessor.this )
                        {
                            if( selector.keys().isEmpty() &&
                                newSessions.isEmpty() )
                            {
                                worker = null;
                                break;
                            }
                        }
                    }
                }
                catch( IOException e )
                {
                    e.printStackTrace();

                    try
                    {
                        Thread.sleep( 1000 );
                    }
                    catch( InterruptedException e1 )
                    {
                    }
                }
            }
        }
    }
}

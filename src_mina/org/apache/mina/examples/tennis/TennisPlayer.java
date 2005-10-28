/*
 * @(#) $Id: TennisPlayer.java 327113 2005-10-21 06:59:15Z trustin $
 */
package org.apache.mina.examples.tennis;

import org.apache.mina.protocol.ProtocolCodecFactory;
import org.apache.mina.protocol.ProtocolHandler;
import org.apache.mina.protocol.ProtocolHandlerAdapter;
import org.apache.mina.protocol.ProtocolProvider;
import org.apache.mina.protocol.ProtocolSession;

/**
 * A {@link ProtocolHandler} implementation which plays a tennis game.
 * 
 * @author The Apache Directory Project (dev@directory.apache.org)
 * @version $Rev: 327113 $, $Date: 2005-10-21 15:59:15 +0900 $
 */
public class TennisPlayer implements ProtocolProvider
{
    private final ProtocolHandler HANDLER = new TennisPlayerHandler();
    
    public ProtocolCodecFactory getCodecFactory()
    {
        throw new UnsupportedOperationException();
    }

    public ProtocolHandler getHandler()
    {
        return HANDLER;
    }

    private static class TennisPlayerHandler extends ProtocolHandlerAdapter
    {
        private static int nextId = 0;

        /** Player ID **/
        private final int id = nextId++;

        public void sessionOpened( ProtocolSession session )
        {
            System.out.println( "Player-" + id + ": READY" );
        }

        public void sessionClosed( ProtocolSession session )
        {
            System.out.println( "Player-" + id + ": QUIT" );
        }

        public void messageReceived( ProtocolSession session, Object message )
        {
            System.out.println( "Player-" + id + ": RCVD " + message );

            TennisBall ball = ( TennisBall ) message;

            // Stroke: TTL decreases and PING/PONG state changes.
            ball = ball.stroke();

            if( ball.getTTL() > 0 )
            {
                // If the ball is still alive, pass it back to peer.
                session.write( ball );
            }
            else
            {
                // If the ball is dead, this player loses.
                System.out.println( "Player-" + id + ": LOSE" );
                session.close();
            }
        }

        public void messageSent( ProtocolSession session, Object message )
        {
            System.out.println( "Player-" + id + ": SENT " + message );
        }
    }
}
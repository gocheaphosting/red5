/*
 *   @(#) $Id: ResultMessageDecoder.java 327113 2005-10-21 06:59:15Z trustin $
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
package org.apache.mina.examples.sumup.codec;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.examples.sumup.message.AbstractMessage;
import org.apache.mina.examples.sumup.message.ResultMessage;
import org.apache.mina.protocol.ProtocolSession;
import org.apache.mina.protocol.codec.MessageDecoder;

/**
 * A {@link MessageDecoder} that decodes {@link ResultMessage}.
 *
 * @author The Apache Directory Project
 * @version $Rev: 327113 $, $Date: 2005-10-21 15:59:15 +0900 $
 */
public class ResultMessageDecoder extends AbstractMessageDecoder
{
    private int code;
    private boolean readCode;
    
    public ResultMessageDecoder()
    {
        super( Constants.RESULT );
    }

    protected AbstractMessage decodeBody( ProtocolSession session, ByteBuffer in )
    {
        if( !readCode )
        {
            if( in.remaining() < Constants.RESULT_CODE_LEN )
            {
                return null; // Need more data.
            }
            
            code = in.getShort();
            readCode = true;
        }
        
        if( code == Constants.RESULT_OK )
        {
            if( in.remaining() < Constants.RESULT_VALUE_LEN )
            {
                return null;
            }
            
            ResultMessage m = new ResultMessage();
            m.setOk( true );
            m.setValue( in.getInt() );
            readCode = false;
            return m;
        }
        else
        {
            ResultMessage m = new ResultMessage();
            m.setOk( false );
            readCode = false;
            return m;
        }
    }
}

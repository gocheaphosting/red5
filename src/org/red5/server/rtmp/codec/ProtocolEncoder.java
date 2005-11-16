package org.red5.server.rtmp.codec;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.protocol.ProtocolEncoderOutput;
import org.apache.mina.protocol.ProtocolSession;
import org.apache.mina.protocol.ProtocolViolationException;
import org.red5.server.io.BufferUtils;
import org.red5.server.io.Serializer;
import org.red5.server.io.amf.Output;
import org.red5.server.protocol.rtmp.RTMPUtils;
import org.red5.server.rtmp.Channel;
import org.red5.server.rtmp.Connection;
import org.red5.server.rtmp.message.AudioData;
import org.red5.server.rtmp.message.Constants;
import org.red5.server.rtmp.message.Invoke;
import org.red5.server.rtmp.message.Message;
import org.red5.server.rtmp.message.Notify;
import org.red5.server.rtmp.message.OutPacket;
import org.red5.server.rtmp.message.PacketHeader;
import org.red5.server.rtmp.message.Ping;
import org.red5.server.rtmp.message.SharedObject;
import org.red5.server.rtmp.message.StreamBytesRead;
import org.red5.server.rtmp.message.VideoData;
import org.red5.server.service.Call;

public class ProtocolEncoder implements org.apache.mina.protocol.ProtocolEncoder, Constants {

	protected static Log log =
        LogFactory.getLog(ProtocolEncoder.class.getName());

	protected static Log ioLog =
        LogFactory.getLog(ProtocolEncoder.class.getName()+".out");
	
	private Serializer serializer = null;
	
	public void encode(ProtocolSession session, Object message, ProtocolEncoderOutput out) 
		throws ProtocolViolationException {
				
		try {
			
			if(message instanceof ByteBuffer){
				if(log.isDebugEnabled())
					log.debug("Sending raw buffer");
				out.write((ByteBuffer)message);
				return;
			}
			
			final Connection conn = (Connection) session.getAttachment();
			final OutPacket packet = (OutPacket) message;
			final PacketHeader header = packet.getDestination();
			final Channel channel = conn.getChannel(header.getChannelId());	
			
			/*
			if(conn.getState() == Connection.STATE_HANDSHAKE){
				log.debug("Sending handshake");
				out.write(packet.getMessage().getData());
				return;
			}
			*/
			
			final ByteBuffer data = encodeMessage(packet.getMessage());
			header.setSize(data.limit());
			ByteBuffer headers = encodeHeader(header,channel.getLastWriteHeader());

			ByteBuffer buf = null;
			buf = ByteBuffer.allocate(data.limit()+(int)Math.floor(data.limit()/128)); // FIX ME
			buf.setAutoExpand(true);

			headers.flip();	
			buf.put(headers);
			headers.release();
	
			int numChunks =  (int) Math.ceil((header.getSize() / (float) header.getChunkSize()));
			
			// TODO: threadsafe way of doing this reusing the data here, im thinking a lock
			for(int i=0; i<numChunks; i++){
				int readAmount = (data.remaining()>header.getChunkSize()) 
					? header.getChunkSize() : data.remaining();
				if(log.isDebugEnabled())
					log.debug("putting chunk: "+readAmount);
				BufferUtils.put(buf,data,readAmount);
				if(data.remaining()>0){
					// log.debug("header byte");
					buf.put(RTMPUtils.encodeHeaderByte(HEADER_CONTINUE, header.getChannelId()));
				}
			}
			
			buf.flip();
			
			if(ioLog.isDebugEnabled()){
				ioLog.debug(packet.getDestination());
				ioLog.debug(packet.getMessage());
				ioLog.debug(buf.getHexDump());
			}
			
			out.write(buf);
			
			// this will destroy the packet if there are no more refs
			packet.getMessage().release();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void setSerializer(Serializer serializer) {
		this.serializer = serializer;
	}

	public ByteBuffer encodeHeader(PacketHeader header, PacketHeader lastHeader){
		
		ByteBuffer buf = ByteBuffer.allocate(9);
		
		byte headerByte = RTMPUtils.encodeHeaderByte(HEADER_NEW, header.getChannelId());
		
		buf.put(headerByte);
		
		// write timer
		RTMPUtils.writeMediumInt(buf, header.getTimer());
		
		// write size
		RTMPUtils.writeMediumInt(buf, header.getSize());
		
		// write datatype
		buf.put(header.getDataType());
		
		// write stream
		RTMPUtils.writeReverseInt(buf, header.getStreamId());
		return buf;
	}
	
	public ByteBuffer encodeMessage(Message message){
		if(message.isSealed()){
			return message.getData();
		}
		switch(message.getDataType()){
		case TYPE_INVOKE:
			encodeInvoke((Invoke) message);
			break;
		case TYPE_NOTIFY:
			encodeInvoke((Invoke) message);
			break;
		case TYPE_PING:
			encodePing((Ping) message);
			break;
		case TYPE_STREAM_BYTES_READ:
			encodeStreamBytesRead((StreamBytesRead) message);
			break;
		case TYPE_AUDIO_DATA:
			encodeAudioData((AudioData) message);
			break;
		case TYPE_VIDEO_DATA:
			encodeVideoData((VideoData) message);
			break;
		case TYPE_SHARED_OBJECT:
			encodeSharedObject((SharedObject) message);
			break;
		}
		message.getData().flip();
		message.setSealed(true);
		return message.getData();
	}
	
	private void encodeSharedObject(SharedObject so) {
		final ByteBuffer data = so.getData();
		if(so.getTimer()!=-1){
			data.putInt(so.getTimer());
		}
		Output output = new Output(data);
		Output.putString(data,so.getName());
		data.putLong(so.getId());
		Iterator it = so.getNumbers().iterator();
		while(it.hasNext()){
			serializer.serialize(output,(Number) it.next());
		} 
		if(so.getKey()!=null){
			serializer.serialize(output,(String) so.getKey());
		}
		if(so.getValue()!=null){
			serializer.serialize(output,(Object) so.getValue());
		}
	}

	public void encodeInvoke(Invoke invoke){
		// TODO: tidy up here
		// log.debug("Encode invoke");
		Output output = new Output(invoke.getData());
		
		final boolean isPending =(invoke.getCall().getStatus()==Call.STATUS_PENDING);
		
		if(!isPending){
			if(log.isDebugEnabled())
				log.debug("Call has been executed, send result");
			serializer.serialize(output, "_result"); // seems right
		} else {
			if(log.isDebugEnabled())
				log.debug("This is a pending call, send request");
			serializer.serialize(output, invoke.getCall().getServiceMethodName()); // seems right
		}
		
		// dont know what this number does, so im just sending it back
		serializer.serialize(output, new Integer(invoke.getInvokeId())); 
		serializer.serialize(output, null);
		if(!isPending){
			if(log.isDebugEnabled())
				log.debug("Writing result: "+invoke.getCall().getResult());
			serializer.serialize(output, invoke.getCall().getResult());
		} else {
			if(log.isDebugEnabled())
				log.debug("Writing params");
			final Object[] args = invoke.getCall().getArguments();
			if(args!=null){
				for (int i = 0; i < args.length; i++) {
					serializer.serialize(output, args[i]);
				}
			}
		}
		
		//invoke.getData().flip();
	}
	
	public void encodeNotify(Notify notify){

	}
	
	public void encodePing(Ping ping){
		final ByteBuffer out = ping.getData();
		out.putShort(ping.getValue1());
		out.putInt(ping.getValue2());
		if(ping.getValue3()!=Ping.UNDEFINED)
			out.putInt(ping.getValue3());
		if(log.isDebugEnabled())
			log.debug(ping);
	}
	
	public void encodeStreamBytesRead(StreamBytesRead streamBytesRead){
		final ByteBuffer out = streamBytesRead.getData();
		out.putInt(streamBytesRead.getBytesRead());
	}
	
	public void encodeAudioData(AudioData audioData){
	}
	
	public void encodeVideoData(VideoData videoData){

	}
	
	public void enchunkData(ByteBuffer in, int chunkSize){

	}
}

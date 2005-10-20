package org.red5.server.protocol.rtmp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.ByteBuffer;
import org.red5.server.io.flv.FLVHeader;
import org.red5.server.io.flv2.FLVReader;
import org.red5.server.io.flv2.FLVTag;
import org.red5.server.protocol.rtmp.status2.StatusObjectService;

public class Stream {
	
	protected static Log log =
        LogFactory.getLog(Stream.class.getName());
	
	public static final byte STATE_STOPPED = 0x00;
	public static final byte STATE_PLAYING = 0x02;
	public static final byte STATE_PAUSED = 0x03;
	public static final byte STATE_END = 0x04;
	
	protected Channel dataChannel = null;
	protected Channel audioChannel = null;
	protected Channel videoChannel = null; 
	protected int source = -1;
	
	protected String flvPath;
	protected int currentTag = 0;
	protected int numTags = 0;
	
	private FLVReader flvReader = null;
	
	protected SessionHandler sessionHandler;
	protected Connection conn;
	
	protected byte state = STATE_STOPPED;
	
	public Stream(Connection conn, int source, SessionHandler sessionHandler){
		this.source = source;
		this.sessionHandler = sessionHandler;
		this.conn = conn;
		if(log.isDebugEnabled()) 
			log.debug("Created stream");
	}
	
	public void play(String flvPath){
		if(state==STATE_PAUSED) resume();
		else start(flvPath);
	} 
	
	protected void start(String flvPath){
		
		this.flvPath = flvPath;

		// Grab FLVDecoder
		try {
		
			flvReader = new FLVReader(this.flvPath);
			
			dataChannel = conn.getChannel((byte)conn.getNextAvailableChannelId());
			if(log.isDebugEnabled()) 
				log.debug("data channel: "+dataChannel.getId());
			
			if(flvReader.getHeader().getFlagVideo()){
				videoChannel = conn.getChannel((byte)conn.getNextAvailableChannelId());
				if(log.isDebugEnabled()) 
					log.debug("video channel: "+videoChannel.getId());
			}
			
			if(flvReader.getHeader().getFlagAudio()){
				audioChannel = conn.getChannel((byte)conn.getNextAvailableChannelId());
				if(log.isDebugEnabled()) 
					log.debug("audio channel: "+audioChannel.getId());
			}
						
		} catch(Exception ex){
			log.error(ex);
			return;
		}
		
		
		if(log.isDebugEnabled()) 
			log.debug("Stream setup: "+flvPath);
		
		state = STATE_PLAYING;
		
		int clientid = 1; // random
		
		// This is what john sends, so im sending it back
		String details = "testing"; //flvPath;
		
		sessionHandler.sendRuntimeStatus(videoChannel, StatusObjectService.NS_PLAY_RESET, details, clientid);

		sessionHandler.sendRuntimeStatus(dataChannel, StatusObjectService.NS_PLAY_START, details, clientid);
		
		sessionHandler.sendNotify(videoChannel, StatusObjectService.NS_DATA_START);
				
		// start sending video packets down video channel, not doing this yet
		//for(int i=0; i<10 && hasMorePackets(); i++)
			writeNextPacket();
		//writeNextPacket();
	}
	
	protected void resume(){
		sessionHandler.sendNotify(videoChannel, StatusObjectService.NS_DATA_START);
		
		// start sending video packets down video channel, not doing this yet
		//for(int i=0; i<10 && hasMorePackets(); i++)
			writeNextPacket();
	}
	
	
	public void pause(){
		if(state==STATE_PLAYING){
			state = STATE_PAUSED;
		}
	}

	public void stop(){
		state = STATE_STOPPED;
	}
	
	public void end(){
		state = STATE_END;
		if(audioChannel!=null) {
			dataChannel.close();
			dataChannel = null;
		}
		if(audioChannel!=null) {
			audioChannel.close();
			audioChannel = null;
		}
		if(videoChannel!=null) {
			videoChannel.close();
			videoChannel = null;
		}
	}
	
	public byte getState(){
		return state;
	}
	
	public boolean hasMorePackets(){		
		if(flvReader==null) return false;
		if(state!=STATE_PLAYING) return false;
		if(!flvReader.hasMoreTags()){
			end();
			return false;
		} else {
			return true;
		}
	}
	
	protected int statusPacketID = 16777216;
	
	protected boolean continueTag = false;
	protected FLVTag lastTag = null;
	
	public void writeNextPacket(){
		// Still need to figure out how to create packet
		Packet packet = null;
		try {
			
			if(log.isDebugEnabled())
				log.debug("Send next packet");
			
			FLVTag tag = null;
			
			if(continueTag){
				tag = lastTag;
			} else {
				tag = flvReader.getNextTag();
			}
			
			lastTag = tag;
			
			byte dataType = tag.getDataType();
			
			if(dataType == FLVTag.TYPE_METADATA){
				packet = new Packet(tag.getBody(), tag.getTimestamp(), Packet.TYPE_FUNCTION_CALL_NOREPLY, source);
				dataChannel.writePacket(packet, this);
			}
			else if(dataType == FLVTag.TYPE_VIDEO){
				packet = new Packet(tag.getBody(), tag.getTimestamp(), tag.getDataType(), statusPacketID);
				videoChannel.writePacket(packet, this);
			}
			else if(dataType == FLVTag.TYPE_AUDIO){
				packet = new Packet(tag.getBody(), tag.getTimestamp(), tag.getDataType(), statusPacketID);				
				audioChannel.writePacket(packet, this);
			} else {
				log.error("Unexpected datatype: "+dataType);
				writeNextPacket();
			}
			
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

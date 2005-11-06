package org.red5.server.rtmp.message;

public class VideoData extends Message {
	
	private static final int INITIAL_CAPACITY = 2048;

	public VideoData(){
		super(TYPE_VIDEO_DATA, INITIAL_CAPACITY);
	}
	
}

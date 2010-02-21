package org.red5.server.net.rtmp.event;

/*
 * RED5 Open Source Flash Server - http://www.osflash.org/red5
 * 
 * Copyright (c) 2006-2009 by respective authors (see below). All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or modify it under the 
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * Foundation; either version 2.1 of the License, or (at your option) any later 
 * version. 
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along 
 * with this library; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 */

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Ping event, actually combination of different events. This is
 * also known as a user control message.
 */
public class Ping extends BaseEvent {
	
	private static final long serialVersionUID = -6478248060425544923L;

	/**
	 * Stream begin / clear event
	 */
	public static final short STREAM_BEGIN = 0;

	/**
	 * Stream EOF, playback of requested stream is completed.
	 */
	public static final short STREAM_PLAYBUFFER_CLEAR = 1;

	/**
	 * Stream is empty
	 */
	public static final short STREAM_DRY = 2;

	/**
	 * Client buffer. Sent by client to indicate its buffer time in milliseconds.
	 */
	public static final short CLIENT_BUFFER = 3;

	/**
	 * Recorded stream. Sent by server to indicate a recorded stream.
	 */
	public static final short RECORDED_STREAM = 4;

	/**
	 * One more unknown event
	 */
	public static final short UNKNOWN_5 = 5;

	/**
	 * Client ping event. Sent by server to test if client is reachable.
	 */
	public static final short PING_CLIENT = 6;

	/**
	 * Server response event. A clients ping response.
	 */
	public static final short PONG_SERVER = 7;

    /**
     * One more unknown event
     */
    public static final short UNKNOWN_8 = 8;

    /**
     * Event type is undefined
     */
    public static final int UNDEFINED = -1;

    /**
     * The sub-type
     */
	private short eventType;

	/**
	 * Represents the stream id in all cases except PING_CLIENT and PONG_SERVER
	 * where it represents the local server timestamp.
	 */
	private int value2;

	private int value3 = UNDEFINED;

	private int value4 = UNDEFINED;

    /**
     * Debug string
     */
    private String debug = "";

	/** Constructs a new Ping. */
    public Ping() {
		super(Type.SYSTEM);
	}

    public Ping(short eventType, int value2) {
		super(Type.SYSTEM);
		this.eventType = eventType;
		this.value2 = value2;
	}

	public Ping(short eventType, int value2, int value3) {
		super(Type.SYSTEM);
		this.eventType = eventType;
		this.value2 = value2;
		this.value3 = value3;
	}

	public Ping(short eventType, int value2, int value3, int value4) {
		super(Type.SYSTEM);
		this.eventType = eventType;
		this.value2 = value2;
		this.value3 = value3;
		this.value4 = value4;
	}
	
	public Ping(Ping in) {
		super(Type.SYSTEM);
		this.eventType = in.getEventType();
		this.value2 = in.getValue2();
		this.value3 = in.getValue3();
		this.value4 = in.getValue4();
	}

	/** {@inheritDoc} */
    @Override
	public byte getDataType() {
		return TYPE_PING;
	}

    /**
     * Returns the events sub-type
     * 
     * @return the event type
     */
	public short getEventType() {
		return eventType;
	}

	/**
	 * Sets the events sub-type
	 * 
	 * @param eventType
	 */
	public void setEventType(short eventType) {
		this.eventType = eventType;
	}

	/**
     * Getter for property 'value2'.
     *
     * @return Value for property 'value2'.
     */
    public int getValue2() {
		return value2;
	}

	/**
     * Setter for property 'value2'.
     *
     * @param value2 Value to set for property 'value2'.
     */
    public void setValue2(int value2) {
		this.value2 = value2;
	}

	/**
     * Getter for property 'value3'.
     *
     * @return Value for property 'value3'.
     */
    public int getValue3() {
		return value3;
	}

	/**
     * Setter for property 'value3'.
     *
     * @param value3 Value to set for property 'value3'.
     */
    public void setValue3(int value3) {
		this.value3 = value3;
	}

	/**
     * Getter for property 'value4'.
     *
     * @return Value for property 'value4'.
     */
    public int getValue4() {
		return value4;
	}

	/**
     * Setter for property 'value4'.
     *
     * @param value4 Value to set for property 'value4'.
     */
    public void setValue4(int value4) {
		this.value4 = value4;
	}

	/**
     * Getter for property 'debug'.
     *
     * @return Value for property 'debug'.
     */
    public String getDebug() {
		return debug;
	}

	/**
     * Setter for property 'debug'.
     *
     * @param debug Value to set for property 'debug'.
     */
    public void setDebug(String debug) {
		this.debug = debug;
	}

	protected void doRelease() {
		eventType = 0;
		value2 = 0;
		value3 = UNDEFINED;
		value4 = UNDEFINED;
	}

	/** {@inheritDoc} */
    @Override
	public String toString() {
		return "Ping: " + eventType + ", " + value2 + ", " + value3 + ", "
				+ value4 + "\n" + debug;
	}

	/** {@inheritDoc} */
    @Override
	protected void releaseInternal() {

	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		eventType = in.readShort();
		value2 = in.readInt();
		value3 = in.readInt();
		value4 = in.readInt();
		// XXX do we need to restore "debug" ?
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeShort(eventType);
		out.writeInt(value2);
		out.writeInt(value3);
		out.writeInt(value4);
		// XXX do we need to save "debug" ?
	}
}
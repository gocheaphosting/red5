package org.red5.server.common.rtmp.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.red5.server.common.BufferEx;
import org.red5.server.common.BufferExUtils;
import org.red5.server.common.amf.AMFInput;
import org.red5.server.common.rtmp.RTMPCodecException;
import org.red5.server.common.rtmp.RTMPCodecState;
import org.red5.server.common.rtmp.RTMPInput;
import org.red5.server.common.rtmp.packet.RTMPChunkSize;
import org.red5.server.common.rtmp.packet.RTMPHandshake;
import org.red5.server.common.rtmp.packet.RTMPHeader;
import org.red5.server.common.rtmp.packet.RTMPPacket;

/**
 * Base RTMPInput that demux RTMP buffer into packets.
 * 
 * @author Steven Gong (steven.gong@gmail.com)
 */
public abstract class BaseRTMPInput implements RTMPInput {
	private static final int INTERNAL_BUF_CAPACITY = 1024;
	private static final int SUB_HEADER_SIZE[] = { 11, 7, 3, 0 }; // without channel id
	
	protected ClassLoader defaultClassLoader;
	protected RTMPCodecState codecState;
	protected AMFInput amfInput;
	protected boolean isServerMode;
	protected int chunkSize = 128;
	
	protected Map<Integer,RTMPHeader> lastHeaderMap =
		new HashMap<Integer,RTMPHeader>();
	
	private Map<Integer,RTMPPacketObject> decodingPacketMap =
		new HashMap<Integer,RTMPPacketObject>();
	
	private BufferEx internalBuf;
	private RTMPDecodeState decodeState = new RTMPDecodeState();
	
	public BaseRTMPInput(boolean isServerMode) {
		codecState = RTMPCodecState.HANDSHAKE_1;
		this.isServerMode = isServerMode;
		this.internalBuf = BufferEx.allocate(INTERNAL_BUF_CAPACITY);
		// TODO initialize AMF input
		// this.amfInput = null;
	}

	@Override
	public RTMPCodecState getCodecState() {
		return codecState;
	}

	@Override
	public boolean isServerMode() {
		return isServerMode;
	}

	@Override
	public RTMPPacket read(BufferEx buf, ClassLoader classLoader)
			throws RTMPCodecException {
		int originPos = buf.position();
		try {
			if (codecState == RTMPCodecState.HANDSHAKE_1 ||
					codecState == RTMPCodecState.HANDSHAKE_2) {
				return readHandshake(buf);
			} else {
				RTMPPacket packet = readPacket(buf, classLoader);
				if (packet instanceof RTMPChunkSize) {
					RTMPChunkSize chunkSizePacket = (RTMPChunkSize) packet;
					chunkSize = chunkSizePacket.getChunkSize();
				}
				return packet;
			}
		} catch (RTMPCodecException e) {
			buf.position(originPos);
			throw e;
		}
	}

	@Override
	public RTMPPacket read(BufferEx buf) throws RTMPCodecException {
		return read(buf, getClassLoader());
	}

	@Override
	public RTMPPacket[] readAll(BufferEx buf, ClassLoader classLoader)
			throws RTMPCodecException {
		int originPos = buf.position();
		try {
			ArrayList<RTMPPacket> packets = new ArrayList<RTMPPacket>();
			RTMPPacket packet = read(buf, classLoader);
			while (packet != null) {
				packets.add(packet);
				packet = read(buf, classLoader);
			}
			return (RTMPPacket[]) packets.toArray();
		} catch (RTMPCodecException e) {
			buf.position(originPos);
			throw e;
		}
	}

	@Override
	public RTMPPacket[] readAll(BufferEx buf) throws RTMPCodecException {
		return readAll(buf, getClassLoader());
	}

	@Override
	public void resetInput() {
		internalBuf.clear();
		amfInput.resetInput();
		lastHeaderMap.clear();
		decodingPacketMap.clear();
	}

	@Override
	public void setDefaultClassLoader(ClassLoader defaultClassLoader) {
		this.defaultClassLoader = defaultClassLoader;
	}
	
	protected RTMPHandshake readHandshake(BufferEx buf) throws RTMPCodecException {
		RTMPCodecState nextState;
		int handshakeSize;
		if (codecState == RTMPCodecState.HANDSHAKE_1) {
			nextState = RTMPCodecState.HANDSHAKE_2;
			handshakeSize = RTMPHandshake.HANDSHAKE_SIZE+1;
		} else if (codecState == RTMPCodecState.HANDSHAKE_2) {
			nextState = RTMPCodecState.GENERIC_RTMP;
			handshakeSize = RTMPHandshake.HANDSHAKE_SIZE;
		} else {
			throw new RTMPCodecException("Illegal state reading handshake");
		}
		RTMPHandshake handshake = null;
		int bytesRemaining = internalBuf.position() + buf.remaining();
		if (bytesRemaining >= handshakeSize) {
			byte[] handshakeBuf = new byte[handshakeSize];
			int internalBufLength = internalBuf.position();
			internalBuf.flip();
			internalBuf.get(handshakeBuf, 0, internalBufLength);
			internalBuf.clear();
			buf.get(handshakeBuf, internalBufLength, handshakeSize-internalBufLength);
			handshake = new RTMPHandshake();
			handshake.setHandshakeData(BufferEx.wrap(handshakeBuf));
			codecState = nextState;
			return handshake;
		} else {
			internalBuf.put(buf);
		}
		return handshake;
	}
	
	protected RTMPPacket readPacket(BufferEx buf, ClassLoader classLoader)
	throws RTMPCodecException {
		if (codecState != RTMPCodecState.GENERIC_RTMP) {
			throw new RTMPCodecException("Illegal state reading generic packet");
		}
		RTMPPacketObject packetObject = readChunk(buf);
		while (packetObject != null) {
			if (packetObject.isCompleteDecoding()) {
				packetObject.body.flip();
				return decodePacket(packetObject.header, packetObject.body, classLoader);
			} else {
				packetObject = readChunk(buf);
			}
		}
		return null;
	}
	
	protected abstract RTMPPacket decodePacket(
			RTMPHeader header, BufferEx body, ClassLoader classLoader)
	throws RTMPCodecException;
	
	private RTMPPacketObject readChunk(BufferEx buf) {
		if (!okToReadChunk(buf)) {
			return null;
		}
		internalBuf.flip();
		internalBuf.position(decodeState.channelIdSize +
				SUB_HEADER_SIZE[decodeState.headerType]);
		RTMPPacketObject decodingPacket =
			decodingPacketMap.get(decodeState.channelId);
		// read remaining bytes in the chunk
		decodingPacket.body.put(internalBuf);
		if (decodingPacket.remaining() == 0) {
			decodingPacketMap.remove(decodeState.channelId);
			lastHeaderMap.put(decodeState.channelId, decodingPacket.header);
			return decodingPacket;
		} else {
			internalBuf.clear();
			return null;
		}
	}
	
	private boolean okToReadChunk(BufferEx buf) {
		if (buf.remaining() < decodeState.bytesNeeded) {
			decodeState.bytesNeeded -= buf.remaining();
			internalBuf.put(buf);
			return false;
		}
		boolean continueDecoding = true;
		int originInternalBufPos;
		while (continueDecoding) {
			BufferExUtils.getBufferByLength(internalBuf, buf,
					decodeState.bytesNeeded);
			originInternalBufPos = internalBuf.position();
			internalBuf.flip();
			switch (decodeState.state) {
			case RTMPDecodeState.DECODE_STATE_INIT:
				byte channelByte0 = internalBuf.get();
				decodeState.channelIdSize = 1;
				if ((channelByte0 & 0x3f) == 0) {
					decodeState.channelIdSize = 2;
				} else if ((channelByte0 & 0x3f) == 1) {
					decodeState.channelIdSize = 3;
				}
				decodeState.headerType = (channelByte0 & 0x3) >> 6;
				int headerSize = decodeState.channelIdSize +
					SUB_HEADER_SIZE[decodeState.headerType];
				decodeState.bytesNeeded = headerSize - 1;
				break;
			case RTMPDecodeState.DECODE_STATE_NEED_HEADER:
				channelByte0 = internalBuf.get();
				switch (decodeState.channelIdSize) {
				case 1:
					decodeState.channelId = channelByte0 & 0x3f;
					break;
				case 2:
					decodeState.channelId = 64 + (internalBuf.get() & 0x0ff);
					break;
				case 3:
				default:
					byte channelByte1, channelByte2;
					channelByte1 = internalBuf.get();
					channelByte2 = internalBuf.get();
					decodeState.channelId = 64 + (channelByte1 & 0x0ff) + ((channelByte2 & 0x0ff) << 8);
					break;
				}

				RTMPHeader lastHeader = lastHeaderMap.get(decodeState.channelId);
				RTMPPacketObject decodingPacket = decodingPacketMap.get(decodeState.channelId);
				if (decodeState.headerType != 0 && lastHeader == null) {
					throw new RTMPCodecException("Last header not found parsing headerType " + decodeState.headerType);
				}
				if (decodeState.headerType != 3 && decodingPacket != null) {
					throw new RTMPCodecException("Got non-continue header type with existing decoding packet");
				}
				if (decodingPacket == null) {
					long timestamp;
					int relativeTS = 0;
					int size;
					int type;
					int streamId;
					switch (decodeState.headerType) {
					case 0:
						timestamp = BufferExUtils.readMediumIntBE(internalBuf);
						size = BufferExUtils.readMediumIntBE(internalBuf);
						type = internalBuf.get() & 0x0ff;
						streamId = BufferExUtils.readMediumIntLE(internalBuf);
						break;
					case 1:
						relativeTS = BufferExUtils.readMediumIntBE(internalBuf);
						timestamp = lastHeader.getTimestamp() + relativeTS;
						size = BufferExUtils.readMediumIntBE(internalBuf);
						type = internalBuf.get() & 0x0ff;
						streamId = lastHeader.getStreamId();
						break;
					case 2:
						relativeTS = BufferExUtils.readMediumIntBE(internalBuf);
						timestamp = lastHeader.getTimestamp() + relativeTS;
						size = lastHeader.getSize();
						type = lastHeader.getSize();
						streamId = lastHeader.getStreamId();
						break;
					case 3:
						relativeTS = lastHeader.getRelativeTS();
						timestamp = lastHeader.getTimestamp() + relativeTS;
						size = lastHeader.getSize();
						type = lastHeader.getSize();
						streamId = lastHeader.getStreamId();
						break;
					default:
						// impossible value
						throw new RTMPCodecException("Impossible code path");
					}
					RTMPHeader currentHeader = new RTMPHeader();
					currentHeader.setChannel(decodeState.channelId);
					currentHeader.setRelativeTS(relativeTS);
					currentHeader.setTimestamp(timestamp);
					currentHeader.setSize(size);
					currentHeader.setType(type);
					currentHeader.setStreamId(streamId);
					decodingPacket = new RTMPPacketObject(currentHeader);
					decodingPacketMap.put(decodeState.channelId, decodingPacket);
				}
				decodeState.bytesNeeded =
					(decodingPacket.header.getSize() - decodingPacket.body.position()) %
					chunkSize;
				break;
			case RTMPDecodeState.DECODE_STATE_NEED_BODY:
				continueDecoding = false;
				break;
			default:
				// impossible state
				throw new RTMPCodecException(
						"Impossible code path for decode state " + decodeState.state);
			}
			decodeState.nextState();
			internalBuf.limit(internalBuf.capacity());
			internalBuf.position(originInternalBufPos);
			
			if (continueDecoding && buf.remaining() < decodeState.bytesNeeded) {
				decodeState.bytesNeeded -= buf.remaining();
				internalBuf.put(buf);
				return false;
			}
		}
		return true;
	}
	
	private ClassLoader getClassLoader() {
		if (defaultClassLoader != null) {
			return defaultClassLoader;
		} else {
			return Thread.currentThread().getContextClassLoader();
		}
	}
	
	private class RTMPPacketObject {
		public RTMPHeader header;
		public BufferEx body;
		
		public RTMPPacketObject(RTMPHeader header) {
			this.header = header;
			body = BufferEx.allocate(header.getSize());
			body.setAutoExpand(false);
		}
		
		public boolean isCompleteDecoding() {
			if (body.position() == header.getSize()) {
				return true;
			} else {
				return false;
			}
		}
		
		public int remaining() {
			int bodyPos = body.position();
			return header.getSize() - bodyPos;
		}
	}
	
	private class RTMPDecodeState {
		private static final int DECODE_STATE_INIT           = 0;
		private static final int DECODE_STATE_NEED_HEADER    = 1;
		private static final int DECODE_STATE_NEED_BODY      = 2;
		private static final int DECODE_STATE_MAX            = 3;
		
		public int state;
		public int channelIdSize;
		public int channelId;
		public int headerType;
		
		public int bytesNeeded;
		
		public RTMPDecodeState() {
			this.reset();
		}
		
		public void reset() {
			this.state = DECODE_STATE_INIT;
			this.bytesNeeded = 1;
		}
		
		public void nextState() {
			this.state++;
			this.state %= DECODE_STATE_MAX;
			if (this.state == DECODE_STATE_INIT) {
				reset();
			}
		}
	}
}

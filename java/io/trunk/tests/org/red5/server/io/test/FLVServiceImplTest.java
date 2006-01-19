package org.red5.server.io.test;

/*
 * RED5 Open Source Flash Server - http://www.osflash.org/red5
 * 
 * Copyright � 2006 by respective authors. All rights reserved.
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
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Dominick Accattato (daccattato@gmail.com)
 * @author Luke Hubbard, Codegent Ltd (luke@codegent.com)
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.mina.common.ByteBuffer;
import org.red5.io.amf.Output;
import org.red5.io.flv.FLV;
import org.red5.io.flv.FLVImpl;
import org.red5.io.flv.FLVService;
import org.red5.io.flv.FLVServiceImpl;
import org.red5.io.flv.MetaDataImpl;
import org.red5.io.flv.Reader;
import org.red5.io.flv.Tag;
import org.red5.io.flv.Writer;
import org.red5.io.object.Deserializer;
import org.red5.io.object.Serializer;
import org.red5.io.utils.IOUtils;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * A FLVServiceImpl TestCase
 * 
 * @author The Red5 Project (red5@osflash.org)
 * @author Dominick Accattato (daccattato@gmail.com)
 * @version 0.3
 */
public class FLVServiceImplTest extends TestCase {
	private FLVService service;
	
	/**
	 * SetUp is called before each test
	 * @return void
	 */
	public void setUp() {
		service = new FLVServiceImpl();
		service.setSerializer(new Serializer());
		service.setDeserializer(new Deserializer());
	}

	/**
	 * Tests: getFlv(String s)
	 * @return void
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void testFLVString() throws FileNotFoundException, IOException  {
		FLV flv = service.getFLV("tests/test_cue.flv");		
		Reader reader = flv.reader();
		Tag tag = null;
		
		while(reader.hasMoreTags()) {
			tag = reader.readTag();
			//printTag(tag);
		}
		
		// simply tests to see if the last tag of the flv file
		// has a timestamp of 2500
		Assert.assertEquals(4166,tag.getTimestamp());
		//Assert.assertEquals(true,true);
	}
	
	private void printTag(Tag tag) {
		System.out.println("tag:\n-------\n" + tag);
	}

	/**
	 * Tests: getFLVFile(File f)
	 * @return void
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void testFLVFile() throws FileNotFoundException, IOException  {
		File f = new File("tests/test_cue.flv");
		System.out.println("test: " + f);
		FLV flv = service.getFLV(f);	
		System.out.println("test: " + flv);
		Reader reader = flv.reader();
		System.out.println("test: " + reader);
		Tag tag = null;
		System.out.println("test: " + reader.hasMoreTags());
		while(reader.hasMoreTags()) {
			tag = reader.readTag();
			//System.out.println("test: " + f);
			//printTag(tag);
		}
		
		// simply tests to see if the last tag of the flv file
		// has a timestamp of 2500
		Assert.assertEquals(4166,tag.getTimestamp());
	}
	
	/**
	 * Tests: getFLVFileInputStream(FileInputStream fis)
	 * @return void
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void testFLVFileInputStream() throws FileNotFoundException, IOException  {
		File f = new File("tests/test_cue.flv");
		FileInputStream fis = new FileInputStream(f);
		FLV flv = service.getFLV(fis);
		Reader reader = flv.reader();
		Tag tag = null;
		
		while(reader.hasMoreTags()) {
			tag = reader.readTag();
			//printTag(tag);
		}

		// simply tests to see if the last tag of the flv file
		// has a timestamp of 2500
		Assert.assertEquals(4166,tag.getTimestamp());	
	}
	
	public void testWriteFLVFileOutputStream() throws IOException {
		File f = new File("tests/test_cue2.flv");
		
		if(f.exists()) {			
			f.delete();
		}
		
		// Create new file
		f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		//fos.write((byte)0x01);
		FLV flv = service.getFLV(fos);
		Writer writer = flv.writer();
		
		// Create a reader for testing
		File readfile = new File("tests/test_cue.flv");
		FileInputStream fis = new FileInputStream(readfile);
		FLV readflv = service.getFLV(fis);
		Reader reader = readflv.reader();
		
		writeTags(reader, writer);		
		
		// Currently asserts to true.  I just wanted to see
		// if the method threw an exception
		Assert.assertEquals(true, true);
	}
	
	private void writeTags(Reader reader, Writer writer) throws IOException {
		
		Tag tag = null;
		
		while(reader.hasMoreTags()) {
			tag = reader.readTag();
			writer.writeTag(tag);
			//printTag(tag);
		}
		
	}

	public void testWriteFLVString() {
		
	}
	
	public void testWriteFLVFile() {
		
	}
	
	public void testMetaDataInjection() throws IOException {
		File f = new File("tests/test_cue2.flv");
		
		if(f.exists()) {			
			f.delete();
		}
		
		// Create new file
		f.createNewFile();
		FileOutputStream fos = new FileOutputStream(f);
		//fos.write((byte)0x01);
		FLV flv = service.getFLV(fos);
		Writer writer = flv.writer();
		
		// Create a reader for testing
		File readfile = new File("tests/test_cue.flv");
		FileInputStream fis = new FileInputStream(readfile);
		FLV readflv = service.getFLV(fis);
		Reader reader = readflv.reader();
		
		writeTagsWithInjection(reader, writer);		
	}

	
	private void writeTagsWithInjection(Reader reader, Writer writer) throws IOException {
		Tag tag = null;
		
		MetaDataImpl mdi1 = new MetaDataImpl("mdi1");
		mdi1.setTimestamp(100);
		mdi1.put("test1", "test1");
		
		MetaDataImpl mdi2 = new MetaDataImpl("mdi2");
		mdi2.setTimestamp(50);
		mdi1.put("test2", "test2");
		
		MetaDataImpl mdi3 = new MetaDataImpl("mdi3");
		mdi3.setTimestamp(0);
		mdi1.put("test3", "test3");
		
		// Place in Treeset for sorting
		TreeSet ts = new TreeSet();
		ts.add(mdi1);
		ts.add(mdi2);
		ts.add(mdi3);
						
		System.out.println("ts: " + ts);
		int tmpTimeStamp = ((MetaDataImpl) ts.first()).getTimestamp();
		
		while(reader.hasMoreTags()) {
			tag = reader.readTag();
			
			if(!ts.isEmpty()) {
				if(tag.getTimestamp() > tmpTimeStamp) {
					injectMetaData(ts.first(), writer, tag);
					ts.remove(ts.first());
				}
			}
			writer.writeTag(tag);
			//printTag(tag);
		}
		
	}
		
	private void injectMetaData(Object mdi, Writer writer, Tag tag) {
		System.out.println("in inject");
		
		ByteBuffer buf = ByteBuffer.allocate(1000);
		Output out = new Output(buf);
		Serializer ser = new Serializer();
		
//		 PreviousTagSize
		//out = out.reset();
		buf.clear();
		buf.putInt(tag.getPreviousTagSize());
		
		// Data Type
		buf.put((byte)(Tag.TYPE_METADATA));
		
		// Body Size
		IOUtils.writeMediumInt(buf, tag.getBodySize());
		
		// Timestamp
		IOUtils.writeMediumInt(buf, tag.getTimestamp());
		
		// Reserved
		buf.putInt(0x00);

		buf.flip();
		//bytesWritten += channel.write(out.buf());

		ByteBuffer bodyBuf = tag.getBody();
		//bytesWritten += channel.write(bodyBuf.buf());
		
		
		ser.serialize(out, new Object());
		/*
		 * Pseudo code
		 * 
		 * 
		 */
	}

	public void testMetaDataOrder() {
		MetaDataImpl mdi1 = new MetaDataImpl("mdi1");
		mdi1.setTimestamp(100);
		
		MetaDataImpl mdi2 = new MetaDataImpl("mdi2");
		mdi2.setTimestamp(50);
		
		MetaDataImpl mdi3 = new MetaDataImpl("mdi3");
		mdi3.setTimestamp(0);
		
		TreeSet ts = new TreeSet();
		ts.add(mdi1);
		ts.add(mdi2);
		ts.add(mdi3);
		
		System.out.println("ts: " + ts);
		
		Assert.assertEquals(true, true);
	}
}

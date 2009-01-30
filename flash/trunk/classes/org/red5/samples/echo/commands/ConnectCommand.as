﻿package org.red5.samples.echo.commands
{	
	/**
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
	 
	import com.adobe.cairngorm.commands.ICommand;
	import com.adobe.cairngorm.control.CairngormEvent;
	
	import mx.rpc.remoting.RemoteObject;
	
	import org.red5.samples.echo.events.ConnectEvent;
	import org.red5.samples.echo.events.PrintTextEvent;
	import org.red5.samples.echo.events.StartTestsEvent;
	import org.red5.samples.echo.model.ModelLocator;
	
	/**
	 * @author Thijs Triemstra (info@collab.nl)
	 */	
	public class ConnectCommand implements ICommand 
	{	
		private var model 		: ModelLocator = ModelLocator.getInstance();
		private var url			: String;
		private var protocol	: String;
		private var encoding	: uint;
		
	 	/**
	 	 * @param cgEvent
	 	 */	 	
	 	public function execute(cgEvent:CairngormEvent):void
	    { 
	    	var flushStatus		: String = null;
	    	var startTestsEvent	: StartTestsEvent = new StartTestsEvent();
			var event			: ConnectEvent = ConnectEvent(cgEvent);
			
			if (model.nc.connected) {
				model.nc.close();
			}
			
			protocol = event.protocol;
			encoding = event.encoding;
			model.nc.objectEncoding = encoding;
			
			if (protocol == "http") {
			    // Remoting...
				url = model.httpServer;
				model.local_so.data.httpUri = url;
			}
			else
			{
				// RTMP...
				url = model.rtmpServer;
				model.local_so.data.rtmpUri = url;
			}
            
            model.statusText = "Connecting through <b>" + protocol.toUpperCase() + "</b> using <b>AMF" 
                         		+ encoding  + "</b> encoding...";
            try {
                flushStatus = model.local_so.flush();
            } 
            catch (error:Error) 
            {
            	var printTextEvent:PrintTextEvent = new PrintTextEvent("<br/><b>" + model.failure +
            										"Local SharedObject error: </font></b>" +
            										error.getStackTrace() + "<br/>");
				printTextEvent.dispatch();
            }
            
			if (protocol == "remoteObject") 
			{
				// Setup a RemoteObject
            	model.echoService = new RemoteObject("Red5Echo");
            	model.echoService.source = "EchoService";
            	
            	// echoService.addEventListener( ResultEvent.RESULT, onRem );
            	
				if (model.user.userid.length > 0) {
					// test credentials feature
					model.echoService.setCredentials(model.user.userid, model.user.password);
					model.statusText += " ( using setCredentials )";
				}
				model.statusText += "...";
				
				startTestsEvent.dispatch();
				// ignore rest of setup logic
				return;
			}
			else if (protocol == "sharedObject")
			{
				
			}
			
			if (model.user.userid.length > 0) {
				// test credentials feature
				model.nc.addHeader("Credentials", false, Object(model.user));
				model.statusText += " ( using setCredentials )";
			}

			model.statusText += "...";
			if (model.echoService != null) {
				// http_txt.text
				model.echoService.destination = null;
			}
			// connect to server
			model.nc.connect(url);
			//			
			if ( protocol == "http" ) {
				// Don't wait for a successfull connection for AMF0/AMF3 remoting tests.
				startTestsEvent.dispatch();
			}
			
			model.connecting = true;
			trace(url);
		}
		
	}
}
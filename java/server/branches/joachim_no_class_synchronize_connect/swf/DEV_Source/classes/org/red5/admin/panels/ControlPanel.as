﻿package org.red5.admin.panels{	/**	 * RED5 Open Source Flash Server - http://www.osflash.org/red5	 *	 * Copyright (c) 2006-2007 by respective authors (see below). All rights reserved.	 *	 * This library is free software; you can redistribute it and/or modify it under the	 * terms of the GNU Lesser General Public License as published by the Free Software	 * Foundation; either version 2.1 of the License, or (at your option) any later	 * version.	 *	 * This library is distributed in the hope that it will be useful, but WITHOUT ANY	 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A	 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.	 *	 * You should have received a copy of the GNU Lesser General Public License along	 * with this library; if not, write to the Free Software Foundation, Inc.,	 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA	*/		import flash.events.*;	import flash.net.*;	import flash.system.*;	import flash.utils.Timer;		import mx.core.Application;	import mx.containers.*;	import mx.controls.*;	import mx.events.*;		import org.red5.admin.connector.Red5Connector;	import org.red5.admin.connector.event.Red5Event;	import org.red5.admin.panels.Login;	import org.red5.utils.SharedObjectHandler;		/**	 * 	 * @author Martijn van Beek	 */		public class ControlPanel extends Application	{		[Bindable]		public var _footer:String;		[Bindable]		private var _error:String = "";		[Bindable]		public var _intervals:Array;		[Bindable]		private var _hosts:Array;		[Bindable]		public var _applications:Array;		[Bindable]		private var _scopes:Array;		[Bindable]		public var _users:Array;		[Bindable]		private var _streams:Array;		[Bindable]		public var _userstats:String;		[Bindable]		public var _selectedHost:String;		[Bindable]		public var _scope_stats:Array;		[Bindable]		public var _user_stats:Array;		private var _connector:Red5Connector;		private var sharedObject:SharedObjectHandler;		private var _selectedApp:String		private var _interval:Timer = null;		private var _selectedUser:Number		// Components				public var loggedInInfo : HBox;				public var intervalSpeed : ComboBox;				public var loginPanel : Login;				public var statsTab : TabNavigator;				public var flowControll : ViewStack;				public var appList : DataGrid;				public var userList : List;				public var scopeList : List;				public var streamList : List;				public var btn_kill : Button;				public function connect (): void 		{			_footer = "VMVersion: " + System.vmVersion + " | Flash Player: " + Capabilities.version;			loggedInInfo.visible = false;			_intervals = [1,5,10,20,30,60]			_connector = Red5Connector.getInstance();			_connector.connectServer()		}		public function initLogin ( event : Event ) : void 		{			loginPanel.addEventListener(Login.CONNECTED,startApp);			loginPanel.addEventListener(Login.CONNECTING,connectingRed5);			loginPanel.addEventListener(Login.FAILED,failedConnection);		}		private function connectingRed5 ( event : Red5Event) : void 		{			flowControll.selectedIndex = 1		}		private function startApp ( event : Red5Event) : void 		{			loggedInInfo.visible = true;			flowControll.selectedIndex = 2			loadApplications()			speedChange ( new ListEvent(ListEvent.CHANGE ) );			_selectedHost = loginPanel.address.text		}		private function loadApplications() : void 		{			var responder:Responder = new Responder(fillApplications,null);			_connector.call ( "getApplications" , responder );		}		private function selectApplication ( event:ListEvent ) : void 		{			_selectedApp = _applications[appList.selectedIndex].name;			trace ( _selectedApp );			var responder:Responder = new Responder(fillScopes,null);			_connector.call ( "getScopes" , responder , _selectedApp );			var responder2:Responder = new Responder(fillUsers,null);			_connector.call ( "getConnections" , responder2 , _selectedApp );			getAppStats();		}		public function fillScopes( scopes : Array ):void		{			_scopes = scopes;		}		public function fillUsers(apps:Array):void		{			_users = apps;			if ( _selectedUser > apps.length && _selectedUser < apps.length ) {				userList.selectedIndex = _selectedUser			} 		}		public function fillApplications(apps:Array):void		{			_applications = apps;			if ( _selectedApp != null ) {				for (var i:Number = 0 ; i < _applications.length ; i++ ) {					if ( _applications[i].name == _selectedApp ) {						appList.selectedIndex = i;					}				}			}		}		private function speedChange(event:ListEvent):void		{			if ( _interval != null ) {				_interval.stop()				_interval = null;			}			_interval = new Timer( _intervals[intervalSpeed.selectedIndex] * 1000 );			_interval.addEventListener(TimerEvent.TIMER,refreshData);			_interval.start()		}		private function refreshData(event:TimerEvent):void		{			loadApplications()			switch ( statsTab.selectedIndex ) {				case 0:					if ( appList.selectedIndex >= 0 ) {						selectApplication ( new ListEvent(ListEvent.CHANGE) );					}					break;				case 1:					if ( userList.selectedIndex >= 0 ) {						selectUser ( new ListEvent(ListEvent.CHANGE) );					}					break;			}		}				public function addAppListeners():void 		{			appList.addEventListener(ListEvent.CHANGE,selectApplication);			intervalSpeed.addEventListener(ListEvent.CHANGE,speedChange);			}		public function addUserListeners():void 		{			userList.addEventListener(ListEvent.CHANGE,selectUser);		}		private function selectUser ( event:ListEvent ) :void 		{			var responder2:Responder = new Responder( showUserStatistics ,null);			_connector.call ( "getUserStatistics" , responder2 , _users[userList.selectedIndex] );			_selectedUser = userList.selectedIndex		}				public function killUser () : void 		{			_connector.call ( "killUser" , null , _users[userList.selectedIndex] );		}		private function failedConnection ( event : Red5Event ) : void 		{			flowControll.selectedIndex = 0;			loggedInInfo.visible = false;		}		private function getAppStats():void		{			var name:String = _applications[appList.selectedIndex].name;			var responder:Responder = new Responder(showStatistics,null);			_connector.call ( "getStatistics" , responder , name );		}		/*		private function getScopeStats():void		{			var responder:Responder = new Responder(showStatistics,null);			_connector.call ( "getStatistics" , responder , _scopes[scopeList.selectedIndex] );		}*/			private function showStatistics(data:Array):void		{			_scope_stats = data		}		private function showUserStatistics(data:Array):void		{			_user_stats = data;		}		public function logout(event:MouseEvent):void		{			_connector.close()			flowControll.selectedIndex = 0;		}			}}
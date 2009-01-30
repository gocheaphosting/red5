﻿package org.red5.samples.echo.model.tests
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

	/**
	 * @author Thijs Triemstra (info@collab.nl)
	*/
	public class NumberTest extends BaseTest
	{
		public function NumberTest()
		{
			super();
			
			tests.push(0);
			tests.push(1);
			tests.push(-1);
			tests.push(256);
			tests.push(-256);
			tests.push(65536);
			tests.push(-65536);
			tests.push(Number.NaN);
			//tests.push(Number.NEGATIVE_INFINITY);
			//tests.push(Number.POSITIVE_INFINITY);
			tests.push(Number.MAX_VALUE);
			tests.push(Number.MIN_VALUE);
			tests.push(0.0);
			tests.push(1.5);
			tests.push(-1.5);
			tests.push(uint(0x000000));
		}
		
	}
}
/*
 *   @(#) $Id: SessionManager.java 327113 2005-10-21 06:59:15Z trustin $
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
package org.apache.mina.common;

/**
 * Base interface for all acceptors and connectors that manage
 * sessions.
 * <p>
 * You can monitor any uncaught exceptions by setting {@link ExceptionMonitor}
 * by calling {@link #setExceptionMonitor(ExceptionMonitor)}.  The default
 * monitor is {@link DefaultExceptionMonitor}.
 * 
 * @author The Apache Directory Project (dev@directory.apache.org)
 * @version $Rev: 327113 $, $Date: 2005-10-21 15:59:15 +0900 $
 */
public interface SessionManager {
    
    /**
     * Returns the current exception monitor.
     */
    ExceptionMonitor getExceptionMonitor();

    /**
     * Sets the uncaught exception monitor.  If <code>null</code> is specified,
     * a new instance of {@link DefaultExceptionMonitor} will be set.
     * 
     * @param monitor A new instance of {@link DefaultExceptionMonitor} is set
     *                if <tt>null</tt> is specified.
     */
    void setExceptionMonitor( ExceptionMonitor monitor );
}

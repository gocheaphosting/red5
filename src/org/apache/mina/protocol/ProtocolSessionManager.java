/*
 *   @(#) $Id: ProtocolSessionManager.java 264677 2005-08-30 02:44:35Z trustin $
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
package org.apache.mina.protocol;

import org.apache.mina.common.SessionManager;

/**
 * A {@link SessionManager} for Protocol layer.
 * <p>
 * {@link ProtocolFilter}s can be added and removed at any time to filter
 * events just like Servlet filters and they are effective immediately.
 *
 * @author The Apache Directory Project (dev@directory.apache.org)
 * @author Trustin Lee (trustin@apache.org)
 * @version $Rev: 264677 $, $Date: 2005-08-30 11:44:35 +0900 $
 */
public interface ProtocolSessionManager extends SessionManager {
    /**
     * Returns the filter chain that filters all events which is related
     * with sessions this manager manages.
     */
    ProtocolFilterChain getFilterChain();
}

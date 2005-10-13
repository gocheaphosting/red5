/*
 *   @(#) $Id: Event.java 264677 2005-08-30 02:44:35Z trustin $
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
package org.apache.mina.util;

/**
 * MINA Event used by {@link BaseThreadPool} internally.
 * 
 * @author The Apache Directory Project (dev@directory.apache.org)
 * @author Trustin Lee (trustin@apache.org)
 * @version $Rev: 264677 $, $Date: 2005-08-30 11:44:35 +0900 $
 */
class Event
{
    private final EventType type;
    private final Object nextFilter;
    private final Object data;
    
    public Event( EventType type, Object nextFilter, Object data )
    {
        this.type = type;
        this.nextFilter = nextFilter;
        this.data = data;
    }

    public Object getData()
    {
        return data;
    }
    

    public Object getNextFilter()
    {
        return nextFilter;
    }
    

    public EventType getType()
    {
        return type;
    }
}
/*
 * Copyright (c) 2009-2016 Dmytro Pishchukhin (http://knowhowlab.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.knowhowlab.osgi.monitoradmin;

/**
 * Constants
 *
 * @author dmytro.pishchukhin
 */
interface ConstantsMonitorAdmin {
    /**
     * <code>MonitorAdmin</code> events topic
     */
    String TOPIC = "org/osgi/service/monitor";
    /**
     * <code>Monitorable</code> ID
     */
    String MON_MONITORABLE_PID = "mon.monitorable.pid";
    /**
     * <code>StatusVariable</code> name
     */
    String MON_STATUSVARIABLE_NAME = "mon.statusvariable.name";
    /**
     * <code>StatusVariable</code> value
     */
    String MON_STATUSVARIABLE_VALUE = "mon.statusvariable.value";
    /**
     * Initiator
     */
    String MON_LISTENER_ID = "mon.listener.id";
}

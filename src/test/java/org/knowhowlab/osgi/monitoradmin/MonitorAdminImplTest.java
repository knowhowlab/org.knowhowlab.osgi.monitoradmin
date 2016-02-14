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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.knowhowlab.osgi.monitoradmin.mocks.MockLogVisitor;
import org.knowhowlab.osgi.monitoradmin.mocks.MockMonitorable;
import org.knowhowlab.osgi.monitoradmin.mocks.MockOsgiVisitor;
import org.knowhowlab.osgi.monitoradmin.mocks.MonitorableMockServiceReference;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.monitor.MonitorAdmin;
import org.osgi.service.monitor.Monitorable;
import org.osgi.service.monitor.MonitoringJob;
import org.osgi.service.monitor.StatusVariable;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author dmytro.pishchukhin
 */
public class MonitorAdminImplTest {
    private MockOsgiVisitor osgiVisitor;
    private MockLogVisitor logVisitor;
    private MonitorAdminCommon common;
    private Bundle bundle = null;

    @Before
    public void init() {
        osgiVisitor = new MockOsgiVisitor();
        logVisitor = new MockLogVisitor();
        common = new MonitorAdminCommon(osgiVisitor, logVisitor);
    }

    @After
    public void uninit() {
        osgiVisitor.cleanPostedEvents();
        if (common != null) {
            common.cancelAllJobs();
        }
    }

    @Test
    public void testGetMonitorableNames_NoMonitorableAvailable() throws Exception {
        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, bundle);
        String[] monitorableNames = monitorAdmin.getMonitorableNames();
        Assert.assertNotNull(monitorableNames);
        Assert.assertEquals(0, monitorableNames.length);
    }

    @Test
    public void testGetMonitorableNames_MonitorableAvailable() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();
        map.put(new MonitorableMockServiceReference("com.acme.pid2"), new MockMonitorable());
        map.put(new MonitorableMockServiceReference("com.acme.pid3"), new MockMonitorable());
        map.put(new MonitorableMockServiceReference("com.acme.pid1"), new MockMonitorable());
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, bundle);

        String[] monitorableNames = monitorAdmin.getMonitorableNames();
        Assert.assertNotNull(monitorableNames);
        Assert.assertEquals(3, monitorableNames.length);
        Assert.assertEquals("com.acme.pid1", monitorableNames[0]);
        Assert.assertEquals("com.acme.pid2", monitorableNames[1]);
        Assert.assertEquals("com.acme.pid3", monitorableNames[2]);
    }

    @Test
    public void testGetMonitorableNames_MonitorableAvailable_InvalidId() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();
        map.put(new MonitorableMockServiceReference("com.acme.pid2.very.long.monitorable.id"), new MockMonitorable());
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, bundle);

        String[] monitorableNames = monitorAdmin.getMonitorableNames();
        Assert.assertNotNull(monitorableNames);
        Assert.assertEquals(0, monitorableNames.length);
    }

    @Test
    public void testGetStatusVariable_Valid() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable();

        StatusVariable[] statusVariables = {new StatusVariable("sv.id", StatusVariable.CM_CC, 0)};
        monitorable.setStatusVariables(statusVariables);

        map.put(new MonitorableMockServiceReference("com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, bundle);

        StatusVariable sv = monitorAdmin.getStatusVariable("com.acme.pid/sv.id");
        Assert.assertNotNull(sv);
        Assert.assertEquals("sv.id", sv.getID());
        Assert.assertEquals(StatusVariable.CM_CC, sv.getCollectionMethod());
        Assert.assertEquals(StatusVariable.TYPE_INTEGER, sv.getType());
        Assert.assertEquals(0, sv.getInteger());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStatusVariable_Invalid1() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForGetStatusVariableTests();

        monitorAdmin.getStatusVariable(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStatusVariable_Invalid2() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForGetStatusVariableTests();

        monitorAdmin.getStatusVariable("/&%(/=");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStatusVariable_Invalid3() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForGetStatusVariableTests();

        monitorAdmin.getStatusVariable("com.aaa/sv.id");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStatusVariable_Invalid4() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForGetStatusVariableTests();

        monitorAdmin.getStatusVariable("com.acme.pid/sv.id_u");
    }

    private MonitorAdmin prepareMonitorAdminForGetStatusVariableTests() {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable();

        StatusVariable[] statusVariables = {new StatusVariable("sv.id", StatusVariable.CM_CC, 0)};
        monitorable.setStatusVariables(statusVariables);

        map.put(new MonitorableMockServiceReference("com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        return new MonitorAdminImpl(logVisitor, common, bundle);
    }

    @Test
    public void testGetDescription_Valid() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable();

        StatusVariable[] statusVariables = {new StatusVariable("sv.id", StatusVariable.CM_CC, 0)};
        monitorable.setStatusVariables(statusVariables);

        map.put(new MonitorableMockServiceReference("com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, bundle);

        String description = monitorAdmin.getDescription("com.acme.pid/sv.id");
        Assert.assertNotNull(description);
        Assert.assertEquals("sv.id", description);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDescription_Invalid1() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForGetDescriptionTests();

        monitorAdmin.getDescription(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDescription_Invalid2() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForGetDescriptionTests();

        monitorAdmin.getDescription("/&%(/=");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDescription_Invalid3() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForGetDescriptionTests();

        monitorAdmin.getDescription("com.aaa/sv.id");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetDescription_Invalid4() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForGetDescriptionTests();

        monitorAdmin.getDescription("com.acme.pid/sv.id_u");
    }

    private MonitorAdmin prepareMonitorAdminForGetDescriptionTests() {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable();

        StatusVariable[] statusVariables = {new StatusVariable("sv.id", StatusVariable.CM_CC, 0)};
        monitorable.setStatusVariables(statusVariables);

        map.put(new MonitorableMockServiceReference("com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        return new MonitorAdminImpl(logVisitor, common, bundle);
    }

    @Test
    public void testGetStatusVariables_Valid() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable();

        StatusVariable[] statusVariables = {
            new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
            new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        };
        monitorable.setStatusVariables(statusVariables);

        map.put(new MonitorableMockServiceReference("com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, bundle);

        StatusVariable[] variables = monitorAdmin.getStatusVariables("com.acme.pid");
        Assert.assertNotNull(variables);
        Assert.assertEquals(2, variables.length);
        Assert.assertTrue("sv.id1".equals(variables[0].getID()));
        Assert.assertTrue("sv.id2".equals(variables[1].getID()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStatusVariables_Invalid1() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForGetStatusVariablesTests();

        monitorAdmin.getStatusVariables(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStatusVariables_Invalid2() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForGetStatusVariablesTests();

        monitorAdmin.getStatusVariables("/&%(/=");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStatusVariables_Invalid3() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForGetStatusVariablesTests();

        monitorAdmin.getStatusVariables("com.aaa");
    }

    private MonitorAdmin prepareMonitorAdminForGetStatusVariablesTests() {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable();

        StatusVariable[] statusVariables = {
            new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
            new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        };
        monitorable.setStatusVariables(statusVariables);

        map.put(new MonitorableMockServiceReference("com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        return new MonitorAdminImpl(logVisitor, common, bundle);
    }

    @Test
    public void testGetStatusVariableNames_Valid() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable();

        StatusVariable[] statusVariables = {
            new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
            new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        };
        monitorable.setStatusVariables(statusVariables);

        map.put(new MonitorableMockServiceReference("com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, bundle);

        String[] names = monitorAdmin.getStatusVariableNames("com.acme.pid");
        Assert.assertNotNull(names);
        Assert.assertEquals(2, names.length);
        Assert.assertTrue("sv.id1".equals(names[0]));
        Assert.assertTrue("sv.id2".equals(names[1]));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStatusVariableNames_Invalid1() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForGetStatusVariableNamesTests();

        monitorAdmin.getStatusVariableNames(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStatusVariableNames_Invalid2() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForGetStatusVariableNamesTests();

        monitorAdmin.getStatusVariableNames("/&%(/=");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetStatusVariableNames_Invalid3() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForGetStatusVariableNamesTests();

        monitorAdmin.getStatusVariableNames("com.aaa");
    }

    private MonitorAdmin prepareMonitorAdminForGetStatusVariableNamesTests() {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable();

        StatusVariable[] statusVariables = {
            new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
            new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        };
        monitorable.setStatusVariables(statusVariables);

        map.put(new MonitorableMockServiceReference("com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        return new MonitorAdminImpl(logVisitor, common, bundle);
    }

    @Test
    public void testGetRunningJobs() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable();

        StatusVariable[] statusVariables = {
            new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
            new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        };
        monitorable.setStatusVariables(statusVariables);
        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference("com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, bundle);

        MonitoringJob[] jobs = monitorAdmin.getRunningJobs();
        Assert.assertNotNull(jobs);
        Assert.assertEquals(0, jobs.length);

        MonitoringJob job = monitorAdmin.startJob("initiator", new String[]{"com.acme.pid/sv.id1"}, 1);

        jobs = monitorAdmin.getRunningJobs();
        Assert.assertNotNull(jobs);
        Assert.assertEquals(1, jobs.length);
        Assert.assertTrue(jobs[0].isRunning());

        job.stop();

        jobs = monitorAdmin.getRunningJobs();
        Assert.assertNotNull(jobs);
        Assert.assertEquals(0, jobs.length);
    }

    @Test
    public void testResetStatusVariable_Valid() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable();

        StatusVariable[] statusVariables = {
            new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
            new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        };
        monitorable.setStatusVariables(statusVariables);
        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference("com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, bundle);

        StatusVariable sv = monitorAdmin.getStatusVariable("com.acme.pid/sv.id1");
        Assert.assertNotNull(sv);
        Assert.assertEquals(0, sv.getInteger());

        monitorable.setNewStatusVariableValue("sv.id1", "15");

        sv = monitorAdmin.getStatusVariable("com.acme.pid/sv.id1");
        Assert.assertNotNull(sv);
        Assert.assertEquals(15, sv.getInteger());

        boolean result = monitorAdmin.resetStatusVariable("com.acme.pid/sv.id1");
        Assert.assertTrue(result);

        sv = monitorAdmin.getStatusVariable("com.acme.pid/sv.id1");
        Assert.assertNotNull(sv);
        Assert.assertEquals(0, sv.getInteger());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResetStatusVariable_Invalid1() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForResetStatusVariableTests();

        monitorAdmin.resetStatusVariable(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResetStatusVariable_Invalid2() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForResetStatusVariableTests();

        monitorAdmin.resetStatusVariable("/&%(/=");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResetStatusVariable_Invalid3() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForResetStatusVariableTests();

        monitorAdmin.resetStatusVariable("com.aaa/sv.id");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResetStatusVariable_Invalid4() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForResetStatusVariableTests();

        monitorAdmin.resetStatusVariable("com.acme.pid/sv.id_u");
    }

    private MonitorAdmin prepareMonitorAdminForResetStatusVariableTests() {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable();

        StatusVariable[] statusVariables = {
            new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
            new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        };
        monitorable.setStatusVariables(statusVariables);
        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference("com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        return new MonitorAdminImpl(logVisitor, common, bundle);
    }

    @Test
    public void testSwitchEvents_Valid() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable();

        StatusVariable[] statusVariables = {
            new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
        };
        monitorable.setStatusVariables(statusVariables);
        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference("com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, bundle);

        monitorAdmin.switchEvents("com.acme.pid/sv.id1", false);

        String[] paths = common.getDisabledNotificationPaths();
        Assert.assertNotNull(paths);
        Assert.assertEquals(1, paths.length);
        Assert.assertEquals("com.acme.pid/sv.id1", paths[0]);

        monitorAdmin.switchEvents("com.acme.pid/sv.id1", true);
        paths = common.getDisabledNotificationPaths();
        Assert.assertEquals(0, paths.length);

        monitorAdmin.switchEvents("*/sv.id1", false);
        paths = common.getDisabledNotificationPaths();
        Assert.assertEquals(1, paths.length);
        Assert.assertEquals("com.acme.pid/sv.id1", paths[0]);

        monitorAdmin.switchEvents("*/*", true);
        paths = common.getDisabledNotificationPaths();
        Assert.assertEquals(0, paths.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSwitchEvents_Invalid1() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForSwitchEventsTests();

        monitorAdmin.switchEvents(null, true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSwitchEvents_Invalid2() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForSwitchEventsTests();

        monitorAdmin.switchEvents("/&%(/=", true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSwitchEvents_Invalid3() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForSwitchEventsTests();

        monitorAdmin.switchEvents("com.aaa/sv.id1", true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSwitchEvents_Invalid4() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForSwitchEventsTests();

        monitorAdmin.switchEvents("com.acme.pid/sv.id_u", true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSwitchEvents_Invalid5() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForSwitchEventsTests();

        monitorAdmin.switchEvents("*", true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSwitchEvents_Invalid6() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForSwitchEventsTests();

        monitorAdmin.switchEvents("**", true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSwitchEvents_Invalid7() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForSwitchEventsTests();

        monitorAdmin.switchEvents("**/*", true);
    }

    private MonitorAdmin prepareMonitorAdminForSwitchEventsTests() {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable();

        StatusVariable[] statusVariables = {
            new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
        };
        monitorable.setStatusVariables(statusVariables);
        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference("com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        return new MonitorAdminImpl(logVisitor, common, bundle);
    }

    @Test
    public void testSwitchEventsWithNotification() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable();

        StatusVariable[] statusVariables = {
            new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
            new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        };
        monitorable.setStatusVariables(statusVariables);
        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference("com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, bundle);

        monitorable.setListener(common);
        monitorable.setMonitorableId("com.acme.pid");

        monitorable.setNewStatusVariableValue("sv.id1", "15");

        Event[] events = osgiVisitor.getPostedEvents();
        Assert.assertEquals(1, events.length);
        Assert.assertEquals(ConstantsMonitorAdmin.TOPIC, events[0].getTopic());
        Assert.assertEquals("com.acme.pid", events[0].getProperty(ConstantsMonitorAdmin.MON_MONITORABLE_PID));
        Assert.assertEquals("sv.id1", events[0].getProperty(ConstantsMonitorAdmin.MON_STATUSVARIABLE_NAME));
        Assert.assertEquals("15", events[0].getProperty(ConstantsMonitorAdmin.MON_STATUSVARIABLE_VALUE));
        Assert.assertNull(events[0].getProperty(ConstantsMonitorAdmin.MON_LISTENER_ID));

        monitorAdmin.switchEvents("com.acme.pid/sv.id1", false);
        osgiVisitor.cleanPostedEvents();

        monitorable.setNewStatusVariableValue("sv.id1", "25");
        events = osgiVisitor.getPostedEvents();
        Assert.assertEquals(0, events.length);

        monitorAdmin.switchEvents("com.acme.pid/sv.id1", true);
        monitorable.setNewStatusVariableValue("sv.id1", "25");
        events = osgiVisitor.getPostedEvents();
        Assert.assertEquals(1, events.length);
        Assert.assertEquals(ConstantsMonitorAdmin.TOPIC, events[0].getTopic());
        Assert.assertEquals("com.acme.pid", events[0].getProperty(ConstantsMonitorAdmin.MON_MONITORABLE_PID));
        Assert.assertEquals("sv.id1", events[0].getProperty(ConstantsMonitorAdmin.MON_STATUSVARIABLE_NAME));
        Assert.assertEquals("25", events[0].getProperty(ConstantsMonitorAdmin.MON_STATUSVARIABLE_VALUE));
        Assert.assertNull(events[0].getProperty(ConstantsMonitorAdmin.MON_LISTENER_ID));
    }

    @Test
    public void testStartJob_Valid() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable();

        StatusVariable[] statusVariables = {
            new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
            new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        };
        monitorable.setStatusVariables(statusVariables);
        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference("com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, bundle);

        monitorable.setListener(common);
        monitorable.setMonitorableId("com.acme.pid");

        MonitoringJob job = monitorAdmin.startJob("init1", new String[]{"com.acme.pid/sv.id1"}, 1);

        MonitoringJob[] jobs = monitorAdmin.getRunningJobs();
        Assert.assertEquals(1, jobs.length);

        monitorable.setNewStatusVariableValue("sv.id1", "15");

        Event[] events = osgiVisitor.getPostedEvents();
        Assert.assertEquals(2, events.length);
        Assert.assertEquals(ConstantsMonitorAdmin.TOPIC, events[0].getTopic());
        Assert.assertEquals("com.acme.pid", events[0].getProperty(ConstantsMonitorAdmin.MON_MONITORABLE_PID));
        Assert.assertEquals("sv.id1", events[0].getProperty(ConstantsMonitorAdmin.MON_STATUSVARIABLE_NAME));
        Assert.assertEquals("15", events[0].getProperty(ConstantsMonitorAdmin.MON_STATUSVARIABLE_VALUE));
        Assert.assertNull(events[0].getProperty(ConstantsMonitorAdmin.MON_LISTENER_ID));

        Assert.assertEquals(ConstantsMonitorAdmin.TOPIC, events[1].getTopic());
        Assert.assertEquals("com.acme.pid", events[1].getProperty(ConstantsMonitorAdmin.MON_MONITORABLE_PID));
        Assert.assertEquals("sv.id1", events[1].getProperty(ConstantsMonitorAdmin.MON_STATUSVARIABLE_NAME));
        Assert.assertEquals("15", events[1].getProperty(ConstantsMonitorAdmin.MON_STATUSVARIABLE_VALUE));
        Assert.assertEquals("init1", events[1].getProperty(ConstantsMonitorAdmin.MON_LISTENER_ID));

        job.stop();
        jobs = monitorAdmin.getRunningJobs();
        Assert.assertEquals(0, jobs.length);

        osgiVisitor.cleanPostedEvents();

        monitorable.setNewStatusVariableValue("sv.id1", "25");

        events = osgiVisitor.getPostedEvents();
        Assert.assertEquals(1, events.length);
        Assert.assertEquals(ConstantsMonitorAdmin.TOPIC, events[0].getTopic());
        Assert.assertEquals("com.acme.pid", events[0].getProperty(ConstantsMonitorAdmin.MON_MONITORABLE_PID));
        Assert.assertEquals("sv.id1", events[0].getProperty(ConstantsMonitorAdmin.MON_STATUSVARIABLE_NAME));
        Assert.assertEquals("25", events[0].getProperty(ConstantsMonitorAdmin.MON_STATUSVARIABLE_VALUE));
        Assert.assertNull(events[0].getProperty(ConstantsMonitorAdmin.MON_LISTENER_ID));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartJob_Invalid1() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForStartJobTests();

        monitorAdmin.startJob("init1", new String[]{"com.acme.pid/sv.id11"}, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartJob_Invalid2() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForStartJobTests();

        monitorAdmin.startJob("init1", new String[]{"com.acme.pid/sv.id2"}, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartJob_Invalid3() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForStartJobTests();

        monitorAdmin.startJob(null, new String[]{"com.acme.pid/sv.id2"}, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartJob_Invalid4() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForStartJobTests();

        monitorAdmin.startJob("init1", new String[]{"com.acme.pid1/sv.id1"}, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartJob_Invalid5() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForStartJobTests();

        monitorAdmin.startJob("init1", new String[]{"com.acme.pid/sv.id1"}, -1);
    }

    private MonitorAdmin prepareMonitorAdminForStartJobTests() {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable();

        StatusVariable[] statusVariables = {
            new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
            new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        };
        monitorable.setStatusVariables(statusVariables);
        monitorable.setNotificationSupport("sv.id1", true);

        map.put(new MonitorableMockServiceReference("com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        return new MonitorAdminImpl(logVisitor, common, bundle);
    }

    @Test
    public void testStartScheduledJob_Valid() throws Exception {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable();

        StatusVariable[] statusVariables = {
            new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
            new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        };
        monitorable.setStatusVariables(statusVariables);

        map.put(new MonitorableMockServiceReference("com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, bundle);

        monitorable.setListener(common);
        monitorable.setMonitorableId("com.acme.pid");

        monitorable.setNewStatusVariableValue("sv.id1", "15");

        MonitoringJob job = monitorAdmin.startScheduledJob("init1", new String[]{"com.acme.pid/sv.id1"}, 2, 0);

        MonitoringJob[] jobs = monitorAdmin.getRunningJobs();
        Assert.assertEquals(1, jobs.length);

        TimeUnit.SECONDS.sleep(3);

        Event[] events = osgiVisitor.getPostedEvents();
        Assert.assertEquals(2, events.length);
        Assert.assertEquals(ConstantsMonitorAdmin.TOPIC, events[0].getTopic());
        Assert.assertEquals("com.acme.pid", events[0].getProperty(ConstantsMonitorAdmin.MON_MONITORABLE_PID));
        Assert.assertEquals("sv.id1", events[0].getProperty(ConstantsMonitorAdmin.MON_STATUSVARIABLE_NAME));
        Assert.assertEquals("15", events[0].getProperty(ConstantsMonitorAdmin.MON_STATUSVARIABLE_VALUE));
        Assert.assertEquals("init1", events[0].getProperty(ConstantsMonitorAdmin.MON_LISTENER_ID));

        job.stop();
        jobs = monitorAdmin.getRunningJobs();
        Assert.assertEquals(0, jobs.length);

        osgiVisitor.cleanPostedEvents();

        monitorable.setNewStatusVariableValue("sv.id1", "25");

        events = osgiVisitor.getPostedEvents();
        Assert.assertEquals(0, events.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartScheduledJob_Invalid1() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForStartScheduledJobTests();

        monitorAdmin.startScheduledJob("init1", new String[]{"com.acme.pid/sv.id11"}, 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartScheduledJob_Invalid2() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForStartScheduledJobTests();

        monitorAdmin.startScheduledJob("init1", new String[]{"com.acme.pid1/sv.id1"}, 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartScheduledJob_Invalid3() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForStartScheduledJobTests();

        monitorAdmin.startScheduledJob(null, new String[]{"com.acme.pid1/sv.id1"}, 1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartScheduledJob_Invalid4() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForStartScheduledJobTests();

        monitorAdmin.startScheduledJob("init1", new String[]{"com.acme.pid/sv.id1"}, -1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStartScheduledJob_Invalid5() throws Exception {
        MonitorAdmin monitorAdmin = prepareMonitorAdminForStartScheduledJobTests();

        monitorAdmin.startScheduledJob("init1", new String[]{"com.acme.pid/sv.id1"}, 1, -1);
    }

    private MonitorAdmin prepareMonitorAdminForStartScheduledJobTests() {
        HashMap<ServiceReference, Monitorable> map = new HashMap<ServiceReference, Monitorable>();

        MockMonitorable monitorable = new MockMonitorable();

        StatusVariable[] statusVariables = {
            new StatusVariable("sv.id1", StatusVariable.CM_CC, 0),
            new StatusVariable("sv.id2", StatusVariable.CM_CC, "test")
        };
        monitorable.setStatusVariables(statusVariables);

        map.put(new MonitorableMockServiceReference("com.acme.pid"), monitorable);
        osgiVisitor.setReferences(map);

        MonitorAdmin monitorAdmin = new MonitorAdminImpl(logVisitor, common, bundle);

        monitorable.setListener(common);
        monitorable.setMonitorableId("com.acme.pid");

        monitorable.setNewStatusVariableValue("sv.id1", "15");
        return monitorAdmin;
    }
}

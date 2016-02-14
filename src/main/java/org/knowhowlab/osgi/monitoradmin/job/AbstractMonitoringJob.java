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

package org.knowhowlab.osgi.monitoradmin.job;

import org.knowhowlab.osgi.monitoradmin.LogVisitor;
import org.osgi.service.monitor.MonitoringJob;
import org.osgi.service.monitor.StatusVariable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.lang.String.format;

/**
 * Abstract MonitoringJob
 *
 * @author dmytro.pishchukhin
 */
public abstract class AbstractMonitoringJob implements MonitoringJob {
    MonitoringJobVisitor visitor;
    LogVisitor logVisitor;
    // job initiator
    private String initiator;
    // list of monitoring StatusVariables
    Set<String> statusVariablePaths = new HashSet<String>();
    // job state
    boolean isRunning;
    int schedule = 0;
    int count = 0;

    AbstractMonitoringJob(MonitoringJobVisitor visitor, LogVisitor logVisitor, String initiator,
                          String[] statusVariablePaths, int schedule, int count) {
        this.visitor = visitor;
        this.logVisitor = logVisitor;
        this.initiator = initiator;
        this.statusVariablePaths.addAll(Arrays.asList(statusVariablePaths));
        this.schedule = schedule;
        this.count = count;
        this.isRunning = true;
    }

    AbstractMonitoringJob(MonitoringJobVisitor visitor, LogVisitor logVisitor, String initiator,
                          String[] statusVariablePaths, int count) {
        this.visitor = visitor;
        this.logVisitor = logVisitor;
        this.initiator = initiator;
        this.statusVariablePaths.addAll(Arrays.asList(statusVariablePaths));
        this.count = count;
        this.isRunning = true;
    }

    public synchronized void stop() {
        if (isRunning) {
            isRunning = false;
            visitor.cancelJob(this);
        }
    }

    public String getInitiator() {
        return initiator;
    }

    public String[] getStatusVariableNames() {
        return statusVariablePaths.toArray(new String[statusVariablePaths.size()]);
    }

    public int getSchedule() {
        return schedule;
    }

    public int getReportCount() {
        return count;
    }

    public boolean isLocal() {
        return true;
    }

    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Cancel job internaly
     */
    public abstract void cancel();

    /**
     * Does job handle StatusVariable update event
     *
     * @param path StatusVariable path
     * @return <code>true</code> - handles, otherwise - <code>false</code>
     */
    public abstract boolean isHandleUpdateEvent(String path);

    /**
     * Handle StatusVariable update event
     *
     * @param monitorableId  monitorableId
     * @param statusVariable statusVariable
     */
    public abstract void handleUpdateEvent(String monitorableId, StatusVariable statusVariable);

    @Override
    public String toString() {
        return format("%s{{initiator='%s', statusVariablePaths=%s, schedule=%s, count=%s}",
            getClass().getSimpleName(), initiator, statusVariablePaths, schedule, count);
    }
}

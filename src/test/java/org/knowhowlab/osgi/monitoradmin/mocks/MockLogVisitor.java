/*
 * Copyright (c) 2016 Dmytro Pishchukhin (http://knowhowlab.org)
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

package org.knowhowlab.osgi.monitoradmin.mocks;

import org.knowhowlab.osgi.monitoradmin.LogVisitor;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author dpishchukhin
 */
public class MockLogVisitor implements LogVisitor {
    private static final Logger LOG = Logger.getLogger(MockLogVisitor.class.getName());

    public void debug(String message, Throwable throwable) {
        LOG.log(Level.FINE, message, throwable);
    }

    public void info(String message, Throwable throwable) {
        LOG.log(Level.INFO, message, throwable);
    }

    public void warning(String message, Throwable throwable) {
        LOG.log(Level.WARNING, message, throwable);
    }

    public void error(String message, Throwable throwable) {
        LOG.log(Level.SEVERE, message, throwable);
    }
}

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

import org.junit.Assert;
import org.junit.Test;
import org.knowhowlab.osgi.monitoradmin.util.StatusVariablePath;

/**
 * @author dmytro.pishchukhin
 */
public class StatusVariablePathTest {
    @Test
    public void testParse_ValidPath() {
        StatusVariablePath path = new StatusVariablePath("aaa/aaa");
        Assert.assertEquals("aaa", path.getMonitorableId());
        Assert.assertEquals("aaa", path.getStatusVariableId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_InvalidPath1() {
        new StatusVariablePath("aaa/aaa/aaa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_InvalidPath2() {
        new StatusVariablePath("/aaa");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParse_InvalidPath3() {
        new StatusVariablePath("aaa.aaa./aaa");
    }
}

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

package org.knowhowlab.osgi.monitoradmin.mocks;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.springframework.osgi.mock.MockBundle;

import java.security.Permission;
import java.util.Dictionary;
import java.util.Map;

/**
 * @author dpishchukhin
 */
public class SecurityMockBundle extends MockBundle {
    private Permission[] permissions;

    public SecurityMockBundle() {
    }

    public SecurityMockBundle(Permission... permissions) {
        this.permissions = permissions;
    }

    public SecurityMockBundle(Dictionary headers) {
        super(headers);
    }

    public SecurityMockBundle(BundleContext context) {
        super(context);
    }

    public SecurityMockBundle(String symName) {
        super(symName);
    }

    public SecurityMockBundle(String symName, Dictionary headers, BundleContext context) {
        super(symName, headers, context);
    }

    @Override
    public boolean hasPermission(Object permission) {
        if (permissions != null) {
            for (Permission perm : permissions) {
                if (perm.implies((Permission) permission)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    public Map getSignerCertificates(int i) {
        return null;
    }

    @Override
    public Version getVersion() {
        return null;
    }
}

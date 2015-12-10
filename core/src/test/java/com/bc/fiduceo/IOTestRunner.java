
/*
 * Copyright (C) 2015 Brockmann Consult GmbH
 * This code was developed for the EC project "Fidelity and Uncertainty in
 * Climate Data Records from Earth Observations (FIDUCEO)".
 * Grant Agreement: 638822
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * A copy of the GNU General Public License should have been supplied along
 * with this program; if not, see http://www.gnu.org/licenses/
 *
 */

package com.bc.fiduceo;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.io.InputStream;

public class IOTestRunner extends BlockJUnit4ClassRunner {

    private static final String PROPERTYNAME_EXECUTE_PRODUCT_TESTS = "com.bc.fiduceo.product.tests.execute";

    private boolean executeProductTests;
    private final Class<?> clazz;

    public IOTestRunner(Class<?> klass) throws InitializationError {
        super(klass);

        this.clazz = klass;
        executeProductTests = Boolean.getBoolean(PROPERTYNAME_EXECUTE_PRODUCT_TESTS);
        if (!executeProductTests) {
            System.out.println("Product Tests disabled. Set VM param -D" + PROPERTYNAME_EXECUTE_PRODUCT_TESTS + "=true to enable.");
        }

        final InputStream resourceStream = getClass().getResourceAsStream("dataDirectory.properties");
        if (resourceStream == null) {
            System.out.println("Product Tests disabled. 'dataDirectory.properties' file not found");
            executeProductTests = false;
        }
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        if (executeProductTests) {
            super.runChild(method, notifier);
        } else {
            final Description description = Description.createTestDescription(clazz, "allMethods. Product tests disabled. Set VM param -D" + PROPERTYNAME_EXECUTE_PRODUCT_TESTS + "=true to enable.");
            notifier.fireTestIgnored(description);
        }
    }
}


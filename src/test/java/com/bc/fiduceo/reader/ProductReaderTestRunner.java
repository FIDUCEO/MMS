package com.bc.fiduceo.reader;

import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class ProductReaderTestRunner extends BlockJUnit4ClassRunner {

    private static final String PROPERTYNAME_EXECUTE_PRODUCT_TESTS = "com.bc.fiduceo.product.tests.execute";

    private boolean executeProductTests;
    private final Class<?> clazz;

    public ProductReaderTestRunner(Class<?> klass) throws InitializationError, IOException {
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


package com.bc.fiduceo.db;


import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class DatabaseTestRunner extends BlockJUnit4ClassRunner {

    private static final String PROPERTYNAME_EXECUTE_DB_TESTS = "com.bc.fiduceo.db.tests.execute";

    private final boolean executeDbTests;
    private final Class<?> clazz;

    public DatabaseTestRunner(Class<?> klass) throws InitializationError {
        super(klass);

        this.clazz = klass;
        executeDbTests = Boolean.getBoolean(PROPERTYNAME_EXECUTE_DB_TESTS);
        if (!executeDbTests) {
            System.out.println("DBTests disabled. Set VM param -D" + PROPERTYNAME_EXECUTE_DB_TESTS + "=true to enable.");
        }
    }

    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        if (executeDbTests) {
            super.runChild(method, notifier);
        } else {
            final Description description = Description.createTestDescription(clazz, "allMethods. Database tests disabled. Set VM param -D" + PROPERTYNAME_EXECUTE_DB_TESTS + "=true to enable.");
            notifier.fireTestIgnored(description);
        }
    }
}

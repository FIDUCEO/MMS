package com.bc.fiduceo.reader;

import com.bc.ceres.core.ServiceRegistry;
import com.bc.ceres.core.ServiceRegistryManager;
import org.esa.snap.SnapCoreActivator;

import java.util.HashMap;

/**
 * @author muhammad.bc
 */
public class ReaderFactory {

    final HashMap<String, Reader> readerHashMap = new HashMap<>();

    public ReaderFactory() {
        final ServiceRegistryManager serviceRegistryManager = ServiceRegistryManager.getInstance();
        ServiceRegistry<Reader> readerRegistry = serviceRegistryManager.getServiceRegistry(Reader.class);
        SnapCoreActivator.loadServices(readerRegistry);

        for (Reader reader : readerRegistry.getServices()) {
            String[] supportedSensorKeys = reader.getSupportedSensorKeys();
            for (String key : supportedSensorKeys) {
                readerHashMap.put(key, reader);
            }
        }
        if (readerHashMap == null) {
            throw new NullPointerException("No exist reader");
        }
    }

    public Reader getReader(String key) {
        if (key.isEmpty() || key.length() <= 0) {
            throw new IllegalArgumentException("The reader support sensor key most be well define");
        }
        Reader reader = readerHashMap.get(key);
        if (reader == null) {
            throw new NullPointerException("No support sensor with such :" + key + " key");
        }
        return reader;
    }
}

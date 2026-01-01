package me.gb2022.modular.pack;

import me.gb2022.commons.TriState;
import me.gb2022.modular.ModularApplicationContext;
import me.gb2022.modular.ObjectOperationResult;
import me.gb2022.modular.service.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.*;

public class PackageManager implements Service {
    protected final Properties statusMap = new Properties();
    private final Map<String, ApplicationPackage> packages = new HashMap<>();
    private final Logger logger = getLogger();
    private final ModularApplicationContext context;

    public PackageManager(ModularApplicationContext context) {
        this.context = context;
    }

    public void disable() throws Exception {
        this.saveStatus();
        for (String id : new ArrayList<>(this.getPackages().keySet())) {
            this.removePackage(id);
        }
    }

    public void initializePackageBuilder(ContentBuilder builder) {
    }

    public final boolean isEnabled(String id) {
        return getStatus(id) == TriState.FALSE;
    }

    public Logger getLogger() {
        return LogManager.getLogger("PackageManager");
    }

    public void saveStatus(Properties meta) {
        this.logger.warn("attempt to save package status with default(No-OP) impl!");
    }

    public void handleException(Throwable e) {
        this.logger.error("Caught exception: {}", e.getMessage());
        this.logger.catching(e);
    }

    public boolean isReservedPackage(ApplicationPackage pack) {
        return pack.meta().internal();
    }

    public boolean defaultPackageStatus(ApplicationPackage pack) {
        return true;
    }

    public final ApplicationPackage buildPackage(Object holder, Method method) {
        if (!method.isAnnotationPresent(ApplicationPackageProvider.class)) {
            throw new IllegalArgumentException("Method " + method.getName() + " is not annotated with @ApplicationPackageProvider!");
        }

        var annotation = method.getAnnotation(ApplicationPackageProvider.class);
        var meta = PackageMetadata.create(annotation);
        var builder = new ContentBuilder();

        this.initializePackageBuilder(builder);

        try {
            method.setAccessible(true);
            method.invoke(null, builder);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return new ApplicationPackage(holder, meta, builder);
    }

    public final ApplicationPackage get(String id) {
        return this.packages.get(id);
    }

    public final Map<String, ApplicationPackage> getPackages() {
        return this.packages;
    }

    public final ObjectOperationResult enable(String id) {
        if (isReservedPackage(get(id))) {
            return ObjectOperationResult.BLOCKED_INTERNAL;
        }
        ObjectOperationResult result = enable0(id);
        if (result == ObjectOperationResult.SUCCESS) {
            this.logger.info("enabled module %s.".formatted(id));
        }
        this.saveStatus();
        return result;
    }

    public final ObjectOperationResult disable(String id) {
        if (isReservedPackage(get(id))) {
            return ObjectOperationResult.BLOCKED_INTERNAL;
        }
        ObjectOperationResult result = disable0(id);
        if (result == ObjectOperationResult.SUCCESS) {
            this.logger.info("disabled module %s.".formatted(id));
        }
        this.saveStatus();
        return result;
    }

    public final TriState getStatus(String id) {
        if (!this.statusMap.containsKey(id)) {
            return TriState.UNKNOWN;
        }
        return Objects.equals(this.statusMap.get(id), "enabled") ? TriState.TRUE : TriState.FALSE;
    }

    public final void addPackage(ApplicationPackage pkg) {
        var id = pkg.meta().id();

        try {
            pkg.initContext(this.context);
            pkg.initialize();


            this.packages.put(id, pkg);
            if (getStatus(id) == TriState.UNKNOWN) {
                var enable = defaultPackageStatus(pkg);
                this.statusMap.put(id, enable ? "enabled" : "disabled");
                this.saveStatus();
            }
            if (isReservedPackage(pkg)) {
                this.statusMap.put(id, "enabled");
            }
            if (getStatus(id) == TriState.TRUE) {
                try {
                    pkg.enable();
                } catch (Exception ex) {
                    this.handleException(ex);
                }
            }
        } catch (Exception e) {
            this.handleException(e);
        }
    }

    public final void removePackage(String id) {
        if (this.getStatus(id) == TriState.UNKNOWN) {
            return;
        }
        if (!this.packages.containsKey(id)) {
            return;
        }
        try {
            this.packages.get(id).disable();
        } catch (Exception e) {
            this.handleException(e);
        }
        this.packages.remove(id);
    }


    private void saveStatus() {
        this.saveStatus(this.statusMap);
    }

    private ObjectOperationResult enable0(String id) {
        if (getStatus(id) == TriState.UNKNOWN) {
            return ObjectOperationResult.NOT_FOUND;
        }
        if (getStatus(id) == TriState.TRUE) {
            return ObjectOperationResult.ALREADY_OPERATED;
        }
        try {
            this.get(id).enable();
        } catch (Exception ex) {
            this.handleException(ex);
            return ObjectOperationResult.INTERNAL_ERROR;
        }
        this.statusMap.put(id, "enabled");
        return ObjectOperationResult.SUCCESS;
    }

    private ObjectOperationResult disable0(String id) {
        if (getStatus(id) == TriState.UNKNOWN) {
            return ObjectOperationResult.NOT_FOUND;
        }
        if (getStatus(id) == TriState.FALSE) {
            return ObjectOperationResult.ALREADY_OPERATED;
        }
        try {
            this.get(id).disable();
        } catch (Exception ex) {
            this.handleException(ex);
            return ObjectOperationResult.INTERNAL_ERROR;
        }
        this.statusMap.put(id, "disabled");
        return ObjectOperationResult.SUCCESS;
    }

    public final ModularApplicationContext context() {
        return this.context;
    }

    public Map<String, ApplicationPackage> getAllPackages() {
        return this.packages;
    }

    public final Set<String> getIdsByStatus(TriState status) {
        var result = new HashSet<String>();
        for (var id : this.packages.keySet()) {
            if (getStatus(id) != status) {
                continue;
            }
            result.add(id);
        }
        return result;
    }

    public void enableAll() {
        for (String id : this.getPackages().keySet()) {
            if (isReservedPackage(get(id))) {
                continue;
            }
            this.enable(id);
        }
    }

    public void disableAll() {
        for (String id : this.getPackages().keySet()) {
            if (isReservedPackage(get(id))) {
                continue;
            }
            this.disable(id);
        }
    }
}

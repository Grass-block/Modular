package me.gb2022.modular;

import me.gb2022.modular.module.ModuleManager;
import me.gb2022.modular.pack.ApplicationPackage;
import me.gb2022.modular.pack.ApplicationPackageProvider;
import me.gb2022.modular.pack.PackageManager;
import me.gb2022.modular.service.ServiceLayer;
import me.gb2022.modular.service.ServiceManager;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public final class ModularApplicationContext {
    private final Object holder;
    private final PackageManager packageManager;
    private final ServiceManager serviceManager;
    private final ModuleManager moduleManager;

    private ModularApplicationContext(Object holder, Builder builder) {
        this.holder = holder;
        this.packageManager = builder.packageManagerProvider.apply(this);
        this.serviceManager = builder.serviceManagerProvider.apply(this);
        this.moduleManager = builder.moduleManagerProvider.apply(this);
    }

    public static Builder builder(Object holder) {
        return new Builder(holder);
    }

    public Set<ApplicationPackage> registerPackage(Object holder, Class<?> target) {
        var packages = new HashSet<ApplicationPackage>();

        for (var m : target.getDeclaredMethods()) {
            if (!m.isAnnotationPresent(ApplicationPackageProvider.class)) {
                continue;
            }

            packages.add(this.getPackageManager().buildPackage(holder, m));
        }

        for (var p : packages) {
            this.packageManager.addPackage(p);
        }

        return packages;
    }

    public void initialize() {
        try {
            this.packageManager.enable();
            this.moduleManager.enable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        try {
            this.moduleManager.disable();
            this.serviceManager.removeAll(ServiceLayer.USER);
            this.serviceManager.removeAll(ServiceLayer.FRAMEWORK);
            this.serviceManager.removeAll(ServiceLayer.FOUNDATION);
            this.packageManager.disable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public PackageManager getPackageManager() {
        return packageManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public <H> H holder(Class<H> type) {
        return type.cast(holder);
    }

    public static final class Builder {
        private final Object holder;

        private Function<ModularApplicationContext, ServiceManager> serviceManagerProvider = ServiceManager::new;
        private Function<ModularApplicationContext, PackageManager> packageManagerProvider = PackageManager::new;
        private Function<ModularApplicationContext, ModuleManager> moduleManagerProvider = ModuleManager::new;

        public Builder(Object holder) {
            this.holder = holder;
        }

        public Builder serviceManager(Function<ModularApplicationContext, ServiceManager> provider) {
            this.serviceManagerProvider = provider;
            return this;
        }

        public Builder packageManager(Function<ModularApplicationContext, PackageManager> provider) {
            this.packageManagerProvider = provider;
            return this;
        }

        public ModularApplicationContext build() {
            return new ModularApplicationContext(this.holder, this);
        }

        public Builder moduleManager(Function<ModularApplicationContext, ModuleManager> provider) {
            this.moduleManagerProvider = provider;
            return this;
        }
    }
}

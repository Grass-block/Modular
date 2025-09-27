package me.gb2022.modular.pack;

import me.gb2022.modular.FeatureAvailability;
import me.gb2022.modular.module.IModule;
import me.gb2022.modular.module.ModuleHandle;
import me.gb2022.modular.registry.ModuleRegistry;
import me.gb2022.modular.registry.ServiceRegistry;
import me.gb2022.modular.service.Service;

import java.util.logging.Logger;

public interface IPackage<M extends IModule, H extends ModuleHandle, S extends Service> {
    String getId();

    default ApplicationPackage getDescriptor() {
        return this.getClass().getAnnotation(ApplicationPackage.class);
    }

    String getLoggerName();

    Logger getLogger();

    void onEnable();

    void onDisable();

    FeatureAvailability getAvailability();

    ServiceRegistry<S> serviceRegistry();

    ModuleRegistry<M, H> moduleRegistry();

    void initializePackage();
}

package me.gb2022.modular.pack;

import me.gb2022.modular.FeatureAvailability;
import me.gb2022.modular.module.IModule;
import me.gb2022.modular.module.ModuleHandle;
import me.gb2022.modular.module.ModuleManagerV2;
import me.gb2022.modular.registry.ContentRegistry;
import me.gb2022.modular.registry.ModuleRegistry;
import me.gb2022.modular.registry.ServiceRegistry;
import me.gb2022.modular.service.ServiceManager;
import me.gb2022.modular.service.Service;


public abstract class AbstractPackage<M extends IModule, H extends ModuleHandle, S extends Service> implements IPackage<M, H, S> {
    private final String id;
    private final ModuleRegistry<M, H> moduleRegistry;
    private final ServiceRegistry<S> serviceRegistry;
    private final FeatureAvailability availability;

    public AbstractPackage(String id, FeatureAvailability availability, ModuleRegistry<M, H> moduleRegistry, ServiceRegistry<S> serviceRegistry) {
        this.id = id;
        this.moduleRegistry = moduleRegistry;
        this.serviceRegistry = serviceRegistry;
        this.availability = availability;
    }

    public AbstractPackage(String id, FeatureAvailability availability, ContentRegistry<M, H, S> registry) {
        this.id = id;
        this.moduleRegistry = registry.createModuleRegistry(this);
        this.serviceRegistry = registry.createServiceRegistry();
        this.availability = availability;
    }

    public abstract ModuleManagerV2 getModuleManager();

    public abstract ServiceManager<S> getServiceManager();


    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void onEnable() {
        this.serviceRegistry.register(this.getServiceManager());
        this.moduleRegistry.register(this.getModuleManager());
    }

    @Override
    public void onDisable() {
        this.moduleRegistry.unregister(this.getModuleManager());
        this.serviceRegistry.unregister(this.getServiceManager());
    }

    @Override
    public FeatureAvailability getAvailability() {
        return availability;
    }

    @Override
    public ServiceRegistry<S> serviceRegistry() {
        return serviceRegistry;
    }

    @Override
    public ModuleRegistry<M, H> moduleRegistry() {
        return this.moduleRegistry;
    }
}

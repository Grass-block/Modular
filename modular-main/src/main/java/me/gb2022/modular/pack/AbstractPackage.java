package me.gb2022.modular.pack;

import me.gb2022.modular.FeatureAvailability;
import me.gb2022.modular.module.IModule;
import me.gb2022.modular.module.ModuleManager;
import me.gb2022.modular.module.meta.ModuleMeta;
import me.gb2022.modular.registry.ContentRegistry;
import me.gb2022.modular.registry.ModuleRegistry;
import me.gb2022.modular.registry.ServiceRegistry;
import me.gb2022.modular.service.Service;
import me.gb2022.modular.service.ServiceManager;

public abstract class AbstractPackage<M extends ModuleMeta, H extends IModule, S extends Service> implements IPackage<M, H, S> {
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

    public abstract ModuleManager<M, H> getModuleManager();

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

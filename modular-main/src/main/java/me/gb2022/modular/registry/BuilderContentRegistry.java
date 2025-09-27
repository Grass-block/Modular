package me.gb2022.modular.registry;

import me.gb2022.modular.module.IModule;
import me.gb2022.modular.module.ModuleHandle;
import me.gb2022.modular.pack.IPackage;
import me.gb2022.modular.service.Service;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("rawtypes")
public abstract class BuilderContentRegistry<M extends IModule, H extends ModuleHandle, S extends Service> implements ContentRegistry<M, H, S> {
    private final Set<Class<? extends M>> modules = new HashSet<>();
    private final Set<Class<? extends S>> services = new HashSet<>();

    public BuilderContentRegistry<M, H, S> module(String id, Class<? extends M> clazz) {
        this.modules.add(clazz);
        return this;
    }

    public BuilderContentRegistry<M, H, S> module(Class<? extends M> clazz) {
        this.modules.add(clazz);
        return this;
    }

    public BuilderContentRegistry<M, H, S> service(Class<? extends S> clazz) {
        this.services.add(clazz);
        return this;
    }

    @Override
    public ServiceRegistry<S> createServiceRegistry() {
        return new ServiceRegistry<>(this.services);
    }

    @Override
    public ModuleRegistry<M, H> createModuleRegistry(IPackage<M, H, S> pkg) {
        return new ModuleRegistry<>(this, pkg, this.modules);
    }
}

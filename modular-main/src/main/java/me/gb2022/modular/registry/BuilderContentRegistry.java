package me.gb2022.modular.registry;

import me.gb2022.modular.module.IModule;
import me.gb2022.modular.module.meta.ModuleMeta;
import me.gb2022.modular.pack.IPackage;
import me.gb2022.modular.service.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class BuilderContentRegistry<M extends ModuleMeta, H extends IModule, S extends Service> implements ContentRegistry<M, H, S> {
    private final Map<String, Class<? extends H>> modules = new HashMap<>();
    private final Set<Class<? extends S>> services = new HashSet<>();

    public BuilderContentRegistry<M, H, S> module(String id, Class<? extends H> clazz) {
        this.modules.put(id, clazz);
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

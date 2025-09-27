package me.gb2022.modular.registry;

import me.gb2022.modular.module.IModule;
import me.gb2022.modular.module.ModuleHandle;
import me.gb2022.modular.pack.IPackage;
import me.gb2022.modular.service.Service;

@SuppressWarnings("rawtypes")
public interface ContentRegistry<M extends IModule, H extends ModuleHandle, S extends Service> {
    H wrapMeta(IPackage<M, H, S> owner, Class<? extends M> mc);

    ServiceRegistry<S> createServiceRegistry();

    ModuleRegistry<M, H> createModuleRegistry(IPackage<M, H, S> pkg);
}

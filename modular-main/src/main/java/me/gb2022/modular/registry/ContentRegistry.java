package me.gb2022.modular.registry;

import me.gb2022.modular.module.IModule;
import me.gb2022.modular.module.meta.ModuleMeta;
import me.gb2022.modular.pack.IPackage;
import me.gb2022.modular.service.Service;

public interface ContentRegistry<M extends ModuleMeta, H extends IModule, S extends Service> {
    M wrapMeta(String id, IPackage<M, H, S> owner, Class<? extends H> mc);

    ServiceRegistry<S> createServiceRegistry();

    ModuleRegistry<M, H> createModuleRegistry(IPackage<M, H, S> pkg);
}

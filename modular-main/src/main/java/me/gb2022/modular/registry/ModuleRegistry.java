package me.gb2022.modular.registry;

import me.gb2022.modular.module.IModule;
import me.gb2022.modular.module.ModuleManager;
import me.gb2022.modular.module.meta.ModuleMeta;
import me.gb2022.modular.pack.IPackage;
import me.gb2022.modular.service.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ModuleRegistry<M extends ModuleMeta, T extends IModule> {
    private final Set<M> metas = new HashSet<>();

    public <S extends Service> ModuleRegistry(ContentRegistry<M, T, S> registry, IPackage<M, T, S> pkg, Map<String, Class<? extends T>> mcs) {
        mcs.forEach((id, c) -> this.metas.add(registry.wrapMeta(id, pkg, c)));
    }

    public void register(ModuleManager<M, T> moduleManager) {
        for (var meta : this.metas) {
            moduleManager.registerMeta(meta);
        }
    }

    public void unregister(ModuleManager<M, T> moduleManager) {
        for (var meta : this.metas) {
            moduleManager.unregister(meta.fullId());
        }
    }

    public Set<M> getMetas() {
        return metas;
    }
}

package me.gb2022.modular.registry;

import me.gb2022.modular.module.IModule;
import me.gb2022.modular.module.ModuleHandle;
import me.gb2022.modular.module.ModuleManagerV2;
import me.gb2022.modular.pack.IPackage;
import me.gb2022.modular.service.Service;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("rawtypes")
public final class ModuleRegistry<M extends IModule, T extends ModuleHandle> {
    private final Set<T> metas = new HashSet<>();

    public <S extends Service> ModuleRegistry(ContentRegistry<M, T, S> registry, IPackage<M, T, S> pkg, Set<Class<? extends M>> mcs) {
        mcs.forEach((c) -> this.metas.add(registry.wrapMeta(pkg, c)));
    }

    public void register(ModuleManagerV2 moduleManager) {
        for (var meta : this.metas) {
            moduleManager.register(meta);
        }
    }

    public void unregister(ModuleManagerV2 moduleManager) {
        for (var meta : this.metas) {
            moduleManager.unregister(meta.getMetadata().key().fullId());
        }
    }

    public Set<T> getMetas() {
        return metas;
    }
}

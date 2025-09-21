package me.gb2022.modular.registry;

import me.gb2022.modular.service.ServiceManager;
import me.gb2022.modular.service.Service;

import java.util.HashSet;
import java.util.Set;

public final class ServiceRegistry<T extends Service> {
    private final Set<Class<? extends T>> services = new HashSet<>();

    public ServiceRegistry(Set<Class<? extends T>> mcs) {
        this.services.addAll(mcs);
    }

    public void register(ServiceManager<T> sm) {
        for (var meta : this.services) {
            sm.registerService(meta);
        }
    }

    public void unregister(ServiceManager<T> sm) {
        for (var meta : this.services) {
            sm.unregisterService(meta);
        }
    }


    public Set<Class<? extends T>> getServices() {
        return services;
    }
}

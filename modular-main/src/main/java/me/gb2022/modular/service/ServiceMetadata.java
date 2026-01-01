package me.gb2022.modular.service;

import me.gb2022.modular.NamespaceKey;
import me.gb2022.modular.service.ApplicationService;
import me.gb2022.modular.service.Service;

public final class ServiceMetadata implements Comparable<ServiceMetadata> {
    private final NamespaceKey key;
    private final ServiceLayer layer;
    private final Class<? extends Service> implClass;
    private final boolean export;

    public ServiceMetadata(String namespace, String id, ServiceLayer layer, Class<? extends Service> implClass, boolean export) {
        this.layer = layer;
        this.implClass = implClass;
        this.export = export;
        this.key = new NamespaceKey(namespace, id);
    }

    public static ServiceMetadata parse(String namespace, Class<? extends Service> ref) {
        var annotation = ref.getAnnotation(ApplicationService.class);
        return new ServiceMetadata(namespace, annotation.id(), annotation.layer(), annotation.impl(), annotation.export());
    }

    public NamespaceKey key() {
        return key;
    }

    public String fullId() {
        return this.key.fullId();
    }

    public Class<? extends Service> impl() {
        return implClass;
    }

    public boolean export() {
        return export;
    }

    public ServiceLayer layer() {
        return layer;
    }

    @Override
    public int compareTo(ServiceMetadata o) {
        return toString().compareTo(o.toString());
    }

    public String id() {
        return this.key.id();
    }
}

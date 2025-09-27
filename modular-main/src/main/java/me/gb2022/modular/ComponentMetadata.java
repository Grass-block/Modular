package me.gb2022.modular;

import me.gb2022.modular.module.ApplicationModule;
import me.gb2022.modular.module.IModule;

public final class ComponentMetadata implements Comparable<ComponentMetadata> {
    private final NamespaceKey key;
    private final String version;
    private final String description;
    private final boolean internal;
    private final boolean defaultEnabled;
    private final boolean beta;

    public ComponentMetadata(String namespace, String id, String version, String description, boolean internal, boolean defaultEnabled, boolean beta) {
        this.key = new NamespaceKey(namespace, id);
        this.internal = internal;
        this.version = version;
        this.defaultEnabled = defaultEnabled;
        this.description = description;
        this.beta = beta;
    }

    public static ComponentMetadata fromModule(String namespace, Class<? extends IModule> ref) {
        var annotation = ref.getAnnotation(ApplicationModule.class);
        return new ComponentMetadata(
                namespace,
                annotation.id(),
                annotation.version(),
                annotation.description(),
                annotation.internal(),
                annotation.defaultEnable(),
                annotation.beta()
        );
    }

    public NamespaceKey key() {
        return key;
    }

    public String version() {
        return version;
    }

    public String description() {
        return description;
    }

    public boolean internal() {
        return internal;
    }

    public boolean defaultEnabled() {
        return defaultEnabled;
    }

    public boolean beta() {
        return beta;
    }

    public String fullId() {
        return this.key.fullId();
    }

    @Override
    public int compareTo(ComponentMetadata o) {
        return toString().compareTo(o.toString());
    }
}

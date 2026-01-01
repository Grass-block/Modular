package me.gb2022.modular.module;

import me.gb2022.modular.NamespaceKey;

public final class ModuleMetadata implements Comparable<ModuleMetadata> {
    private final NamespaceKey key;
    private final String version;
    private final boolean internal;
    private final boolean defaultEnabled;
    private final boolean beta;
    private String description;

    public ModuleMetadata(String namespace, String id, String version, String description, boolean internal, boolean defaultEnabled, boolean beta) {
        this.key = new NamespaceKey(namespace, id);
        this.internal = internal;
        this.version = version;
        this.defaultEnabled = defaultEnabled;
        this.description = description;
        this.beta = beta;
    }

    public static ModuleMetadata parse(String namespace, Class<?> ref) {
        var annotation = ref.getAnnotation(ApplicationModule.class);
        return new ModuleMetadata(
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
    public int compareTo(ModuleMetadata o) {
        return toString().compareTo(o.toString());
    }

    public void appendDescription(String s) {
        this.description += ("\n" + s);
    }
}

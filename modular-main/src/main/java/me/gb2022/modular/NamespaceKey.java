package me.gb2022.modular;

public final class NamespaceKey {
    private final String namespace;
    private final String key;

    public NamespaceKey(String namespace, String key) {
        this.namespace = namespace;
        this.key = key;
    }

    public String id() {
        return key;
    }

    public String namespace() {
        return namespace;
    }

    public String fullId() {
        return namespace + ":" + key;
    }

    @Override
    public String toString() {
        return fullId();
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NamespaceKey k)) {
            return false;
        }

        return toString().equals(k.toString());
    }
}

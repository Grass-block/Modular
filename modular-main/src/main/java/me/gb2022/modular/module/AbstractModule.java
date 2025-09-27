package me.gb2022.modular.module;

import me.gb2022.modular.pack.IPackage;

import java.util.Objects;

@SuppressWarnings("rawtypes")
public abstract class AbstractModule<H extends ModuleHandle,P extends IPackage<?,H,?>> implements IModule<P,H> {
    private H handle;

    @Override
    public P parent() {
        return (P) this.handle.getParent();
    }

    @Override
    public H handle() {
        return this.handle;
    }

    @Override
    public void init(String id, P parent, H handle) {
        this.handle = handle;
    }

    @Override
    public final String toString() {
        return "%s{%s}".formatted(getClass().getSimpleName(), descriptor());
    }

    @Override
    public final int hashCode() {
        return this.getFullId().hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof IModule m)) {
            return false;
        }
        return Objects.equals(m.getFullId(), this.getFullId());
    }

    @Override
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("cannot clone a module instance!");
    }
}

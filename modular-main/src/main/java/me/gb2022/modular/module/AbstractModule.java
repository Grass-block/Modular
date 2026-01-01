package me.gb2022.modular.module;

import me.gb2022.modular.pack.ApplicationPackage;

import java.util.Objects;

@SuppressWarnings("rawtypes")
public abstract class AbstractModule implements AppModule {
    private ApplicationPackage parent;
    private ModuleContainer handle;

    @Override
    public final ApplicationPackage parent() {
        return this.parent;
    }

    @Override
    public final ModuleContainer handle() {
        return this.handle;
    }

    @Override
    public void init(String id, ApplicationPackage parent, ModuleContainer handle) {
        this.parent = parent;
        this.handle = handle;
    }

    @Override
    public final String toString() {
        return "%s{%s}".formatted(getClass().getSimpleName(), handle().getMetadata());
    }

    @Override
    public final int hashCode() {
        return this.getFullId().hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (!(obj instanceof AppModule m)) {
            return false;
        }
        return Objects.equals(m.getFullId(), this.getFullId());
    }

    @Override
    protected final Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("cannot clone a module instance!");
    }
}

package me.gb2022.modular.module;

import me.gb2022.modular.pack.IPackage;
import me.gb2022.modular.subcomponent.SubComponent;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class AbstractModule implements IModule {
    private final Map<Class<? extends SubComponent<?>>, SubComponent<?>> components = new HashMap<>();
    protected Logger logger;

    @Override
    public final Map<Class<? extends SubComponent<?>>, SubComponent<?>> getComponents() {
        return components;
    }

    @Override
    public final <I extends SubComponent<?>> void getComponent(Class<I> clazz, Consumer<I> consumer) {
        consumer.accept((I) this.components.get(clazz));
    }

    @Override
    public final <I extends SubComponent<?>> I getComponent(Class<I> clazz) {
        return (I) this.components.get(clazz);
    }

    @Override
    public final Logger logger() {
        return this.logger;
    }

    public abstract Logger createLogger();

    @Override
    public void init(String id, IPackage parent) {
        this.logger = createLogger();
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

package me.gb2022.modular.module;

import me.gb2022.modular.FunctionalComponent;
import me.gb2022.modular.pack.IPackage;

public interface IModule<P extends IPackage, H extends ModuleHandle> extends FunctionalComponent {
    default ApplicationModule descriptor() {
        return this.getClass().getAnnotation(ApplicationModule.class);
    }

    default String version() {
        return this.descriptor().version() + (this.descriptor().beta() ? " - beta" : "");
    }

    default String getFullId() {
        return handle().getMetadata().key().fullId();
    }

    default String id() {
        return handle().getMetadata().key().id();
    }

    P parent();

    H handle();

    default void init(String id, P parent, H handle) {
    }
}

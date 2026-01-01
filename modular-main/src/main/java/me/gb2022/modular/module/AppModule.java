package me.gb2022.modular.module;

import me.gb2022.modular.FunctionalComponent;
import me.gb2022.modular.pack.ApplicationPackage;

public interface AppModule extends FunctionalComponent {
    default void init(String id, ApplicationPackage parent, ModuleContainer handle) {
    }

    default <T> T owner(Class<T> c){
        return this.parent().holder(c);
    }

    default Object owner(){
        return owner(Object.class);
    }

    default String getFullId() {
        return handle().getMetadata().key().fullId();
    }

    default String id() {
        return handle().getMetadata().key().id();
    }

    ApplicationPackage parent();

    ModuleContainer handle();
}

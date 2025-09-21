package me.gb2022.modular.module;

import me.gb2022.modular.FunctionalComponent;
import me.gb2022.modular.subcomponent.SubComponentHolder;
import org.apache.logging.log4j.Logger;
import me.gb2022.modular.pack.IPackage;

public interface IModule extends FunctionalComponent, SubComponentHolder {
    default ApplicationModule descriptor() {
        return this.getClass().getAnnotation(ApplicationModule.class);
    }

    default String version() {
        return this.descriptor().version() + (this.descriptor().beta() ? " - beta" : "");
    }

    default String getFullId() {
        return id();
    }

    default String id() {
        return descriptor().id();
    }

    Logger logger();

    IPackage parent();

    default void enableModule() throws Exception {
        this.enable();

        for (var component : this.getComponents().values()) {
            component.enable();
        }
    }

    default void disableModule() throws Exception {
        for (var component : this.getComponents().values()) {
            component.disable();
        }

        this.disable();
    }


    default void init(String id, IPackage parent){
    }
}

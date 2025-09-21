package me.gb2022.modular.module.meta;

import me.gb2022.modular.module.IModule;
import me.gb2022.modular.FeatureAvailability;
import me.gb2022.modular.FunctionalComponentStatus;
import me.gb2022.modular.pack.IPackage;

public interface ModuleMeta {
    static ModuleMeta dummy(String id) {
        return new DummyModuleMeta(id);
    }

    //----[FIXED]----
    Class<? extends IModule> reference();

    String id();

    String fullId();

    FeatureAvailability available();

    String version();

    boolean defaultEnable();

    boolean beta();

    boolean internal();

    String description();

    IPackage parent();


    //----[META]----
    FunctionalComponentStatus status();

    String additional();

    <I extends IModule> I get(Class<I> type);

    void status(FunctionalComponentStatus functionalComponentStatus);

    void additional(String info);

    IModule handle();

    void handle(IModule handle);

    default boolean unknown() {
        return this instanceof DummyModuleMeta;
    }


    record DummyModuleMeta(String id) implements ModuleMeta {

        @Override
        public String id() {
            return this.id + "[unknown]";
        }

        @Override
        public String fullId() {
            return "unknown:" + this.id;
        }

        @Override
        public String version() {
            return "[unknown]";
        }

        @Override
        public String description() {
            return "no description";
        }

        @Override
        public FunctionalComponentStatus status() {
            return FunctionalComponentStatus.UNKNOWN;
        }

        @Override
        public String additional() {
            return "[unknown or unregistered module]";
        }

        @Override
        public <I extends IModule> I get(Class<I> type) {
            return null;
        }

        @Override
        public IModule handle() {
            return null;
        }

        @Override
        public void status(FunctionalComponentStatus status) {
            throw new UnsupportedOperationException("unknown module!");
        }

        @Override
        public void additional(String info) {
            throw new UnsupportedOperationException("unknown module!");
        }

        @Override
        public void handle(IModule handle) {
            throw new UnsupportedOperationException("unknown module!");
        }

        @Override
        public Class<? extends IModule> reference() {
            return null;
        }

        @Override
        public boolean defaultEnable() {
            return false;
        }

        @Override
        public boolean beta() {
            return false;
        }

        @Override
        public boolean internal() {
            return false;
        }

        @Override
        public FeatureAvailability available() {
            return FeatureAvailability.DEMO_AVAILABLE;
        }

        @Override
        public IPackage parent() {
            return null;
        }
    }

}

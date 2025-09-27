package me.gb2022.modular.module;

import me.gb2022.modular.ComponentMetadata;
import me.gb2022.modular.FunctionalComponentStatus;
import me.gb2022.modular.pack.IPackage;
import me.gb2022.modular.subcomponent.SubComponent;
import me.gb2022.modular.subcomponent.SubComponentHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class ModuleHandle<M extends IModule, H extends ModuleHandle, P extends IPackage> implements SubComponentHolder {
    private final Class<? extends M> reference;
    private final ComponentMetadata metadata;
    private final Logger logger = createLogger();
    private final Map<Class<? extends SubComponent<?>>, SubComponent<?>> components = new HashMap<>();
    private final P parent;
    private FunctionalComponentStatus status = FunctionalComponentStatus.UNKNOWN;
    private M handle;

    protected ModuleHandle(P parent, Class<? extends M> reference, ComponentMetadata metadata) {
        this.parent = parent;
        this.metadata = metadata;
        this.reference = reference;
    }

    public String getLoggerName() {
        return this.reference.getSimpleName();
    }

    public Logger createLogger() {
        return LogManager.getLogger(getLoggerName());
    }

    public final <I extends M> Optional<I> getModule(Class<I> type) {
        return Optional.ofNullable(type.cast(this.handle));
    }

    public final Optional<IModule> getModule() {
        return Optional.ofNullable(this.handle);
    }

    public final ComponentMetadata getMetadata() {
        return this.metadata;
    }

    public final FunctionalComponentStatus getStatus() {
        return status;
    }

    public P getParent() {
        return parent;
    }

    public Class<? extends M> getReference() {
        return reference;
    }

    public final Logger getLogger() {
        return logger;
    }

    public final void register(ModuleManagerV2 mmv2) {
        if (mmv2.validRegister(this)) {
            this.status = FunctionalComponentStatus.REGISTER;
        } else {
            this.status = FunctionalComponentStatus.REGISTER_FAILED;
        }
    }

    public final void construct() {
        if (this.handle != null) {
            throw new IllegalStateException("Module handle already exists");
        }

        try {
            this.handle = this.reference.getDeclaredConstructor().newInstance();
            this.handle.init("__null__", this.parent, this);
            this.handle.initialize();
            this.status = FunctionalComponentStatus.CONSTRUCT;
        } catch (Exception e) {
            this.status = FunctionalComponentStatus.CONSTRUCT_FAILED;
        }
    }

    public final void init(ModuleManagerV2 owner) {
        try {
            this.enable();
        } catch (Exception e) {
            this.status = FunctionalComponentStatus.ENABLE_FAILED;
            owner.handleException(e);
        }
    }

    public final void enable() throws Exception {
        if (this.status == FunctionalComponentStatus.ENABLED) {
            throw new IllegalStateException("already enabled");
        }
        this.preEnable(this.handle);
        this.handle.enable();
        for (var c : this.components.values()) {
            c.enable();
        }
        this.postEnable(this.handle);
        this.status = FunctionalComponentStatus.ENABLED;
    }

    public final void disable() throws Exception {
        if (this.status == FunctionalComponentStatus.DISABLED) {
            throw new IllegalStateException("already enabled");
        }
        this.preDisable(this.handle);
        for (var c : this.components.values()) {
            c.disable();
        }
        this.handle.disable();
        this.postDisable(this.handle);
        this.status = FunctionalComponentStatus.DISABLED;
    }

    public void preEnable(M module) throws Exception {
    }

    public void postEnable(M module) throws Exception {
    }

    public void preDisable(M module) throws Exception {
    }

    public void postDisable(M module) throws Exception {
    }


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
    public int hashCode() {
        return this.metadata.key().hashCode();
    }
}

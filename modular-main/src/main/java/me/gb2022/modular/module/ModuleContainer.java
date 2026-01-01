package me.gb2022.modular.module;

import me.gb2022.modular.APIIncompatibleException;
import me.gb2022.modular.FunctionalComponentStatus;
import me.gb2022.modular.ModularApplicationContext;
import me.gb2022.modular.attachment.SimpleAttachmentContainer;
import me.gb2022.modular.module.attachment.ModuleAttachment;
import me.gb2022.modular.module.attachment.ModuleComponentContainer;
import me.gb2022.modular.pack.ApplicationPackage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ModuleContainer extends SimpleAttachmentContainer<ModuleAttachment> {
    private final ModuleComponentContainer components = new ModuleComponentContainer();
    private final Logger logger;
    private final ApplicationPackage owner;
    private final Class<? extends AppModule> reference;
    private final ModuleMetadata metadata;
    private FunctionalComponentStatus status = FunctionalComponentStatus.UNKNOWN;
    private AppModule handle;

    public ModuleContainer(ApplicationPackage owner, Class<? extends AppModule> reference) {
        this.owner = owner;
        this.reference = reference;
        this.metadata = ModuleMetadata.parse(owner.meta().id(), reference);
        this.logger = LogManager.getLogger(this.reference.getSimpleName());
        this.addAttachment(this.components);
    }


    public <H extends AppModule> H getHandle(Class<H> type) {
        return type.cast(this.handle);
    }

    public ModuleMetadata getMetadata() {
        return this.metadata;
    }

    public FunctionalComponentStatus getStatus() {
        return status;
    }

    public ApplicationPackage getOwner() {
        return this.owner;
    }

    public ModuleComponentContainer getComponentContainer() {
        return components;
    }

    public void register(ModuleManager mm) {
        if (mm.validRegister(this)) {
            this.status = FunctionalComponentStatus.REGISTER;
        } else {
            this.status = FunctionalComponentStatus.REGISTER_FAILED;
        }
    }

    public void initContext(ModularApplicationContext context) {
        for (var h : this.getAttachments().values()) {
            h.initContext(context, this);
        }
    }

    public void construct() {
        if (this.handle != null) {
            //throw new IllegalStateException("Module handle already exists");
        }

        try {
            this.handle = this.reference.getDeclaredConstructor().newInstance();
            this.handle.init("__null__", this.owner, this);
            this.handle.initialize();
            this.handle.checkCompatibility();
            this.status = FunctionalComponentStatus.CONSTRUCT;
        } catch (APIIncompatibleException e) {
            this.status = FunctionalComponentStatus.CONSTRUCT_FAILED;
            this.metadata.appendDescription("ERR_API_INCOMPATIBLE:\n  " + e.getMessage());
        } catch (Throwable e) {
            this.status = FunctionalComponentStatus.CONSTRUCT_FAILED;
            e.printStackTrace();
        }
    }

    public ApplicationPackage getParent() {
        return this.owner;
    }

    public Class<? extends AppModule> getReference() {
        return reference;
    }

    public Logger getLogger() {
        return logger;
    }


    public void init(ModuleManager owner) {
        try {
            this.enable();
        } catch (Throwable e) {
            this.status = FunctionalComponentStatus.ENABLE_FAILED;
            owner.handleException(e);
        }
    }

    public void enable() throws Exception {
        if (this.status == FunctionalComponentStatus.ENABLED) {
            throw new IllegalStateException("already enabled");
        }

        this.handle.enable();
        for (var a : this.getAttachments().values()) {
            a.enable();
        }
        this.status = FunctionalComponentStatus.ENABLED;
    }

    public void disable() throws Exception {
        if (this.status == FunctionalComponentStatus.DISABLED) {
            throw new IllegalStateException("already enabled");
        }
        for (var a : this.getAttachments().values()) {
            a.disable();
        }
        this.handle.disable();
        this.status = FunctionalComponentStatus.DISABLED;
    }


    @Override
    public int hashCode() {
        return this.metadata.key().hashCode();
    }
}

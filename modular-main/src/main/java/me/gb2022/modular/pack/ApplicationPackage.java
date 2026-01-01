package me.gb2022.modular.pack;

import me.gb2022.modular.APIIncompatibleException;
import me.gb2022.modular.FunctionalComponent;
import me.gb2022.modular.ModularApplicationContext;
import me.gb2022.modular.attachment.SimpleAttachmentContainer;
import me.gb2022.modular.module.ModuleContainer;
import me.gb2022.modular.service.ServiceContainer;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public final class ApplicationPackage extends SimpleAttachmentContainer<PackageAttachment> implements FunctionalComponent {
    private final Set<ServiceContainer> services = new HashSet<>();
    private final Set<ModuleContainer> modules = new HashSet<>();
    private final PackageMetadata meta;
    private final Object holder;
    private ModularApplicationContext context;

    public Set<ModuleContainer> getModules() {
        return modules;
    }

    public Set<ServiceContainer> getServices() {
        return services;
    }

    public ApplicationPackage(Object holder, PackageMetadata meta, ContentBuilder builder) {
        this.holder = holder;
        this.meta = meta;

        this.getAttachments().putAll(builder.getAttachments());

        for (var services : builder.getServices()) {
            this.services.add(new ServiceContainer(this, services));
        }
        for (var services : builder.getModules()) {
            this.modules.add(new ModuleContainer(this, services));
        }
    }

    public void initContext(ModularApplicationContext context) {
        this.context = context;
        for (var a : this.getAttachments().values()) {
            a.initContext(context, this);
        }
        for (var c : this.modules) {
            this.context.getModuleManager().initializeModuleContainer(c);
        }
    }

    @Override
    public void initialize() throws Exception {
        for (var a : this.getAttachments().values()) {
            a.initialize();
        }
    }

    @Override
    public void enable() throws Exception {
        for (var a : this.getAttachments().values()) {
            a.enable();
        }

        for (var s : this.services.stream().sorted(Comparator.comparing(t0 -> t0.meta().layer())).toList()) {
            this.context.getServiceManager().addService(s);
        }

        for (var s : this.modules) {
            this.context.getModuleManager().register(s);
        }
    }

    @Override
    public void disable() throws Exception {
        for (var s : this.modules) {
            this.context.getModuleManager().unregister(s.getMetadata().fullId());
        }

        for (var s : this.services) {
            this.context.getServiceManager().removeService(s);
        }

        for (var a : this.getAttachments().values()) {
            a.disable();
        }
    }

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        for (var a : this.getAttachments().values()) {
            a.checkCompatibility();
        }
    }

    public PackageMetadata meta() {
        return this.meta;
    }

    public <H> H holder(Class<H> type) {
        return type.cast(this.holder);
    }
}

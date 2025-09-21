package me.gb2022.modular.module.meta;

import me.gb2022.modular.FeatureAvailability;
import me.gb2022.modular.module.ApplicationModule;
import me.gb2022.modular.module.IModule;
import me.gb2022.modular.pack.IPackage;

public class AnnotationBasedMeta extends AbstractMeta {
    protected final Class<? extends IModule> reference;
    protected final String id;
    protected final String namespace;
    protected final ApplicationModule descriptor;

    public AnnotationBasedMeta(Class<? extends IModule> reference, IPackage parent, String namespace, String id) {
        super(parent);

        this.reference = reference;
        this.id = id;
        this.namespace = namespace;
        this.descriptor = reference.getAnnotation(ApplicationModule.class);
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public String fullId() {
        return this.namespace + ":" + this.id;
    }

    @Override
    public String version() {
        return this.descriptor.version();
    }

    @Override
    public String description() {
        return this.descriptor.description();
    }

    @Override
    public Class<? extends IModule> reference() {
        return this.reference;
    }

    @Override
    public boolean defaultEnable() {
        return this.descriptor.defaultEnable();
    }

    @Override
    public boolean beta() {
        return this.descriptor.beta();
    }

    @Override
    public boolean internal() {
        return this.descriptor.internal();
    }

    @Override
    public FeatureAvailability available() {
        var val = this.descriptor.available();

        if (val == FeatureAvailability.INHERIT) {
            return parent().getAvailability();
        }

        return val;
    }
}

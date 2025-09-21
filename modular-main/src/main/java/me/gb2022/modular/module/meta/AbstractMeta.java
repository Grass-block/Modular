package me.gb2022.modular.module.meta;

import me.gb2022.modular.FunctionalComponentStatus;
import me.gb2022.modular.module.IModule;
import me.gb2022.modular.pack.IPackage;

public abstract class AbstractMeta implements ModuleMeta {
    private final IPackage parent;
    private FunctionalComponentStatus status = FunctionalComponentStatus.UNKNOWN;
    private String additional = "";
    private IModule handle;

    protected AbstractMeta(IPackage parent) {
        this.parent = parent;
    }

    @Override
    public void status(FunctionalComponentStatus status) {
        this.status = status;
    }

    @Override
    public FunctionalComponentStatus status() {
        return this.status;
    }

    @Override
    public void additional(String info) {
        this.additional = info;
    }

    @Override
    public String additional() {
        return this.additional;
    }

    @Override
    public IModule handle() {
        return this.handle;
    }

    @Override
    public <I extends IModule> I get(Class<I> type) {
        return type.cast(this.handle);
    }

    @Override
    public void handle(IModule handle) {
        this.handle = handle;
    }

    @Override
    public IPackage parent() {
        return this.parent;
    }
}

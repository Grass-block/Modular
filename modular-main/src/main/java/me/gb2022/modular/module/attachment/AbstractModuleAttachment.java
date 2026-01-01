package me.gb2022.modular.module.attachment;

import me.gb2022.modular.ModularApplicationContext;
import me.gb2022.modular.module.ModuleContainer;

public abstract class AbstractModuleAttachment implements ModuleAttachment {
    private ModularApplicationContext context;
    private ModuleContainer module;

    @Override
    public void initContext(ModularApplicationContext ctx, ModuleContainer container) {
        this.context = ctx;
        this.module = container;
    }

    public ModuleContainer getModule() {
        return module;
    }

    public ModularApplicationContext getContext() {
        return context;
    }
}


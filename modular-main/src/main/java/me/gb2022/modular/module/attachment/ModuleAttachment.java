package me.gb2022.modular.module.attachment;

import me.gb2022.modular.FunctionalComponent;
import me.gb2022.modular.ModularApplicationContext;
import me.gb2022.modular.module.ModuleContainer;

public interface ModuleAttachment extends FunctionalComponent {
    void initContext(ModularApplicationContext ctx, ModuleContainer container);
}

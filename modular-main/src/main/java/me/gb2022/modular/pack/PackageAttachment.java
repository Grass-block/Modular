package me.gb2022.modular.pack;

import me.gb2022.modular.FunctionalComponent;
import me.gb2022.modular.ModularApplicationContext;

public interface PackageAttachment extends FunctionalComponent {
    void initContext(ModularApplicationContext ctx, ApplicationPackage pkg);
}

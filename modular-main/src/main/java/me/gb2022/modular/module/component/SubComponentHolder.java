package me.gb2022.modular.module.component;

import java.util.Map;
import java.util.function.Consumer;

public interface SubComponentHolder {
    Map<Class<? extends SubComponent<?>>, ? extends SubComponent<?>> getComponents();

    <I extends SubComponent<?>> void getComponent(Class<I> clazz, Consumer<I> consumer);

    <I extends SubComponent<?>> I getComponent(Class<I> clazz);
}

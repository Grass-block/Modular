package me.gb2022.modular.module.attachment;

import me.gb2022.modular.APIIncompatibleException;
import me.gb2022.modular.module.component.SubComponent;
import me.gb2022.modular.module.component.SubComponentHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class ModuleComponentContainer extends AbstractModuleAttachment implements SubComponentHolder {
    private final Map<Class<? extends SubComponent<?>>, SubComponent<?>> components = new HashMap<>();

    @Override
    public void enable() throws Exception {
        for (var c : this.components.values()) {
            c.enable();
        }
    }

    @Override
    public void disable() throws Exception {
        for (var c : this.components.values()) {
            c.disable();
        }
    }

    @Override
    public void checkCompatibility() throws APIIncompatibleException {
        for (var c : this.components.values()) {
            c.checkCompatibility();
        }
    }

    @Override
    public Map<Class<? extends SubComponent<?>>, SubComponent<?>> getComponents() {
        return components;
    }

    @Override
    public <I extends SubComponent<?>> void getComponent(Class<I> clazz, Consumer<I> consumer) {
        consumer.accept((I) this.components.get(clazz));
    }

    @Override
    public <I extends SubComponent<?>> I getComponent(Class<I> clazz) {
        return (I) this.components.get(clazz);
    }

    public void clear() {
        this.components.clear();
    }
}

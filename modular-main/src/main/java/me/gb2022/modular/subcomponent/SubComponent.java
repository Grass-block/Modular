package me.gb2022.modular.subcomponent;

import me.gb2022.modular.FunctionalComponent;

public abstract class SubComponent<E> implements FunctionalComponent {
    protected E parent;

    public SubComponent() {
    }

    public SubComponent(final E parent) {
        ctx(parent);
    }

    public void ctx(E parent) {
        this.parent = parent;
    }

}

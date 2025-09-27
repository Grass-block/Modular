package me.gb2022.modular;

public interface FunctionalComponent {
    default void initialize() throws Exception {
    }

    default void enable() throws Exception {
    }

    default void disable() throws Exception {
    }

    default void checkCompatibility() throws APIIncompatibleException {
    }
}

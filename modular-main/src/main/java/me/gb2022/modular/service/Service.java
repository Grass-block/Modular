package me.gb2022.modular.service;

import me.gb2022.modular.FunctionalComponent;

public interface Service extends FunctionalComponent {
    static String getServiceId(Class<? extends Service> clazz) {
        return clazz.getAnnotation(ApplicationService.class).id();
    }

    static ServiceLayer getServiceLayer(Class<? extends Service> clazz) {
        return clazz.getAnnotation(ApplicationService.class).layer();
    }

    static boolean isLazy(Class<? extends Service> clazz) {
        return clazz.getAnnotation(ApplicationService.class).requiredBy().length != 0;
    }
}

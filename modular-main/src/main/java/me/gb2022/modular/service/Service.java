package me.gb2022.modular.service;

import me.gb2022.modular.Debug;
import me.gb2022.modular.FunctionalComponent;
import me.gb2022.modular.service.ServiceInject;
import me.gb2022.modular.service.ServiceLayer;
import me.gb2022.modular.service.ServiceProvider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

    static boolean hasImplementation(Class<? extends Service> clazz) {
        for (var m : clazz.getDeclaredMethods()) {
            if (m.getAnnotation(ServiceProvider.class) != null) {
                return true;
            }
        }

        var implClass = clazz.getAnnotation(ApplicationService.class).impl();

        return implClass != Service.class;
    }

    static Field getInjection(Class<? extends Service> service) {
        if (!hasImplementation(service)) {
            return null;
        }

        Field injection = null;

        for (var f : service.getDeclaredFields()) {
            if (f.getAnnotation(ServiceInject.class) == null) {
                continue;
            }

            if (injection == null) {
                injection = f;
            } else {
                throw new IllegalArgumentException("find multiple injection point in %s, this will cause BUGS!".formatted(service));
            }
        }

        if (injection != null) {
            injection.setAccessible(true);
        }

        return injection;
    }

    static <E extends Service> void inject(Class<E> service, E instance) throws Exception {
        var injectionPoint = getInjection(service);

        if (injectionPoint != null) {
            Debug.log().info("Found injection for {}: {}", service, injectionPoint);

            var holder = ((ServiceHolder<E>) injectionPoint.get(null));

            if (instance != null) {
                holder.set(null);
            }

            holder.set(instance);
        }

        Method m_start = null;

        try {
            m_start = service.getDeclaredMethod("start");
        } catch (NoSuchMethodException e) {
            try {
                m_start = service.getDeclaredMethod("init");
            } catch (NoSuchMethodException ignored) {
            }
        }

        if (m_start != null && m_start.isAnnotationPresent(ServiceInject.class)) {
            m_start.invoke(null);
        }
    }

    static <E extends Service> void uninject(Class<E> service) throws Exception {
        try {
            var m_stop = service.getDeclaredMethod("stop");

            if (m_stop.isAnnotationPresent(ServiceInject.class)) {
                m_stop.invoke(null);
            }
        } catch (NoSuchMethodException ignored) {
        }

        var injectionPoint = getInjection(service);

        if (injectionPoint != null) {
            var holder = ((ServiceHolder<E>) injectionPoint.get(null));

            holder.set(null);
        }
    }
}

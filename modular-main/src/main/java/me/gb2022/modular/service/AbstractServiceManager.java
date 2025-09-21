package me.gb2022.modular.service;

import me.gb2022.modular.APIIncompatibleException;
import me.gb2022.modular.service.injection.Export;
import me.gb2022.modular.service.injection.ServiceInject;
import me.gb2022.modular.service.injection.ServiceProvider;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

public abstract class AbstractServiceManager<I extends Service> implements ServiceManager<I> {
    private final Logger logger = getLogger();
    private final HashMap<String, Class<? extends I>> services = new HashMap<>(24);

    static boolean hasImplementation(Class<? extends Service> clazz) {
        for (var m : clazz.getDeclaredMethods()) {
            if (m.getAnnotation(ServiceProvider.class) != null) {
                return true;
            }
        }

        var implClass = clazz.getAnnotation(ApplicationService.class).impl();

        return implClass != Service.class;
    }

    static boolean isLazy(Class<? extends Service> clazz) {
        return clazz.getAnnotation(ApplicationService.class).requiredBy().length != 0;
    }


    private Field getInjection(Class<? extends Service> service) {
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

    private <E extends I> void inject(Class<E> service, String id, Field inject) {
        try {
            var holder = ((ServiceHolder<E>) inject.get(null));
            var instance = createImplementation(service, id);

            if (instance == null) {
                logger.warn("service {} has null impl created(may caused by api error)", id);
                holder.set(null);
                return;
            }

            try {
                instance.checkCompatibility();
            } catch (APIIncompatibleException e) {
                logger.warn("service {} failed compat check: {}", id, e.getCause().toString());
                holder.set(null);
                return;
            }

            holder.set(instance);

            if (inject.isAnnotationPresent(Export.class)) {
                this.exportService(holder.get(), service);
            }

            holder.get().disable();
        } catch (Throwable e) {
            logger.error("failed to set implementation for service [{}]:", id);
            this.handleException(e);
        }
    }

    @Override
    public <E extends I> void registerService(Class<E> service) {
        var id = Service.getServiceId(service);

        if (this.services.containsKey(id)) {
            throw new RuntimeException("exist registered service: %s".formatted(id));
        }
        this.services.put(id, service);

        var inject = getInjection(service);

        if (inject != null) {
            inject(service, id, inject);
        }

        try {
            Method m = service.getMethod("start");

            if (m.getAnnotation(ServiceInject.class) == null) {
                return;
            }

            try {
                m.invoke(null);
            } catch (Throwable e) {
                this.handleException(e);
            }

        } catch (NoSuchMethodException ignored) {
        }
    }

    @Override
    public void unregisterService(Class<? extends I> service) {
        var id = Service.getServiceId(service);
        this.services.remove(id);

        try {
            Method m = service.getMethod("stop");

            if (m.getAnnotation(ServiceInject.class) != null) {
                m.invoke(null);
            }
        } catch (NoSuchMethodException ignored) {
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        var inject = getInjection(service);

        if (inject == null) {
            return;
        }

        try {
            var handle = service.cast(((ServiceHolder<?>) inject.get(null)).get());

            if (handle == null) {
                return;
            }

            if (inject.isAnnotationPresent(Export.class)) {
                this.unregisterExportedService(service);
            }

            handle.disable();
        } catch (Throwable e) {
            this.logger.error("failed to stop implementation for [{}]", id);
            this.handleException(e);
        }
    }

    @Override
    public <E extends I> Class<E> getService(String id, Class<Class<E>> type) {
        return type.cast(this.services.get(id));
    }

    @Override
    public void unregisterAllServices(ServiceLayer layer) {
        for (var serviceClass : new HashSet<>(this.services.values())) {
            if (Service.getServiceLayer(serviceClass) != layer) {
                continue;
            }
            this.unregisterService(serviceClass);
        }
    }
}

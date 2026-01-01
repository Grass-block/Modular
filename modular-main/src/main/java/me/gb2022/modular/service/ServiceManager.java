package me.gb2022.modular.service;

import me.gb2022.modular.APIIncompatibleException;
import me.gb2022.modular.Debug;
import me.gb2022.modular.ModularApplicationContext;
import me.gb2022.modular.service.ApplicationService;
import me.gb2022.modular.service.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceManager {
    private final ModularApplicationContext context;
    private final Map<String, ServiceContainer> services = new ConcurrentHashMap<>(24);
    Logger LOGGER = LogManager.getLogger("ServiceManager");

    public ServiceManager(ModularApplicationContext context) {
        this.context = context;
    }

    public final Optional<ServiceContainer> getService(String fullId) {
        return Optional.ofNullable(this.services.get(fullId));
    }

    public final void addService(ServiceContainer container) {
        container.initContext(this.context);

        try {
            container.checkCompatibility();
        } catch (APIIncompatibleException e) {
            throw new RuntimeException(e);
        }

        try {
            container.initialize();
            container.enable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        this.services.put(container.meta().fullId(), container);
    }

    public final void removeService(ServiceContainer container) {
        var o = this.getService(container.meta().fullId());

        if (o.isEmpty()) {
            return;
        }

        var cc = o.get();

        if (cc != container) {
            throw new ConcurrentModificationException("ServiceContainer object mismatch!");
        }

        try {
            cc.disable();
        } catch (Exception e) {
            LOGGER.warn("Failed to stop service {}, THIS MAY CAUSE BUG!!!", container.meta().fullId());
            LOGGER.catching(e);
        } finally {
            this.services.remove(container.meta().fullId());
        }
    }

    public final void removeAll(ServiceLayer layer) {
        for (var container : new HashSet<>(this.services.values())) {
            if (Service.getServiceLayer(container.getHandle()) != layer) {
                continue;
            }
            this.removeService(container);
        }
    }

    public Service createImplementation(ServiceContainer serviceContainer, Class<Service> clazz) {
        Debug.log().info("creating impl of {}", clazz);
        return createImplementation(clazz);
    }

    public final Service createImplementation(Class<? extends Service> clazz, Object... args) {
        var paramClasses = Arrays.stream(args).map(Object::getClass).toArray(Class[]::new);

        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getAnnotation(ServiceProvider.class) == null) {
                continue;
            }

            Debug.log().info("Found service provider: {}", m.getName());

            try {
                if (m.getParameterTypes().length == 0) {
                    Debug.log().info("Using empty create: {}", m.getName());
                    return (Service) m.invoke(null);
                }

                Debug.log().info("Using user create with {}: {}", Arrays.toString(args), m.getName());
                return (Service) m.invoke(null, args);
            } catch (NoClassDefFoundError ignored) {
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (e.getCause() instanceof APIIncompatibleException) {
                    return null;
                }
                if (e.getCause() instanceof NoClassDefFoundError) {
                    return null;
                }

                throw new RuntimeException(e);
            }
        }

        var implClass = clazz.getAnnotation(ApplicationService.class).impl();

        if (implClass == Service.class) {
            Debug.log().info("No service provider or registered impl: {}", clazz.getName());
            return null;
        }

        try {
            Debug.log().info("Using user arg constructor {}: {}", Arrays.toString(args), clazz.getName());

            return implClass.getDeclaredConstructor(paramClasses).newInstance(args);
        } catch (NoSuchMethodException e) {
            try {
                Debug.log().info("Using empty constructor {}: {}", Arrays.toString(args), clazz.getName());
                return implClass.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                if (e.getCause() instanceof APIIncompatibleException) {
                    return null;
                }
                if (e.getCause() instanceof NoClassDefFoundError) {
                    return null;
                }
                throw new RuntimeException(ex);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
            if (ex.getCause() instanceof APIIncompatibleException) {
                return null;
            }
            if (ex.getCause() instanceof NoClassDefFoundError) {
                return null;
            }
            throw new RuntimeException(ex);
        }
    }

    public <E extends Service> void exportService(E object, Class<E> type) {
    }

    public void unregisterExportedService(Class<? extends Service> type) {
    }

    protected final ModularApplicationContext context() {
        return this.context;
    }

    public Map<String, ServiceContainer> all() {
        return this.services;
    }
}

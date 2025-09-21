package me.gb2022.modular.service;

import org.apache.logging.log4j.Logger;

public interface ServiceManager<I extends Service> {
    Logger getLogger();

    <E extends I> E createImplementation(Class<E> service, String id);

    <E extends I> void exportService(E object, Class<E> type);

    <E extends I> void registerService(Class<E> service);

    void handleException(Throwable e);

    void unregisterService(Class<? extends I> service);

    void unregisterExportedService(Class<? extends I> type);

    <E extends I> Class<E> getService(String id, Class<Class<E>> type);

    void unregisterAllServices(ServiceLayer layer);
}

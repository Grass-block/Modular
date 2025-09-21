package me.gb2022.modular.pack;

import me.gb2022.commons.TriState;
import me.gb2022.modular.ObjectOperationResult;

import java.util.Map;

public interface PackageManager<P extends IPackage<?, ?, ?>> {
    P get(String id);

    Map<String, P> getPackages();

    void addPackage(P pkg);

    void removePackage(String id);

    ObjectOperationResult enable(String id);

    ObjectOperationResult disable(String id);

    TriState getStatus(String id);
}

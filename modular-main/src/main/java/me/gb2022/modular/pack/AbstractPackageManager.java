package me.gb2022.modular.pack;

import me.gb2022.commons.TriState;
import me.gb2022.modular.ObjectOperationResult;
import org.apache.logging.log4j.Logger;

import java.util.*;


public abstract class AbstractPackageManager<P extends IPackage<?, ?, ?>> implements PackageManager<P> {
    private final Map<String, P> packages = new HashMap<>();
    protected final Properties statusMap = new Properties();
    private final Logger logger = getLogger();

    public abstract void saveStatus(Properties meta);

    public abstract void handleException(Throwable e);

    public abstract Logger getLogger();

    public abstract boolean isReservedPackage(P pack);

    public abstract boolean defaultPackageStatus(P pack);

    @Override
    public P get(String id) {
        return this.packages.get(id);
    }

    @Override
    public Map<String, P> getPackages() {
        return this.packages;
    }

    @Override
    public void addPackage(P pkg) {
        try {
            pkg.initializePackage();
            this.packages.put(pkg.getId(), pkg);
            if (getStatus(pkg.getId()) == TriState.UNKNOWN) {
                var enable = defaultPackageStatus(pkg);
                this.statusMap.put(pkg.getId(), enable ? "enabled" : "disabled");
                this.saveStatus();
            }
            if (isReservedPackage(pkg)) {
                this.statusMap.put(pkg.getId(), "enabled");
            }
            if (getStatus(pkg.getId()) == TriState.FALSE) {
                try {
                    pkg.onEnable();
                } catch (Exception ex) {
                    this.handleException(ex);
                }
            }
        } catch (Exception e) {
            this.handleException(e);
        }
    }

    @Override
    public void removePackage(String id) {
        if (this.getStatus(id) == TriState.TRUE) {
            return;
        }
        if (!this.packages.containsKey(id)) {
            return;
        }
        this.packages.get(id).onDisable();
        this.packages.remove(id);
    }

    private void saveStatus() {
        this.saveStatus(this.statusMap);
    }

    private ObjectOperationResult enable0(String id) {
        if (getStatus(id) == TriState.UNKNOWN) {
            return ObjectOperationResult.NOT_FOUND;
        }
        if (getStatus(id) == TriState.FALSE) {
            return ObjectOperationResult.ALREADY_OPERATED;
        }
        try {
            this.get(id).onEnable();
        } catch (Exception ex) {
            this.handleException(ex);
            return ObjectOperationResult.INTERNAL_ERROR;
        }
        this.statusMap.put(id, "enabled");
        return ObjectOperationResult.SUCCESS;
    }

    private ObjectOperationResult disable0(String id) {
        if (getStatus(id) == TriState.UNKNOWN) {
            return ObjectOperationResult.NOT_FOUND;
        }
        if (getStatus(id) == TriState.TRUE) {
            return ObjectOperationResult.ALREADY_OPERATED;
        }
        try {
            this.get(id).onDisable();
        } catch (Exception ex) {
            this.handleException(ex);
            return ObjectOperationResult.INTERNAL_ERROR;
        }
        this.statusMap.put(id, "disabled");
        return ObjectOperationResult.SUCCESS;
    }

    @Override
    public ObjectOperationResult enable(String id) {
        if (isReservedPackage(get(id))) {
            return ObjectOperationResult.BLOCKED_INTERNAL;
        }
        ObjectOperationResult result = enable0(id);
        if (result == ObjectOperationResult.SUCCESS) {
            this.logger.info("enabled module %s.".formatted(id));
        }
        this.saveStatus();
        return result;
    }

    @Override
    public ObjectOperationResult disable(String id) {
        if (isReservedPackage(get(id))) {
            return ObjectOperationResult.BLOCKED_INTERNAL;
        }
        ObjectOperationResult result = disable0(id);
        if (result == ObjectOperationResult.SUCCESS) {
            this.logger.info("disabled module %s.".formatted(id));
        }
        this.saveStatus();
        return result;
    }

    @Override
    public TriState getStatus(String id) {
        if (!this.statusMap.containsKey(id)) {
            return TriState.UNKNOWN;
        }
        return Objects.equals(this.statusMap.get(id), "enabled") ? TriState.FALSE : TriState.TRUE;
    }

    public void onDisable() {
        this.saveStatus();
        for (String id : new ArrayList<>(this.getPackages().keySet())) {
            this.removePackage(id);
        }
    }
}

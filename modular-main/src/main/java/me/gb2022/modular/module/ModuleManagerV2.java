package me.gb2022.modular.module;

import me.gb2022.commons.TriState;
import me.gb2022.modular.FunctionalComponentStatus;
import me.gb2022.modular.ObjectOperationResult;
import org.apache.logging.log4j.Logger;

import java.util.*;

public abstract class ModuleManagerV2<M extends IModule<?, H>, H extends ModuleHandle> {
    protected final Logger logger;
    protected final Map<String, H> modules = new HashMap<>();
    protected final Properties statusMap = new Properties();

    public ModuleManagerV2(Logger logger) {
        this.logger = logger;
    }

    public void enable() {
    }

    public void disable() {
        this.saveStatus();
        for (var id : new ArrayList<>(this.getModules().keySet())) {
            this.unregister(id);
        }
    }

    public void register(H handle) {
        this.modules.put(handle.getMetadata().key().fullId(), handle);
        handle.register(this);

        if (handle.getStatus() == FunctionalComponentStatus.REGISTER_FAILED) {
            return;
        }

        handle.construct();

        if (handle.getStatus() == FunctionalComponentStatus.CONSTRUCT_FAILED) {
            return;
        }

        var meta = handle.getMetadata();
        var id = meta.key().fullId();

        if (getStatus(id) == TriState.UNKNOWN) {
            var status = false;

            if (meta.defaultEnabled()) {
                status = getDefaultModuleStatus();
            }

            this.statusMap.put(id, status ? "enabled" : "disabled");
        }
        if (meta.internal()) {
            this.statusMap.put(meta.fullId(), "enabled");
        }

        this.saveStatus();
        if (getStatus(id) == TriState.TRUE && !meta.beta()) {
            handle.init(this);
        }
    }

    public void unregister(String id) {
        if (!this.modules.containsKey(id)) {
            if (!this.modules.isEmpty()) {
                this.logger.warn("Module with id {} not found", id);
            }
            return;
        }

        if (this.getStatus(id) == TriState.TRUE) {
            var m = this.get(id).orElseThrow();

            if (m.getStatus() != FunctionalComponentStatus.ENABLED && m.getStatus() != FunctionalComponentStatus.DISABLED) {
                this.modules.remove(id);
                return;
            }

            try {
                m.disable();
            } catch (Throwable ex) {
                this.handleException(ex);
            }
        }

        this.modules.remove(id);
    }

    private void saveStatus() {
        this.saveStatus(this.statusMap);
    }

    public Properties getStatusMap() {
        return statusMap;
    }

    public Map<String, H> getModules() {
        return modules;
    }

    public Optional<H> get(String id) {
        return Optional.ofNullable(this.modules.get(id));
    }

    public TriState getStatus(String id) {
        if (!this.statusMap.containsKey(id)) {
            return TriState.UNKNOWN;
        }
        return Objects.equals(this.statusMap.get(id), "enabled") ? TriState.TRUE : TriState.FALSE;
    }

    public Set<String> getIdsByStatus(TriState status) {
        var result = new HashSet<String>();
        for (var id : this.modules.keySet()) {
            if (getStatus(id) != status) {
                continue;
            }
            result.add(id);
        }
        return result;
    }

    public final ObjectOperationResult enable(String id) {
        if (get(id).orElseThrow().getMetadata().internal()) {
            return ObjectOperationResult.BLOCKED_INTERNAL;
        }

        var result = enable0(id);

        if (result == ObjectOperationResult.SUCCESS) {
            this.logger.info("enabled module {}.", id);
        }

        this.saveStatus();
        return result;
    }

    public final ObjectOperationResult disable(String id) {
        if (get(id).orElseThrow().getMetadata().internal()) {
            return ObjectOperationResult.BLOCKED_INTERNAL;
        }

        var result = disable0(id);

        if (result == ObjectOperationResult.SUCCESS) {
            this.logger.info("disabled module {}.", id);
        }

        this.saveStatus();
        return result;
    }

    public final ObjectOperationResult reload(String id) {
        ObjectOperationResult result = this.disable(id);
        if (result != ObjectOperationResult.SUCCESS) {
            return result;
        }
        return enable(id);
    }

    private ObjectOperationResult checkState0(H handle, FunctionalComponentStatus state) {
        if (handle == null || handle.getStatus() == FunctionalComponentStatus.UNKNOWN) {
            return ObjectOperationResult.NOT_FOUND;
        }

        if (handle.getStatus() == state) {
            return ObjectOperationResult.ALREADY_OPERATED;
        }

        return ObjectOperationResult.INTERNAL_ERROR;
    }

    private ObjectOperationResult enable0(String id) {
        var meta = this.get(id).orElse(null);
        var result = checkState0(meta, FunctionalComponentStatus.ENABLED);

        if (result != ObjectOperationResult.INTERNAL_ERROR) {
            return result;
        }

        this.handlePreEnable(meta);

        try {
            assert meta != null;
            meta.enable();
            result = ObjectOperationResult.SUCCESS;
        } catch (Exception ex) {
            this.handleException(ex);
        }

        this.handlePostEnable(meta, result);

        if (result == ObjectOperationResult.SUCCESS) {
            this.statusMap.put(id, "enabled");
        }

        return result;
    }

    private ObjectOperationResult disable0(String id) {
        var meta = this.get(id).orElse(null);
        var result = checkState0(meta, FunctionalComponentStatus.DISABLED);

        if (result != ObjectOperationResult.INTERNAL_ERROR) {
            return result;
        }

        this.handlePreDisable(meta);

        try {
            assert meta != null;
            meta.disable();
            result = ObjectOperationResult.SUCCESS;
        } catch (Exception ex) {
            this.handleException(ex);
        }

        this.handlePostDisable(meta, result);

        if (result == ObjectOperationResult.SUCCESS) {
            this.statusMap.put(id, "disabled");
        }

        return result;
    }


    public boolean validRegister(H handle) {
        return true;
    }

    public boolean getDefaultModuleStatus() {
        return true;
    }

    public abstract void saveStatus(Properties meta);

    public abstract void handleException(Throwable ex);

    public void handlePreEnable(H handle) {
    }

    public void handlePostEnable(H handle, ObjectOperationResult result) {
    }

    public void handlePreDisable(H handle) {
    }

    public void handlePostDisable(H handle, ObjectOperationResult result) {
    }


    public final void enableAll() {
        for (String id : this.getModules().keySet()) {
            this.enable(id);
        }
    }

    public final void disableAll() {
        for (String id : this.getModules().keySet()) {
            this.disable(id);
        }
    }

    public final void reloadAll() {
        List<String> list = new ArrayList<>();
        for (String id : this.getModules().keySet()) {
            if (this.disable(id) != ObjectOperationResult.SUCCESS) {
                continue;
            }
            list.add(id);
        }
        for (String s : list) {
            this.enable(s);
        }
    }
}

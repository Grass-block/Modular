package me.gb2022.modular.module;

import me.gb2022.commons.TriState;
import me.gb2022.modular.FunctionalComponentStatus;
import me.gb2022.modular.ModularApplicationContext;
import me.gb2022.modular.ObjectOperationResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ModuleManager {
    protected final Logger logger = createLogger();
    protected final Map<String, ModuleContainer> modules = new HashMap<>();
    protected final Properties statusMap = new Properties();
    private final ModularApplicationContext context;

    public ModuleManager(ModularApplicationContext context) {
        this.context = context;
    }

    public Logger createLogger() {
        return LogManager.getLogger("ModuleManager");
    }

    public void enable() {
    }

    public void disable() {
        this.saveStatus();
        for (var id : new ArrayList<>(this.getModules().keySet())) {
            this.unregister(id);
        }
    }

    public ModularApplicationContext getContext() {
        return context;
    }

    public Logger getLogger() {
        return LogManager.getLogger("PackageManager");
    }

    public void register(ModuleContainer handle) {
        this.modules.put(handle.getMetadata().key().fullId(), handle);
        handle.register(this);

        if (handle.getStatus() == FunctionalComponentStatus.REGISTER_FAILED) {
            return;
        }

        handle.initContext(this.context);
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
            this.handlePreEnable(handle);
            handle.init(this);
            this.handlePostEnable(handle, ObjectOperationResult.SUCCESS);
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

    public final Properties getStatusMap() {
        return statusMap;
    }

    public final Map<String, ModuleContainer> getModules() {
        return modules;
    }

    public final Optional<ModuleContainer> get(String id) {
        return Optional.ofNullable(this.modules.get(id));
    }

    public final TriState getStatus(String id) {
        if (!this.statusMap.containsKey(id)) {
            return TriState.UNKNOWN;
        }
        return Objects.equals(this.statusMap.get(id), "enabled") ? TriState.TRUE : TriState.FALSE;
    }

    public final Set<String> getIdsByStatus(TriState status) {
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

    private ObjectOperationResult checkState0(ModuleContainer handle, FunctionalComponentStatus state) {
        if (handle == null || handle.getStatus() == FunctionalComponentStatus.UNKNOWN) {
            return ObjectOperationResult.NOT_FOUND;
        }

        if (handle.getStatus() == state) {
            return ObjectOperationResult.ALREADY_OPERATED;
        }

        return ObjectOperationResult.INTERNAL_ERROR;
    }

    private ObjectOperationResult enable0(String id) {
        var handle = this.get(id).orElse(null);
        var result = checkState0(handle, FunctionalComponentStatus.ENABLED);

        if (result != ObjectOperationResult.INTERNAL_ERROR) {
            return result;
        }

        this.handlePreEnable(handle);

        try {
            assert handle != null;
            handle.enable();
            result = ObjectOperationResult.SUCCESS;
        } catch (Exception ex) {
            this.handleException(ex);
        }

        this.handlePostEnable(handle, result);

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

    private void saveStatus() {
        this.saveStatus(this.statusMap);
    }


    public boolean validRegister(ModuleContainer handle) {
        return true;
    }

    public boolean getDefaultModuleStatus() {
        return true;
    }

    public void saveStatus(Properties meta) {
    }

    public void handleException(Throwable ex) {
        this.logger.catching(ex);
    }

    public void handlePreEnable(ModuleContainer handle) {
    }

    public void handlePostEnable(ModuleContainer handle, ObjectOperationResult result) {
    }

    public void handlePreDisable(ModuleContainer handle) {
    }

    public void handlePostDisable(ModuleContainer handle, ObjectOperationResult result) {
    }

    public void initializeModuleContainer(ModuleContainer handle) {
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
        var list = new ArrayList<String>();
        for (String id : this.getModules().keySet()) {
            if (this.disable(id) != ObjectOperationResult.SUCCESS) {
                continue;
            }
            list.add(id);
        }
        for (var s : list) {
            this.enable(s);
        }
    }
}

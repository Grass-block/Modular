package me.gb2022.modular.module;

import me.gb2022.commons.TriState;
import me.gb2022.modular.APIIncompatibleException;
import me.gb2022.modular.FunctionalComponentStatus;
import me.gb2022.modular.ObjectOperationResult;
import me.gb2022.modular.module.meta.ModuleMeta;
import org.apache.logging.log4j.Logger;

import java.util.*;

public abstract class ModuleManager<M extends ModuleMeta, H extends IModule> {
    protected final Logger logger;
    protected final Map<String, H> moduleMap = new HashMap<>();
    protected final Properties statusMap = new Properties();
    protected final Map<String, M> metas = new HashMap<>();

    public ModuleManager(Logger logger) {
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

    //----[query]----
    public TriState getModuleStatus(String id) {
        return this.getStatus(id);
    }

    public Set<String> getIdsByStatus(TriState status) {
        Set<String> result = new HashSet<>();
        for (String id : this.getKnownModuleMetas().keySet()) {
            if (getModuleStatus(id) != status) {
                continue;
            }
            result.add(id);
        }
        return result;
    }

    public M getMeta(String id) {
        if (!this.metas.containsKey(id)) {
            throw new IllegalArgumentException("Meta not found: " + id);
        }

        return this.metas.get(id);
    }

    public H get(String id) {
        return this.moduleMap.get(id);
    }

    public Map<String, H> getModules() {
        return this.moduleMap;
    }

    public Map<String, M> getKnownModuleMetas() {
        return this.metas;
    }

    public TriState getStatus(String id) {
        if (!this.statusMap.containsKey(id)) {
            return TriState.UNKNOWN;
        }
        return Objects.equals(this.statusMap.get(id), "enabled") ? TriState.TRUE : TriState.FALSE;
    }

    private void saveStatus() {
        this.saveStatus(this.statusMap);
    }

    public abstract void saveStatus(Properties meta);


    //----[operation]----
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

    public final ObjectOperationResult enable(String id) {
        if (get(id).descriptor().internal()) {
            return ObjectOperationResult.BLOCKED_INTERNAL;
        }

        var result = enable0(id);

        if (result == ObjectOperationResult.SUCCESS) {
            this.logger.info("enabled module %s.".formatted(id));
        }

        this.saveStatus();
        return result;
    }

    public final ObjectOperationResult disable(String id) {
        if (get(id).descriptor().internal()) {
            return ObjectOperationResult.BLOCKED_INTERNAL;
        }

        var result = disable0(id);

        if (result == ObjectOperationResult.SUCCESS) {
            this.logger.info("disabled module %s.".formatted(id));
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

    private ObjectOperationResult checkState0(ModuleMeta meta, FunctionalComponentStatus state) {
        if (meta == null || meta.unknown() || meta.status() == FunctionalComponentStatus.UNKNOWN) {
            return ObjectOperationResult.NOT_FOUND;
        }

        if (meta.status() == state) {
            return ObjectOperationResult.ALREADY_OPERATED;
        }

        return ObjectOperationResult.INTERNAL_ERROR;
    }

    private ObjectOperationResult enable0(String id) {
        var meta = this.getMeta(id);
        var result = checkState0(meta, FunctionalComponentStatus.ENABLE);

        if (result != ObjectOperationResult.INTERNAL_ERROR) {
            return result;
        }

        this.handlePreEnable(meta);

        try {
            meta.handle().enableModule();
            result = ObjectOperationResult.SUCCESS;
        } catch (Exception ex) {
            this.handleException(ex);
        }

        this.handlePostEnable(meta, result);

        if (result == ObjectOperationResult.SUCCESS) {
            this.statusMap.put(id, "enabled");
            meta.status(FunctionalComponentStatus.ENABLE);
        }

        return result;
    }

    public abstract void handleException(Throwable ex);

    public void handlePreEnable(M meta) {

    }

    public void handlePostEnable(M meta, ObjectOperationResult result) {

    }

    public void handlePreDisable(M meta) {

    }

    public void handlePostDisable(M meta, ObjectOperationResult result) {

    }

    private ObjectOperationResult disable0(String id) {
        var meta = this.getMeta(id);
        var result = checkState0(meta, FunctionalComponentStatus.DISABLED);

        if (result != ObjectOperationResult.INTERNAL_ERROR) {
            return result;
        }

        this.handlePreDisable(meta);

        try {
            meta.handle().disableModule();
            result = ObjectOperationResult.SUCCESS;
        } catch (Exception ex) {
            this.handleException(ex);
        }

        this.handlePostDisable(meta, result);

        if (result == ObjectOperationResult.SUCCESS) {
            this.statusMap.put(id, "disabled");
            meta.status(FunctionalComponentStatus.DISABLED);
        }

        return result;
    }


    //----[register]----
    public boolean validPlatform(M meta) {
        return false;
    }

    private boolean validFeature(M meta) {
        if (meta.available().load()) {
            return false;
        }

        meta.status(FunctionalComponentStatus.REGISTER_FAILED);
        meta.additional("NOT_IN_CURRENT_PRODUCT_SETTING");
        return true;
    }

    private boolean construct(ModuleMeta meta) {
        var clazz = meta.reference();

        try {
            var m = (H) clazz.getDeclaredConstructor().newInstance();
            m.init(meta.id(), meta.parent());
            meta.handle(m);
            meta.status(FunctionalComponentStatus.CONSTRUCT);
        } catch (Throwable e) {
            handleException(e);
            meta.status(FunctionalComponentStatus.CONSTRUCT_FAILED);

            if (e.getCause() != null) {
                meta.additional(e.getCause().getClass().getSimpleName() + "[" + e.getCause().getMessage() + "]");
            }

            return true;
        }

        return false;
    }

    private boolean validAPI(ModuleMeta meta) {
        try {
            meta.handle().checkCompatibility();
        } catch (APIIncompatibleException e) {
            meta.status(FunctionalComponentStatus.REGISTER_FAILED);
            meta.additional("ERROR_INCOMPATIBLE_API: " + e.getMessage());
            return true;
        }

        return false;
    }

    public boolean getDefaultModuleStatus() {
        return true;
    }

    public void registerMeta(M meta) {
        this.metas.put(meta.fullId(), meta);

        if (meta.unknown()) {
            return;
        }
        if (validFeature(meta)) {
            return;
        }
        if (validPlatform(meta)) {
            return;
        }
        if (meta.handle() == null && construct(meta)) {
            return;
        }
        if (validAPI(meta)) {
            return;
        }

        var m = meta.handle();

        this.moduleMap.put(m.getFullId(), (H) m);
        meta.status(FunctionalComponentStatus.REGISTER);

        if (getModuleStatus(m.getFullId()) == TriState.UNKNOWN) {
            boolean status = false;

            if (m.descriptor().defaultEnable()) {
                status = getDefaultModuleStatus();
            }

            this.statusMap.put(m.getFullId(), status ? "enabled" : "disabled");
        }
        if (m.descriptor().internal()) {
            this.statusMap.put(m.getFullId(), "enabled");
        }

        this.saveStatus();
        if (getModuleStatus(m.getFullId()) == TriState.TRUE && !m.descriptor().beta()) {
            try {
                this.get(m.getFullId()).enableModule();
                meta.status(FunctionalComponentStatus.ENABLE);
            } catch (Exception ex) {
                meta.status(FunctionalComponentStatus.ENABLE_FAILED);
                meta.additional(ex.getMessage());
                this.handleException(ex);
            }
        }
    }

    public void register(H m) {
        if (m == null) {
            return;
        }

        var meta = getMetadata(m);
        this.metas.put(m.getFullId(), meta);

        registerMeta(meta);
    }

    public abstract M getMetadata(H handle);

    public void unregister(String id) {
        if (this.getStatus(id) == TriState.TRUE) {
            var m = this.get(id);
            if (m != null) {
                try {
                    m.disableModule();
                } catch (Throwable ex) {
                    this.handleException(ex);
                }
            }
        }

        this.moduleMap.remove(id);
        this.getKnownModuleMetas().remove(id);
    }
}

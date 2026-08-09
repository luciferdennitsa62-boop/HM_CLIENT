package com.client.impl.module;

import com.client.impl.module.modules.misc.Fullbright;
import com.client.impl.module.modules.movement.AutoSprint;
import com.client.impl.module.modules.render.Tracers;

import java.util.ArrayList;
import java.util.List;

public final class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public void registerModules() {
        modules.clear();
        modules.add(new Fullbright());
        modules.add(new AutoSprint());
        modules.add(new Tracers());
    }

    public List<Module> getModules() {
        return List.copyOf(modules);
    }

    public List<Module> getModulesByCategory(Category category) {
        return modules.stream().filter(module -> module.getCategory() == category).toList();
    }

    public Module getModuleByName(String name) {
        return modules.stream()
                .filter(module -> module.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public void handleKeyPress(int keyCode) {
        if (keyCode < 0) return;
        for (Module module : modules) {
            if (module.getKeyBind() == keyCode) {
                module.toggle();
            }
        }
    }
}

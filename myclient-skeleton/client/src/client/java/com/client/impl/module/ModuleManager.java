package com.client.impl.module;

import com.client.impl.module.modules.misc.Fullbright;
import com.client.impl.module.modules.movement.AutoSprint;
import com.client.impl.module.modules.render.Tracers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ModuleManager {

	private final List<Module> modules = new ArrayList<>();

	/**
	 * Тут регистрируются все модули клиента. Добавляешь новый модуль —
	 * создаёшь класс в impl/module/modules/<категория>/ и добавляешь строку сюда.
	 */
	public void registerModules() {
		modules.add(new Fullbright());
		modules.add(new AutoSprint());
		modules.add(new Tracers());
	}

	public List<Module> getModules() {
		return modules;
	}

	public List<Module> getModulesByCategory(Category category) {
		return modules.stream()
				.filter(m -> m.getCategory() == category)
				.collect(Collectors.toList());
	}

	public Module getModuleByName(String name) {
		for (Module module : modules) {
			if (module.getName().equalsIgnoreCase(name)) return module;
		}
		return null;
	}

	/** Вызывать из клавиатурного события клиента, чтобы модули реагировали на свои keybind'ы. */
	public void handleKeyPress(int keyCode) {
		for (Module module : modules) {
			if (module.getKeyBind() == keyCode && keyCode != -1) {
				module.toggle();
			}
		}
	}
}

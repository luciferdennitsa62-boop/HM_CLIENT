package com.client;

import com.client.event.EventBus;
import com.client.impl.module.ModuleManager;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.client.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Точка входа клиента. Тут инициализируется всё: event bus, менеджер модулей,
 * биндинг клавиши открытия ClickGUI.
 */
public class MyClient implements ClientModInitializer {

	public static final String MOD_ID = "myclient";
	public static final String CLIENT_NAME = "MyClient";
	public static final String CLIENT_VERSION = "0.1.0";

	private static MyClient instance;

	private EventBus eventBus;
	private ModuleManager moduleManager;

	private KeyMapping openGuiKeyBinding;

	@Override
	public void onInitializeClient() {
		instance = this;

		this.eventBus = new EventBus();
		this.moduleManager = new ModuleManager();
		this.moduleManager.registerModules();

		// Биндинг клавиши открытия ClickGUI — по умолчанию Right Shift
		this.openGuiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key." + MOD_ID + ".open_gui",
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				"category." + MOD_ID
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openGuiKeyBinding.consumeClick()) {
				if (Minecraft.getInstance().screen == null) {
					Minecraft.getInstance().setScreen(
							new com.client.impl.gui.screen.ClickGuiScreen()
					);
				}
			}
			// Раздаём тик всем модулям через event bus
			eventBus.postTick();
		});

		System.out.println("[" + CLIENT_NAME + "] initialized, v" + CLIENT_VERSION);
	}

	public static MyClient getInstance() {
		return instance;
	}

	public EventBus getEventBus() {
		return eventBus;
	}

	public ModuleManager getModuleManager() {
		return moduleManager;
	}
}

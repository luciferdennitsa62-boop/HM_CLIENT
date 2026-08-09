package com.client;

import com.client.event.EventBus;
import com.client.impl.gui.screen.ClickGuiScreen;
import com.client.impl.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class MyClient implements ClientModInitializer {
    public static final String MOD_ID = "myclient";
    public static final String CLIENT_NAME = "HM Client";
    public static final String CLIENT_VERSION = "0.1.0";

    private static MyClient instance;
    private EventBus eventBus;
    private ModuleManager moduleManager;
    private KeyMapping openGuiKeyBinding;

    @Override
    public void onInitializeClient() {
        instance = this;
        eventBus = new EventBus();
        moduleManager = new ModuleManager();
        moduleManager.registerModules();

        openGuiKeyBinding = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key." + MOD_ID + ".open_gui",
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGuiKeyBinding.consumeClick()) {
                if (client.screen == null) {
                    client.setScreen(new ClickGuiScreen());
                }
            }
            eventBus.postTick();
        });

        System.out.println("[" + CLIENT_NAME + "] initialized, Minecraft 1.21.11, Fabric");
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

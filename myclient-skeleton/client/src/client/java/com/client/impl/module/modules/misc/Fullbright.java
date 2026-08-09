package com.client.impl.module.modules.misc;

import com.client.impl.module.Category;
import com.client.impl.module.Module;
import net.minecraft.client.Minecraft;

public final class Fullbright extends Module {
    private double previousGamma = 1.0;

    public Fullbright() {
        super("Fullbright", "Максимальная яркость мира", Category.RENDER);
    }

    @Override
    protected void onEnable() {
        Minecraft client = Minecraft.getInstance();
        previousGamma = client.options.gamma().get();
        client.options.gamma().set(16.0);
    }

    @Override
    protected void onDisable() {
        Minecraft client = Minecraft.getInstance();
        client.options.gamma().set(previousGamma);
    }
}

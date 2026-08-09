package com.client.impl.module.modules.movement;

import com.client.impl.module.Category;
import com.client.impl.module.Module;
import net.minecraft.client.Minecraft;

public final class AutoSprint extends Module {
    public AutoSprint() {
        super("AutoSprint", "Автоматический бег вперёд", Category.MOVEMENT);
    }

    @Override
    protected void onTick() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        boolean movingForward = client.player.input.getMoveVector().y > 0.0f;
        boolean enoughFood = client.player.getFoodData().getFoodLevel() > 6;
        boolean notSneaking = !client.player.isShiftKeyDown();

        if (movingForward && enoughFood && notSneaking) {
            client.player.setSprinting(true);
        }
    }

    @Override
    protected void onDisable() {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.setSprinting(false);
        }
    }
}

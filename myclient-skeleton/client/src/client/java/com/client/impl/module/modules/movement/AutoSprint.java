package com.client.impl.module.modules.movement;

import com.client.impl.module.Category;
import com.client.impl.module.Module;
import net.minecraft.client.Minecraft;

/**
 * Автоматически включает спринт, когда игрок движется вперёд.
 */
public class AutoSprint extends Module {

	public AutoSprint() {
		super("AutoSprint", "Автоматический бег вперёд", Category.MOVEMENT);
	}

	@Override
	protected void onTick() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;

		boolean movingForward = mc.player.input.getMoveVector().y > 0.0F;
		boolean hungry = mc.player.getFoodData().getFoodLevel() <= 6;

		if (movingForward && !hungry && !mc.player.isShiftKeyDown()) {
			mc.player.setSprinting(true);
		}
	}

	@Override
	protected void onDisable() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null) {
			mc.player.setSprinting(false);
		}
	}
}

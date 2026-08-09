package com.client.impl.module.modules.movement;

import com.client.impl.module.Category;
import com.client.impl.module.Module;
import net.minecraft.client.MinecraftClient;

/**
 * Честный модуль: включает setSprinting(true) на клиенте, когда игрок
 * двигается вперёд. Никакой подмены пакетов — сервер получает обычный
 * sprint-статус через штатный игровой механизм, просто автоматически,
 * а не вручную по Ctrl.
 */
public class AutoSprint extends Module {

	public AutoSprint() {
		super("AutoSprint", "Автоматический бег вперёд", Category.MOVEMENT);
	}

	@Override
	protected void onTick() {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null) return;

		boolean movingForward = mc.player.input.movementForward > 0;
		boolean hungry = mc.player.getHungerManager().getFoodLevel() <= 6;

		if (movingForward && !hungry && !mc.player.isSneaking()) {
			mc.player.setSprinting(true);
		}
	}

	@Override
	protected void onDisable() {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player != null) {
			mc.player.setSprinting(false);
		}
	}
}

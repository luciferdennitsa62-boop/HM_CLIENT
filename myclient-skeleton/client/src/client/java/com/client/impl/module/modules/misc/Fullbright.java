package com.client.impl.module.modules.misc;

import com.client.impl.module.Category;
import com.client.impl.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

/**
 * Полностью клиентская фича — просто выставляет игровую опцию яркости
 * в максимум, пока модуль включён, и возвращает как было при выключении.
 * Никакой сети тут нет вообще.
 */
public class Fullbright extends Module {

	private double previousGamma;

	public Fullbright() {
		super("Fullbright", "Максимальная яркость мира", Category.RENDER);
	}

	@Override
	protected void onEnable() {
		SimpleOption<Double> gammaOption = MinecraftClient.getInstance().options.getGamma();
		previousGamma = gammaOption.getValue();
		gammaOption.setValue(16.0); // за пределами обычного слайдера (0-1), но клиент это принимает
	}

	@Override
	protected void onDisable() {
		MinecraftClient.getInstance().options.getGamma().setValue(previousGamma);
	}
}

package com.client.impl.module.modules.misc;

import com.client.impl.module.Category;
import com.client.impl.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

/**
 * Полностью клиентская фича — просто выставляет игровую опцию яркости
 * в максимум, пока модуль включён, и возвращает как было при выключении.
 */
public class Fullbright extends Module {

	private double previousGamma;

	public Fullbright() {
		super("Fullbright", "Максимальная яркость мира", Category.RENDER);
	}

	@Override
	protected void onEnable() {
		OptionInstance<Double> gammaOption = Minecraft.getInstance().options.gamma();
		previousGamma = gammaOption.get();
		gammaOption.set(16.0);
	}

	@Override
	protected void onDisable() {
		Minecraft.getInstance().options.gamma().set(previousGamma);
	}
}

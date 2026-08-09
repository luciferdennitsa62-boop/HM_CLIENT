package com.client.impl.module;

import com.client.MyClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Базовый класс модуля. Каждая фича клиента (ESP, Fullbright, Sprint и т.д.)
 * наследуется от этого класса и переопределяет onEnable/onDisable/onTick/onRender
 * по необходимости.
 *
 * Пример модуля лежит в impl/module/modules/misc/Fullbright.java —
 * смотри его как референс структуры.
 */
public abstract class Module {

	private final String name;
	private final String description;
	private final Category category;
	private final List<Setting<?>> settings = new ArrayList<>();

	private boolean enabled = false;
	private int keyBind = InputUtil.UNKNOWN_KEY.getCode();

	// Анимация переключения для ClickGUI (0.0 = выкл, 1.0 = вкл, плавный переход)
	private float toggleAnimationProgress = 0f;

	public Module(String name, String description, Category category) {
		this.name = name;
		this.description = description;
		this.category = category;
	}

	protected <T extends Setting<?>> T addSetting(T setting) {
		settings.add(setting);
		return setting;
	}

	public void toggle() {
		setEnabled(!enabled);
	}

	public void setEnabled(boolean enabled) {
		if (this.enabled == enabled) return;
		this.enabled = enabled;

		if (enabled) {
			MyClient.getInstance().getEventBus().subscribeTick(v -> onTick());
			MyClient.getInstance().getEventBus().subscribeRender(this::onRender);
			onEnable();
		} else {
			onDisable();
			// Полная отписка по ссылке на конкретный метод через lambda невозможна напрямую —
			// в реальном проекте лучше хранить ссылки на конкретные Consumer'ы как поля,
			// см. TODO в ModuleManager.
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public float getToggleAnimationProgress() {
		return toggleAnimationProgress;
	}

	/** Вызывается каждый кадр ClickGUI для плавной анимации переключателей. */
	public void updateAnimation(float delta) {
		float target = enabled ? 1f : 0f;
		float speed = 12f; // скорость анимации, подбирается на глаз
		if (toggleAnimationProgress < target) {
			toggleAnimationProgress = Math.min(target, toggleAnimationProgress + speed * delta);
		} else if (toggleAnimationProgress > target) {
			toggleAnimationProgress = Math.max(target, toggleAnimationProgress - speed * delta);
		}
	}

	/** Вызывается один раз при включении модуля. */
	protected void onEnable() {}

	/** Вызывается один раз при выключении модуля. */
	protected void onDisable() {}

	/** Вызывается каждый игровой тик, пока модуль включён. */
	protected void onTick() {}

	/** Вызывается каждый кадр рендера, пока модуль включён. tickDelta — интерполяция кадра. */
	protected void onRender(float tickDelta) {}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Category getCategory() {
		return category;
	}

	public List<Setting<?>> getSettings() {
		return settings;
	}

	public int getKeyBind() {
		return keyBind;
	}

	public void setKeyBind(int keyBind) {
		this.keyBind = keyBind;
	}
}

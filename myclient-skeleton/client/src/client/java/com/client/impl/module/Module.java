package com.client.impl.module;

import com.client.MyClient;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class Module {

	private final String name;
	private final String description;
	private final Category category;
	private final List<Setting<?>> settings = new ArrayList<>();

	private boolean enabled = false;
	private int keyBind = InputConstants.UNKNOWN.getValue();

	private float toggleAnimationProgress = 0f;

	private final Consumer<Void> tickListener = v -> onTick();
	private final Consumer<Float> renderListener = this::onRender;

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
			MyClient.getInstance().getEventBus().subscribeTick(tickListener);
			MyClient.getInstance().getEventBus().subscribeRender(renderListener);
			onEnable();
		} else {
			MyClient.getInstance().getEventBus().unsubscribeTick(tickListener);
			MyClient.getInstance().getEventBus().unsubscribeRender(renderListener);
			onDisable();
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public float getToggleAnimationProgress() {
		return toggleAnimationProgress;
	}

	public void updateAnimation(float delta) {
		float target = enabled ? 1f : 0f;
		float speed = 12f;
		if (toggleAnimationProgress < target) {
			toggleAnimationProgress = Math.min(target, toggleAnimationProgress + speed * delta);
		} else if (toggleAnimationProgress > target) {
			toggleAnimationProgress = Math.max(target, toggleAnimationProgress - speed * delta);
		}
	}

	protected void onEnable() {}
	protected void onDisable() {}
	protected void onTick() {}
	protected void onRender(float tickDelta) {}

	public String getName() { return name; }
	public String getDescription() { return description; }
	public Category getCategory() { return category; }
	public List<Setting<?>> getSettings() { return settings; }
	public int getKeyBind() { return keyBind; }
	public void setKeyBind(int keyBind) { this.keyBind = keyBind; }
}

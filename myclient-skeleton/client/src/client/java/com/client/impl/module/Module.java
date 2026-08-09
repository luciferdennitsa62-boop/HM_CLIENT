package com.client.impl.module;

import com.client.MyClient;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean enabled;
    private int keyBind = InputConstants.UNKNOWN.getValue();
    private float toggleAnimationProgress;
    private final Runnable tickListener = this::onTick;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    protected <T extends Setting<?>> T addSetting(T setting) {
        settings.add(setting);
        return setting;
    }

    public final void toggle() {
        setEnabled(!enabled);
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) {
            MyClient.getInstance().getEventBus().subscribeTick(tickListener);
            onEnable();
        } else {
            MyClient.getInstance().getEventBus().unsubscribeTick(tickListener);
            onDisable();
        }
    }

    public final boolean isEnabled() {
        return enabled;
    }

    public final void updateAnimation(float deltaSeconds) {
        float target = enabled ? 1.0f : 0.0f;
        float step = Math.min(1.0f, deltaSeconds * 12.0f);
        toggleAnimationProgress += (target - toggleAnimationProgress) * step;
    }

    public final float getToggleAnimationProgress() {
        return toggleAnimationProgress;
    }

    protected void onEnable() {}
    protected void onDisable() {}
    protected void onTick() {}

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public List<Setting<?>> getSettings() { return settings; }
    public int getKeyBind() { return keyBind; }
    public void setKeyBind(int keyBind) { this.keyBind = keyBind; }
}

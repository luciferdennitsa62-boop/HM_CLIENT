package com.client.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Минималистичный event bus. Модули подписываются на тик/рендер через
 * onEnable(), отписываются через onDisable() — это делает Module сам,
 * через обёртки ниже.
 */
public class EventBus {

	private final List<Consumer<Void>> tickListeners = new ArrayList<>();
	private final List<Consumer<Float>> renderListeners = new ArrayList<>();

	public void subscribeTick(Consumer<Void> listener) {
		if (!tickListeners.contains(listener)) tickListeners.add(listener);
	}

	public void unsubscribeTick(Consumer<Void> listener) {
		tickListeners.remove(listener);
	}

	public void subscribeRender(Consumer<Float> listener) {
		if (!renderListeners.contains(listener)) renderListeners.add(listener);
	}

	public void unsubscribeRender(Consumer<Float> listener) {
		renderListeners.remove(listener);
	}

	public void postTick() {
		for (Consumer<Void> listener : new ArrayList<>(tickListeners)) {
			listener.accept(null);
		}
	}

	public void postRender(float tickDelta) {
		for (Consumer<Float> listener : new ArrayList<>(renderListeners)) {
			listener.accept(tickDelta);
		}
	}
}

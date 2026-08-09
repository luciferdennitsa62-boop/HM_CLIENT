package com.client.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Small client-thread event bus used by the module system. */
public final class EventBus {
    private final List<Runnable> tickListeners = new ArrayList<>();

    public void subscribeTick(Runnable listener) {
        if (!tickListeners.contains(listener)) {
            tickListeners.add(listener);
        }
    }

    public void unsubscribeTick(Runnable listener) {
        tickListeners.remove(listener);
    }

    public void postTick() {
        for (Runnable listener : List.copyOf(tickListeners)) {
            listener.run();
        }
    }
}

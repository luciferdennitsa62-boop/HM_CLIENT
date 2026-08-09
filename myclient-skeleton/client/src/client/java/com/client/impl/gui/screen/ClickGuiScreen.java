package com.client.impl.gui.screen;

import com.client.MyClient;
import com.client.impl.module.Category;
import com.client.impl.module.Module;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ClickGuiScreen extends Screen {
    private static final int BACKGROUND = 0xE8101014;
    private static final int HEADER = 0xF01C1C24;
    private static final int ROW = 0xC0181820;
    private static final int ACCENT = 0xFF9D4EFF;
    private static final int TEXT = 0xFFE8E8EA;
    private static final int MUTED = 0xFF96969E;
    private static final int WIDTH = 150;
    private static final int HEADER_HEIGHT = 22;
    private static final int ROW_HEIGHT = 20;
    private static final int GAP = 12;

    private final Map<Category, Boolean> expanded = new EnumMap<>(Category.class);
    private final Map<Category, Float> progress = new EnumMap<>(Category.class);
    private final List<HitBox> hitBoxes = new ArrayList<>();
    private long lastTime = System.nanoTime();

    public ClickGuiScreen() {
        super(Component.literal(MyClient.CLIENT_NAME));
        for (Category category : Category.values()) {
            expanded.put(category, true);
            progress.put(category, 1.0f);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - lastTime) / 1_000_000_000.0f);
        lastTime = now;

        graphics.fill(0, 0, width, height, BACKGROUND);
        hitBoxes.clear();

        int x = 20;
        int y = 20;
        for (Category category : Category.values()) {
            int panelHeight = renderCategory(graphics, category, x, y, dt);
            y += panelHeight + GAP;
            if (y + HEADER_HEIGHT + GAP > height) {
                y = 20;
                x += WIDTH + GAP;
            }
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    private int renderCategory(GuiGraphics graphics, Category category, int x, int y, float dt) {
        boolean targetOpen = expanded.get(category);
        float current = progress.get(category);
        float target = targetOpen ? 1.0f : 0.0f;
        float step = Math.min(1.0f, dt * 12.0f);
        current += (target - current) * step;
        progress.put(category, current);

        List<Module> modules = MyClient.getInstance().getModuleManager().getModulesByCategory(category);
        int visible = Math.round(modules.size() * current);
        int panelHeight = HEADER_HEIGHT + visible * ROW_HEIGHT;

        graphics.fill(x, y, x + WIDTH, y + HEADER_HEIGHT, HEADER);
        graphics.fill(x, y + HEADER_HEIGHT - 2, x + 3, y + HEADER_HEIGHT, ACCENT);
        graphics.drawString(font, Component.literal(category.name()), x + 9, y + 7, TEXT, false);
        hitBoxes.add(new HitBox(x, y, WIDTH, HEADER_HEIGHT, category, null));

        int rowY = y + HEADER_HEIGHT;
        for (int i = 0; i < visible; i++) {
            Module module = modules.get(i);
            module.updateAnimation(dt);
            int bg = module.isEnabled() ? ACCENT : ROW;
            graphics.fill(x, rowY, x + WIDTH, rowY + ROW_HEIGHT, bg);
            int textColor = module.isEnabled() ? 0xFFFFFFFF : TEXT;
            graphics.drawString(font, Component.literal(module.getName()), x + 8, rowY + 6, textColor, false);
            if (!module.getDescription().isEmpty()) {
                graphics.drawString(font, Component.literal(module.getDescription()), x + WIDTH - 4, rowY + 6,
                        MUTED, false);
            }
            hitBoxes.add(new HitBox(x, rowY, WIDTH, ROW_HEIGHT, category, module));
            rowY += ROW_HEIGHT;
        }
        return panelHeight;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (event.button() != 0) return super.mouseClicked(event, doubled);
        for (HitBox hitBox : hitBoxes) {
            if (!hitBox.contains(event.x(), event.y())) continue;
            if (hitBox.module == null) {
                expanded.put(hitBox.category, !expanded.get(hitBox.category));
            } else {
                hitBox.module.toggle();
            }
            return true;
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private record HitBox(int x, int y, int width, int height, Category category, Module module) {
        boolean contains(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        }
    }
}

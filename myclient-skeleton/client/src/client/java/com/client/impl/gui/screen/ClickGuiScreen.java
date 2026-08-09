package com.client.impl.gui.screen;

import com.client.MyClient;
import com.client.impl.module.Category;
import com.client.impl.module.Module;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClickGuiScreen extends Screen {

	private static final int BACKGROUND_COLOR = 0xE0141414;
	private static final int PANEL_HEADER_COLOR = 0xF01C1C22;
	private static final int ACCENT_COLOR = 0xFF9D4EFF;
	private static final int TEXT_COLOR = 0xFFDDDDDD;
	private static final int PANEL_WIDTH = 120;
	private static final int HEADER_HEIGHT = 18;
	private static final int ROW_HEIGHT = 16;

	private final Map<Category, Float> openProgress = new HashMap<>();
	private final Map<Category, Boolean> openTarget = new HashMap<>();
	private final List<ClickableRow> headerRows = new ArrayList<>();
	private final List<ClickableRow> moduleRows = new ArrayList<>();
	private long lastFrameTime = System.currentTimeMillis();

	public ClickGuiScreen() {
		super(Component.literal(MyClient.CLIENT_NAME));
		for (Category category : Category.values()) {
			openProgress.put(category, 0f);
			openTarget.put(category, false);
		}
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		long now = System.currentTimeMillis();
		float dt = Math.min((now - lastFrameTime) / 1000f, 0.1f);
		lastFrameTime = now;

		headerRows.clear();
		moduleRows.clear();
		context.fill(0, 0, this.width, this.height, BACKGROUND_COLOR);

		int panelX = 20;
		int panelY = 20;
		int spacing = 10;

		for (Category category : Category.values()) {
			panelY = renderCategoryPanel(context, category, panelX, panelY, dt) + spacing;
			if (panelY > this.height - 40) {
				panelY = 20;
				panelX += PANEL_WIDTH + spacing;
			}
		}

		super.render(context, mouseX, mouseY, delta);
	}

	private int renderCategoryPanel(GuiGraphics context, Category category, int x, int y, float dt) {
		List<Module> modules = MyClient.getInstance().getModuleManager().getModulesByCategory(category);
		float target = openTarget.get(category) ? 1f : 0f;
		float progress = openProgress.get(category);
		float speed = 10f;
		if (progress < target) progress = Math.min(target, progress + speed * dt);
		else if (progress > target) progress = Math.max(target, progress - speed * dt);
		openProgress.put(category, progress);

		context.fill(x, y, x + PANEL_WIDTH, y + HEADER_HEIGHT, PANEL_HEADER_COLOR);
		context.drawString(this.font, Component.literal(category.name()), x + 4, y + 5, TEXT_COLOR, false);
		headerRows.add(new ClickableRow(x, y, PANEL_WIDTH, HEADER_HEIGHT, category, null));

		int currentY = y + HEADER_HEIGHT;
		for (int i = 0; i < modules.size(); i++) {
			Module module = modules.get(i);
			module.updateAnimation(dt);
			float rowAlphaProgress = Math.max(0f, Math.min(1f, progress * modules.size() - i));
			if (rowAlphaProgress <= 0f) continue;

			int rowY = currentY;
			int rowAlpha = (int) (rowAlphaProgress * 255);
			float enabledProgress = module.getToggleAnimationProgress();
			int rowBg = blendColor(0x00000000, ACCENT_COLOR, enabledProgress * 0.5f);
			context.fill(x, rowY, x + PANEL_WIDTH, rowY + ROW_HEIGHT, withAlpha(rowBg, rowAlpha));
			context.drawString(this.font, Component.literal(module.getName()), x + 4, rowY + 4,
					withAlpha(TEXT_COLOR, rowAlpha), false);
			moduleRows.add(new ClickableRow(x, rowY, PANEL_WIDTH, ROW_HEIGHT, category, module));
			currentY += ROW_HEIGHT;
		}
		return currentY;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (event.button() != 0) return super.mouseClicked(event, doubled);

		double mouseX = event.x();
		double mouseY = event.y();
		for (ClickableRow row : headerRows) {
			if (row.contains(mouseX, mouseY)) {
				openTarget.put(row.category, !openTarget.get(row.category));
				return true;
			}
		}
		for (ClickableRow row : moduleRows) {
			if (row.contains(mouseX, mouseY)) {
				row.module.toggle();
				return true;
			}
		}
		return super.mouseClicked(event, doubled);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private static int blendColor(int from, int to, float progress) {
		int a1 = (from >> 24) & 0xFF, r1 = (from >> 16) & 0xFF, g1 = (from >> 8) & 0xFF, b1 = from & 0xFF;
		int a2 = (to >> 24) & 0xFF, r2 = (to >> 16) & 0xFF, g2 = (to >> 8) & 0xFF, b2 = to & 0xFF;
		int a = (int) (a1 + (a2 - a1) * progress);
		int r = (int) (r1 + (r2 - r1) * progress);
		int g = (int) (g1 + (g2 - g1) * progress);
		int b = (int) (b1 + (b2 - b1) * progress);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private static int withAlpha(int color, int alpha) {
		return (alpha << 24) | (color & 0x00FFFFFF);
	}

	private static class ClickableRow {
		final int x, y, width, height;
		final Category category;
		final Module module;

		ClickableRow(int x, int y, int width, int height, Category category, Module module) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.category = category;
			this.module = module;
		}

		boolean contains(double mouseX, double mouseY) {
			return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
		}
	}
}

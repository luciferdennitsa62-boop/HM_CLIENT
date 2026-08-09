package com.client.impl.gui.screen;

import com.client.MyClient;
import com.client.impl.module.Category;
import com.client.impl.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClickGUI. Стиль минималистичный тёмный с акцентным цветом — меняется
 * через константы ACCENT_COLOR / BACKGROUND_COLOR ниже.
 *
 * Анимации: у каждой панели категории — плавное раскрытие списка модулей
 * (openProgress 0..1), у каждого модуля — плавная заливка при включении
 * (см. Module#updateAnimation).
 *
 * Это упрощённая, но полностью рабочая база — расположение панелей
 * жёстко захардкожено по сетке, для реального продукта стоит добавить
 * drag&drop и сохранение позиций в конфиг.
 */
public class ClickGuiScreen extends Screen {

	private static final int BACKGROUND_COLOR = 0xE0141414;
	private static final int PANEL_HEADER_COLOR = 0xF01C1C22;
	private static final int ACCENT_COLOR = 0xFF9D4EFF; // неоново-фиолетовый, меняй тут
	private static final int TEXT_COLOR = 0xFFDDDDDD;

	private static final int PANEL_WIDTH = 120;
	private static final int HEADER_HEIGHT = 18;
	private static final int ROW_HEIGHT = 16;

	private final Map<Category, Float> openProgress = new HashMap<>();
	private final Map<Category, Boolean> openTarget = new HashMap<>();

	private long lastFrameTime = System.currentTimeMillis();

	public ClickGuiScreen() {
		super(Text.literal(MyClient.CLIENT_NAME));
		for (Category category : Category.values()) {
			openProgress.put(category, 0f);
			openTarget.put(category, false);
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		long now = System.currentTimeMillis();
		float dt = Math.min((now - lastFrameTime) / 1000f, 0.1f);
		lastFrameTime = now;

		context.fill(0, 0, this.width, this.height, BACKGROUND_COLOR);

		int panelX = 20;
		int panelY = 20;
		int spacing = 10;

		for (Category category : Category.values()) {
			panelY = renderCategoryPanel(context, category, panelX, panelY, dt, mouseX, mouseY);
			panelY += spacing;

			// перенос колонки, если панели не влезают по высоте экрана
			if (panelY > this.height - 40) {
				panelY = 20;
				panelX += PANEL_WIDTH + spacing;
			}
		}

		super.render(context, mouseX, mouseY, delta);
	}

	private int renderCategoryPanel(DrawContext context, Category category, int x, int y, float dt, int mouseX, int mouseY) {
		List<Module> modules = MyClient.getInstance().getModuleManager().getModulesByCategory(category);

		// анимация раскрытия списка модулей в категории
		float target = openTarget.get(category) ? 1f : 0f;
		float progress = openProgress.get(category);
		float speed = 10f;
		if (progress < target) progress = Math.min(target, progress + speed * dt);
		else if (progress > target) progress = Math.max(target, progress - speed * dt);
		openProgress.put(category, progress);

		// заголовок панели
		context.fill(x, y, x + PANEL_WIDTH, y + HEADER_HEIGHT, PANEL_HEADER_COLOR);
		context.drawText(this.textRenderer, Text.literal(category.name()), x + 4, y + 5, TEXT_COLOR, false);

		int currentY = y + HEADER_HEIGHT;
		int visibleRows = Math.round(modules.size() * progress);

		for (int i = 0; i < modules.size(); i++) {
			Module module = modules.get(i);
			module.updateAnimation(dt);

			// плавно проявляем строки по мере раскрытия панели
			float rowAlphaProgress = Math.max(0f, Math.min(1f, progress * modules.size() - i));
			if (rowAlphaProgress <= 0f) continue;

			int rowY = currentY;
			int rowAlpha = (int) (rowAlphaProgress * 255);

			// подложка модуля — плавно заливается акцентным цветом при включении
			float enabledProgress = module.getToggleAnimationProgress();
			int rowBg = blendColor(0x00000000, ACCENT_COLOR, enabledProgress * 0.5f);
			context.fill(x, rowY, x + PANEL_WIDTH, rowY + ROW_HEIGHT, withAlpha(rowBg, rowAlpha));

			context.drawText(this.textRenderer, Text.literal(module.getName()), x + 4, rowY + 4,
					withAlpha(TEXT_COLOR, rowAlpha), false);

			currentY += ROW_HEIGHT;
		}

		return currentY;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		// Упрощённый обработчик: для реального проекта лучше хранить хитбоксы
		// панелей/строк, вычисленные в render(), и проверять клики по ним тут.
		// Оставлено как TODO — структура готова, начинка кликов дописывается
		// вместе с системой хранения layout'а панелей (см. комментарий в классе).
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean shouldPause() {
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
}

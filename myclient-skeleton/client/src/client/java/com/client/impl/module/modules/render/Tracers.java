package com.client.impl.module.modules.render;

import com.client.impl.module.Category;
import com.client.impl.module.Module;
import com.client.impl.module.Setting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/**
 * Tracers — рисует линии от игрока до других сущностей в радиусе видимости.
 * Чисто визуальная фича, без сети — клиент рендерит данные о сущностях,
 * которые сервер и так прислал в пределах трекинг-радиуса.
 *
 * ПРИМЕЧАНИЕ: buffer-билдер API (addVertex/setColor) в 1.21.x периодически
 * менялся между минорными версиями. Если при сборке под 1.21.11 вылезет
 * ошибка именно на вызовах buffer.addVertex(...).setColor(...) — проверь
 * актуальную сигнатуру VertexConsumer в декомпилированных сорцах
 * (Minecraft > Tasks > genSources в IDE) и поправь по месту, логика вокруг
 * этого (сбор сущностей, дистанция, настройки) менять не придётся.
 */
public class Tracers extends Module {

	private final Setting.DoubleSetting range;
	private final Setting.BoolSetting playersOnly;

	public Tracers() {
		super("Tracers", "Линии до ближайших сущностей", Category.RENDER);
		this.range = addSetting(new Setting.DoubleSetting("Range", "Радиус отрисовки", 64.0, 8.0, 128.0, 1.0));
		this.playersOnly = addSetting(new Setting.BoolSetting("PlayersOnly", "Только игроки", true));
	}

	private final WorldRenderEvents.End renderCallback = this::onWorldRender;

	@Override
	protected void onEnable() {
		WorldRenderEvents.END.register(renderCallback);
	}

	@Override
	protected void onDisable() {
		WorldRenderEvents.END.unregister(renderCallback);
	}

	private void onWorldRender(WorldRenderContext context) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null) return;

		PoseStack matrices = context.matrixStack();
		if (matrices == null) return;

		Vec3 camera = context.camera().getPosition();
		MultiBufferSource.BufferSource consumers =
				(MultiBufferSource.BufferSource) context.consumers();
		if (consumers == null) return;

		var buffer = consumers.getBuffer(RenderType.lines());
		Matrix4f matrix = matrices.last().pose();

		for (Entity entity : mc.level.entitiesForRendering()) {
			if (entity == mc.player) continue;
			if (playersOnly.getValue() && !(entity instanceof Player)) continue;
			if (entity.distanceToSqr(mc.player) > range.getValue() * range.getValue()) continue;

			Vec3 entityPos = entity.getPosition(context.tickCounter().getGameTimeDeltaPartialTick(true))
					.add(0, entity.getBbHeight() / 2f, 0);

			float startY = (float) mc.player.getEyeHeight(mc.player.getPose());
			float endX = (float) (entityPos.x - camera.x);
			float endY = (float) (entityPos.y - camera.y);
			float endZ = (float) (entityPos.z - camera.z);

			buffer.addVertex(matrix, 0, startY, 0).setColor(0, 255, 200, 180);
			buffer.addVertex(matrix, endX, endY, endZ).setColor(0, 255, 200, 180);
		}
	}
}

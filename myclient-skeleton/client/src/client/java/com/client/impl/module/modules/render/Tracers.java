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
 * Tracers — клиентская визуализация линий до сущностей в заданном радиусе.
 */
public class Tracers extends Module {

	private final Setting.DoubleSetting range;
	private final Setting.BoolSetting playersOnly;
	private final WorldRenderEvents.End renderCallback = this::onWorldRender;

	public Tracers() {
		super("Tracers", "Линии до ближайших сущностей", Category.RENDER);
		this.range = addSetting(new Setting.DoubleSetting("Range", "Радиус отрисовки", 64.0, 8.0, 128.0, 1.0));
		this.playersOnly = addSetting(new Setting.BoolSetting("PlayersOnly", "Только игроки", true));
	}

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
		if (matrices == null || context.camera() == null) return;

		MultiBufferSource consumers = context.consumers();
		if (!(consumers instanceof MultiBufferSource.BufferSource bufferSource)) return;

		Vec3 camera = context.camera().getPosition();
		var buffer = bufferSource.getBuffer(RenderType.lines());
		Matrix4f matrix = matrices.last().pose();

		for (Entity entity : mc.level.entitiesForRendering()) {
			if (entity == mc.player) continue;
			if (playersOnly.getValue() && !(entity instanceof Player)) continue;
			if (entity.distanceToSqr(mc.player) > range.getValue() * range.getValue()) continue;

			Vec3 entityPos = entity.getPosition(context.tickCounter().getGameTimeDeltaPartialTick(true))
					.add(0.0, entity.getBbHeight() / 2.0, 0.0);

			float startY = mc.player.getEyeHeight();
			float endX = (float) (entityPos.x - camera.x);
			float endY = (float) (entityPos.y - camera.y);
			float endZ = (float) (entityPos.z - camera.z);

			buffer.addVertex(matrix, 0.0F, startY, 0.0F).setColor(0, 255, 200, 180);
			buffer.addVertex(matrix, endX, endY, endZ).setColor(0, 255, 200, 180);
		}

		bufferSource.endBatch(RenderType.lines());
	}
}

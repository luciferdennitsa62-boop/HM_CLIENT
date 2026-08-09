package com.client.impl.module.modules.render;

import com.client.impl.module.Category;
import com.client.impl.module.Module;
import com.client.impl.module.Setting;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

/**
 * Tracers — рисует линии от игрока до других сущностей в радиусе видимости.
 * Это чисто визуальная фича: клиент только рендерит данные о сущностях,
 * которые сервер и так прислал в пределах трекинг-радиуса (обычная игровая
 * механика отправки entity-данных клиенту). Никакой сети, никакого обмана —
 * просто дополнительная отрисовка поверх того, что уже есть в памяти клиента.
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
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.player == null || mc.world == null) return;

		MatrixStack matrices = context.matrixStack();
		if (matrices == null) return;

		Vec3d camera = context.camera().getPos();
		VertexConsumerProvider.Immediate consumers =
				(VertexConsumerProvider.Immediate) context.consumers();
		if (consumers == null) return;

		var buffer = consumers.getBuffer(RenderLayer.getLines());
		Matrix4f matrix = matrices.peek().getPositionMatrix();

		for (Entity entity : mc.world.getEntities()) {
			if (entity == mc.player) continue;
			if (playersOnly.getValue() && !(entity instanceof PlayerEntity)) continue;
			if (entity.squaredDistanceTo(mc.player) > range.getValue() * range.getValue()) continue;

			Vec3d entityPos = entity.getLerpedPos(context.tickCounter().getTickDelta(true))
					.add(0, entity.getHeight() / 2f, 0);

			float x1 = (float) (camera.x - camera.x); // рисуем от позиции камеры
			float startX = 0, startY = (float) (mc.player.getEyeHeight(mc.player.getPose())), startZ = 0;
			float endX = (float) (entityPos.x - camera.x);
			float endY = (float) (entityPos.y - camera.y);
			float endZ = (float) (entityPos.z - camera.z);

			buffer.vertex(matrix, startX, startY, startZ).color(0, 255, 200, 180).next();
			buffer.vertex(matrix, endX, endY, endZ).color(0, 255, 200, 180).next();
		}

		consumers.draw(RenderLayer.getLines());
	}
}

package com.client.impl.module.modules.render;

import com.client.impl.module.Category;
import com.client.impl.module.Module;
import com.client.impl.module.Setting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class Tracers extends Module {
    private final Setting.DoubleSetting range;
    private final Setting.BoolSetting playersOnly;

    public Tracers() {
        super("Tracers", "Линии до ближайших сущностей", Category.RENDER);
        range = addSetting(new Setting.DoubleSetting("Range", "Радиус отрисовки", 64.0, 8.0, 128.0, 1.0));
        playersOnly = addSetting(new Setting.BoolSetting("PlayersOnly", "Только игроки", true));
        WorldRenderEvents.AFTER_ENTITIES.register(this::render);
    }

    private void render(WorldRenderContext context) {
        if (!isEnabled()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) return;

        PoseStack matrices = context.matrices();
        MultiBufferSource consumers = context.consumers();
        if (matrices == null || consumers == null) return;

        // In 1.21.11 Camera exposes getCameraPos() in the official mappings.
        Vec3 camera = context.gameRenderer().getMainCamera().getCameraPos();
        float partialTick = client.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        double maxDistanceSq = range.getValue() * range.getValue();
        VertexConsumer buffer = consumers.getBuffer(RenderTypes.LINES);

        for (Entity entity : client.level.entitiesForRendering()) {
            if (entity == client.player) continue;
            if (playersOnly.getValue() && !(entity instanceof Player)) continue;
            if (entity.distanceToSqr(client.player) > maxDistanceSq) continue;

            Vec3 target = entity.getPosition(partialTick)
                    .add(0.0, entity.getBbHeight() * 0.5, 0.0)
                    .subtract(camera);

            float startY = client.player.getEyeHeight();
            buffer.addVertex(matrices.last(), 0.0f, startY, 0.0f)
                    .setColor(157, 78, 255, 220)
                    .setNormal(matrices.last(), 0.0f, 1.0f, 0.0f);
            buffer.addVertex(matrices.last(), (float) target.x, (float) target.y, (float) target.z)
                    .setColor(157, 78, 255, 220)
                    .setNormal(matrices.last(), 0.0f, 1.0f, 0.0f);
        }
    }
}

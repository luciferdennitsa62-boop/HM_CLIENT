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
import org.joml.Matrix4f;

/**
 * Draws client-side tracer lines to nearby entities.
 */
public class Tracers extends Module {

    private final Setting.DoubleSetting range;
    private final Setting.BoolSetting playersOnly;

    public Tracers() {
        super("Tracers", "Линии до ближайших сущностей", Category.RENDER);
        this.range = addSetting(new Setting.DoubleSetting("Range", "Радиус отрисовки", 64.0, 8.0, 128.0, 1.0));
        this.playersOnly = addSetting(new Setting.BoolSetting("PlayersOnly", "Только игроки", true));

        // Fabric events do not expose unregister(). Register once and use the
        // module's enabled state to decide whether anything should be drawn.
        WorldRenderEvents.AFTER_ENTITIES.register(this::onWorldRender);
    }

    @Override
    protected void onEnable() {
        // Callback is registered once in the constructor.
    }

    @Override
    protected void onDisable() {
        // No unregister API is available for Fabric's event instance.
    }

    private void onWorldRender(WorldRenderContext context) {
        if (!isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        PoseStack matrices = context.matrices();
        MultiBufferSource consumers = context.consumers();
        if (matrices == null || consumers == null) return;

        Vec3 camera = context.worldState().cameraRenderState().pos;
        Matrix4f pose = matrices.last().pose();
        VertexConsumer buffer = consumers.getBuffer(RenderTypes.LINES);
        double maxDistanceSq = range.getValue() * range.getValue();
        float partialTick = context.tickCounter().getGameTimeDeltaPartialTick(true);

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player) continue;
            if (playersOnly.getValue() && !(entity instanceof Player)) continue;
            if (entity.distanceToSqr(mc.player) > maxDistanceSq) continue;

            Vec3 entityPos = entity.getPosition(partialTick)
                    .add(0.0, entity.getBbHeight() * 0.5, 0.0);

            float endX = (float) (entityPos.x - camera.x);
            float endY = (float) (entityPos.y - camera.y);
            float endZ = (float) (entityPos.z - camera.z);
            float startY = mc.player.getEyeHeight();

            buffer.addVertex(pose, 0.0F, startY, 0.0F)
                    .setColor(157, 78, 255, 200)
                    .setNormal(0.0F, 1.0F, 0.0F);
            buffer.addVertex(pose, endX, endY, endZ)
                    .setColor(157, 78, 255, 200)
                    .setNormal(0.0F, 1.0F, 0.0F);
        }
    }
}

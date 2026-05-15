package mods.flammpfeil.slashblade.compat.jei.drawable;

import mezz.jei.api.gui.drawable.IDrawable;
import mods.flammpfeil.slashblade.compat.jei.data.ShowEntityListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yiran1457
 */
public class EntityDrawable implements IDrawable {
    public static final Map<EntityType<?>, LivingEntity> ENTITY_CACHE = new HashMap<>();
    public final EntityType<?> entityType;
    @Nullable
    public final LivingEntity renderEntity;
    public float renderTicks = 0;
    
    public EntityDrawable(EntityType<?> entityType) {
        this.entityType = entityType;
        if (!ENTITY_CACHE.containsKey(entityType)) {
            if (Minecraft.getInstance().level != null) {
                ENTITY_CACHE.put(entityType, (LivingEntity) entityType.create(Minecraft.getInstance().level));
            }
        }
        renderEntity = ENTITY_CACHE.get(entityType);
    }
    
    @Override
    public int getWidth() {
        return 0;
    }
    
    @Override
    public int getHeight() {
        return 0;
    }
    
    @Override
    public void draw(@NotNull GuiGraphics guiGraphics, int i, int i1) {
        if (renderEntity != null) {
            AABB box = renderEntity.getBoundingBox();
            
            guiGraphics.pose().translate(35, 0, 0);
            var data = ShowEntityListener.getShowData(entityType);
            int scale;
            if (data != null) {
                guiGraphics.pose().translate(0, data.yOffset(), 0);
                scale = data.scale();
            } else {
                scale = (int) Math.min(120 / box.getXsize(), 50 / box.getYsize());
            }
            
            renderTicks += Minecraft.getInstance().getTimer().getRealtimeDeltaTicks();
            guiGraphics.pose().translate(0, 0, 40);
            guiGraphics.pose().mulPose(new Quaternionf().rotateY(renderTicks * 0.0125f));
            guiGraphics.pose().translate(0, 0, -40);
            
            Quaternionf quaternionf = (new Quaternionf()).rotateZ((float) Math.PI);
            Quaternionf quaternionf1 = (new Quaternionf()).rotateX(((float) Math.PI / 180F));
            quaternionf.mul(quaternionf1);
            InventoryScreen.renderEntityInInventory(
                guiGraphics, 0, 80, scale,
                new Vector3f(),
                quaternionf,
                quaternionf1,
                renderEntity
            );
        }
    }
}

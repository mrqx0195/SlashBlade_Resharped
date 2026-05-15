package mods.flammpfeil.slashblade.compat.jei.drawable;

import com.mojang.blaze3d.platform.Window;
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
    public void draw(@NotNull GuiGraphics guiGraphics, int xOffset, int yOffset) {
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
            
            Minecraft minecraft = Minecraft.getInstance();
            Window window = minecraft.getWindow();
            float xMouse = (float) minecraft.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
            float yMouse = (float) minecraft.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();
            float f2 = (float) Math.atan((window.getGuiScaledWidth() / 2.0 - xMouse) / 40);
            float f3 = (float) Math.atan((window.getGuiScaledHeight() / 2.0 - yMouse) / 40);
            Quaternionf quaternionf = (new Quaternionf()).rotateZ((float) Math.PI);
            Quaternionf quaternionf1 = new Quaternionf().rotateX(f3 * 20 * (float) (Math.PI / 180));
            quaternionf.mul(quaternionf1);
            float f4 = renderEntity.yBodyRot;
            float f5 = renderEntity.getYRot();
            float f6 = renderEntity.getXRot();
            float f7 = renderEntity.yHeadRotO;
            float f8 = renderEntity.yHeadRot;
            renderEntity.yBodyRot = 180.0F + f2 * 20.0F;
            renderEntity.setYRot(180.0F + f2 * 40.0F);
            renderEntity.setXRot(-f3 * 20.0F);
            renderEntity.yHeadRot = renderEntity.getYRot();
            renderEntity.yHeadRotO = renderEntity.getYRot();
            float f9 = renderEntity.getScale();
            Vector3f vector3f = new Vector3f(0.0F, renderEntity.getBbHeight() / 2.0F, 0.0F);
            float f10 = (float) scale / f9;
            InventoryScreen.renderEntityInInventory(guiGraphics, 0, 60, f10, vector3f, quaternionf, quaternionf1, renderEntity);
            renderEntity.yBodyRot = f4;
            renderEntity.setYRot(f5);
            renderEntity.setXRot(f6);
            renderEntity.yHeadRotO = f7;
            renderEntity.yHeadRot = f8;
        }
    }
}

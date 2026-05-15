package mods.flammpfeil.slashblade.compat.jei.ingredient;

import mods.flammpfeil.slashblade.registry.specialeffects.SpecialEffect;
import mods.flammpfeil.slashblade.slasharts.SlashArts;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Style;

/**
 * @author yiran1457
 */
public class Decoration {
    public static void renderSADecorator(GuiGraphics guiGraphics, Font font, SlashArts slashArts) {
        renderString(guiGraphics, font, slashArts.getDescription().getString());
    }
    
    public static void renderSEDecorator(GuiGraphics guiGraphics, Font font, SpecialEffect specialEffect) {
        renderString(guiGraphics, font, specialEffect.getDescription().getString());
    }
    
    public static void renderString(GuiGraphics guiGraphics, Font font, String text) {
        var pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(0, 11, 256);
        pose.scale(0.45f, 0.45f, 1);
        guiGraphics.drawString(
            font,
            font.getSplitter().formattedHeadByWidth(text, 9 * 4, Style.EMPTY),
            0, 0,
            -1, false
        );
        pose.popPose();
    }
}

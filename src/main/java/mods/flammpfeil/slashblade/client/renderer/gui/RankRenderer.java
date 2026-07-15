package mods.flammpfeil.slashblade.client.renderer.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.capability.concentrationrank.IConcentrationRank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class RankRenderer {
    private static final class SingletonHolder {
        private static final RankRenderer instance = new RankRenderer();
    }
    
    public static RankRenderer getInstance() {
        return SingletonHolder.instance;
    }
    
    private RankRenderer() {
    }
    
    public void register() {
        NeoForge.EVENT_BUS.register(this);
    }
    
    static ResourceLocation RankImg = ResourceLocation.fromNamespaceAndPath(SlashBlade.MODID, "textures/gui/rank.png");
    
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void renderTick(RenderGuiEvent.Post event) {
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        // if(!mc.isGameFocused()) return;
        if (!Minecraft.renderNames()) {
            return;
        }
        if (mc.screen != null) {
            if (!(mc.screen instanceof ChatScreen)) {
                return;
            }
        }
        
        LocalPlayer player = mc.player;
        long time = System.currentTimeMillis();
        
        renderRankHud(event.getPartialTick().getGameTimeDeltaPartialTick(true), player, time);
    }
    
    private void renderRankHud(Float partialTicks, LocalPlayer player, long time) {
        Minecraft mc = Minecraft.getInstance();
        
        IConcentrationRank cr = player.getData(CapabilityConcentrationRank.RANK_POINT.get());
        {
            long now = player.level().getGameTime();
            
            IConcentrationRank.ConcentrationRanks rank = cr.getRank(now);
            
            /*
             * debug rank = IConcentrationRank.ConcentrationRanks.C; now =
             * cr.getLastUpdate();
             */
            
            if (rank == IConcentrationRank.ConcentrationRanks.NONE) {
                return;
            }
            
            // todo : korenani loadGUIRenderMatrix
            // mc.getMainWindow().loadGUIRenderMatrix(Minecraft.IS_RUNNING_ON_MAC);
            
            int k = mc.getWindow().getGuiScaledWidth();
            int l = mc.getWindow().getGuiScaledHeight();
            
            PoseStack poseStack = new PoseStack();
            // position
            poseStack.translate((float) (k * 2) / 3, (float) l / 5, 0);
            
            // RenderSystem.enableTexture();
            RenderSystem.disableDepthTest();
            TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
            texturemanager.getTexture(RankImg).setFilter(false, false);
            RenderSystem.setShaderTexture(0, RankImg);
            
            boolean showTextRank = false;
            
            long textTimeout = cr.getLastRankRise() + 20;
            long visibleTimeout = cr.getLastUpdate() + 120;
            
            if (now < textTimeout) {
                showTextRank = true;
            }
            
            if (now < visibleTimeout) {
                int rankOffset = 32 * (rank.level - 1);
                int textOffset = showTextRank ? 128 : 0;
                
                int progress = (int) (33 * cr.getRankProgress(now));
                
                int progressIcon = (int) (18 * cr.getRankProgress(now));
                int progressIconInv = 17 - progressIcon;
                
                // GL11.glScalef(3,3,3);
                // iconFrame
                drawTexturedQuad(poseStack, 0, 0, textOffset + 64, rankOffset, 64, 32, -95f);
                // icon
                drawTexturedQuad(poseStack, 0, progressIconInv + 7, textOffset, rankOffset + progressIconInv + 7,
                    64, progressIcon, -90f);
                
                // gauge frame
                drawTexturedQuad(poseStack, 0, 32, 0, 256 - 16, 64, 16, -90f);
                // gause fill
                drawTexturedQuad(poseStack, 16, 32, 16, 256 - 32, progress, 16, -95f);
            }
            
        }
        
    }
    
    public static void drawTexturedQuad(PoseStack poseStack, int x, int y, int u, int v, int width, int height,
                                        float zLevel) {
        float var7 = 0.00390625F; // 1/256 texturesize
        float var8 = 0.00390625F;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        
        Matrix4f m = poseStack.last().pose();
        
        BufferBuilder wr = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        wr.addVertex(m, x, y + height, zLevel).setUv((u + 0.0f) * var7, (v + height) * var8);
        wr.addVertex(m, x + width, y + height, zLevel).setUv((u + width) * var7, (v + height) * var8);
        wr.addVertex(m, x + width, y, zLevel).setUv((u + width) * var7, (v) * var8);
        wr.addVertex(m, x, y, zLevel).setUv((u) * var7, (v) * var8);
        
        MeshData meshData = wr.build();
        if (meshData != null) {
            BufferUploader.drawWithShader(meshData);
        }
    }
}

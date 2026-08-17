package mods.flammpfeil.slashblade.client.renderer.model.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import javax.annotation.Nullable;
import java.util.function.BiFunction;

public class Face {
    
    public static final BiFunction<Vector4f, Integer, Integer> alphaNoOverride = (v, a) -> a;
    public static final BiFunction<Vector4f, Integer, Integer> alphaOverrideYZZ = (v, a) -> v.y() == 0 ? 0 : a;
    public static BiFunction<Vector4f, Integer, Integer> alphaOverride = alphaNoOverride;
    
    public static void setAlphaOverride(BiFunction<Vector4f, Integer, Integer> alphaOverride) {
        Face.alphaOverride = alphaOverride;
    }
    
    public static void resetAlphaOverride() {
        Face.alphaOverride = alphaNoOverride;
    }
    
    public static final Vector4f uvDefaultOperator = new Vector4f(1, 1, 0, 0);
    public static Vector4f uvOperator = uvDefaultOperator;
    
    public static void setUvOperator(float uScale, float vScale, float uOffset, float vOffset) {
        Face.uvOperator = new Vector4f(uScale, vScale, uOffset, vOffset);
    }
    
    public static void resetUvOperator() {
        Face.uvOperator = uvDefaultOperator;
    }
    
    public Vertex[] vertices = new Vertex[]{};
    @Nullable
    public Vertex[] vertexNormals;
    @Nullable
    public Vertex faceNormal;
    @Nullable
    public TextureCoordinate[] textureCoordinates;
    @Nullable
    public boolean[] texUSign;
    @Nullable
    public boolean[] texVSign;
    
    private static final ThreadLocal<Vector3f> NORMAL_TMP = ThreadLocal.withInitial(Vector3f::new);
    
    public void initTexSigns() {
        if (textureCoordinates != null && textureCoordinates.length > 0) {
            float rawAvgU = 0F;
            float rawAvgV = 0F;
            for (TextureCoordinate tc : textureCoordinates) {
                rawAvgU += tc.u;
                rawAvgV += tc.v;
            }
            rawAvgU /= textureCoordinates.length;
            rawAvgV /= textureCoordinates.length;
            
            texUSign = new boolean[textureCoordinates.length];
            texVSign = new boolean[textureCoordinates.length];
            for (int i = 0; i < textureCoordinates.length; i++) {
                texUSign[i] = textureCoordinates[i].u > rawAvgU;
                texVSign[i] = textureCoordinates[i].v > rawAvgV;
            }
        }
    }
    
    @OnlyIn(Dist.CLIENT)
    public void addFaceForRender(VertexConsumer tessellator, PoseStack matrixStack, int light, int color) {
        PoseStack.Pose pose = matrixStack.last();
        addFaceForRender(tessellator, 0.0005F, pose.pose(), pose.normal(), light, color);
    }
    
    @OnlyIn(Dist.CLIENT)
    public void addFaceForRender(VertexConsumer tessellator, float textureOffset, Matrix4f transform, int light, int color) {
        addFaceForRender(tessellator, textureOffset, transform, new Matrix3f(transform), light, color);
    }
    
    @OnlyIn(Dist.CLIENT)
    public void addFaceForRender(VertexConsumer tessellator, float textureOffset, Matrix4f transform,
                                 Matrix3f normalTransform, int light, int color) {
        if (faceNormal == null) {
            faceNormal = this.calculateFaceNormal();
        }
        
        float averageU = 0F;
        float averageV = 0F;
        
        if ((textureCoordinates != null) && (textureCoordinates.length > 0)) {
            for (TextureCoordinate textureCoordinate : textureCoordinates) {
                averageU += textureCoordinate.u * uvOperator.x() + uvOperator.z();
                averageV += textureCoordinate.v * uvOperator.y() + uvOperator.w();
            }
            
            averageU = averageU / textureCoordinates.length;
            averageV = averageV / textureCoordinates.length;
        }
        
        for (int i = 0; i < vertices.length; ++i) {
            putVertex(tessellator, i, transform, normalTransform, textureOffset, averageU, averageV, light, color);
        }
    }
    
    void putVertex(VertexConsumer wr, int i, Matrix4f transform, Matrix3f normalTransform,
                   float textureOffset, float averageU, float averageV, int light, int color) {
        float offsetU, offsetV;
        wr.addVertex(transform, vertices[i].x, vertices[i].y, vertices[i].z);
        
        int alpha = FastColor.ARGB32.alpha(color);
        if (Face.alphaOverride.equals(Face.alphaOverrideYZZ) && vertices[i].y == 0) {
            alpha = 0;
        }
        wr.setColor(FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color), alpha);
        
        if ((textureCoordinates != null) && (textureCoordinates.length > 0)) {
            offsetU = texUSign != null && texUSign[i] ? -textureOffset : textureOffset;
            offsetV = texVSign != null && texVSign[i] ? -textureOffset : textureOffset;
            
            float textureU = textureCoordinates[i].u * uvOperator.x() + uvOperator.z();
            float textureV = textureCoordinates[i].v * uvOperator.y() + uvOperator.w();
            
            wr.setUv(textureU + offsetU, textureV + offsetV);
        } else {
            wr.setUv(0, 0);
        }
        
        wr.setOverlay(OverlayTexture.NO_OVERLAY);
        wr.setLight(light);
        
        Vector3f vecNormal;
        if (vertexNormals != null) {
            Vertex normal = vertexNormals[i];
            vecNormal = NORMAL_TMP.get().set(normal.x, normal.y, normal.z);
        } else if (faceNormal != null) {
            vecNormal = NORMAL_TMP.get().set(faceNormal.x, faceNormal.y, faceNormal.z);
        } else {
            vecNormal = null;
        }
        
        if (vecNormal != null) {
            vecNormal.mul(normalTransform);
            vecNormal.normalize();
            wr.setNormal(vecNormal.x(), vecNormal.y(), vecNormal.z());
        }
    }
    
    public Vertex calculateFaceNormal() {
        Vec3 v1 = new Vec3(vertices[1].x - vertices[0].x, vertices[1].y - vertices[0].y, vertices[1].z - vertices[0].z);
        Vec3 v2 = new Vec3(vertices[2].x - vertices[0].x, vertices[2].y - vertices[0].y, vertices[2].z - vertices[0].z);
        Vec3 normalVector = v1.cross(v2).normalize();
        
        return new Vertex((float) normalVector.x, (float) normalVector.y, (float) normalVector.z);
    }
}

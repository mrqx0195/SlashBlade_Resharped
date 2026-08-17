# 客户端渲染性能优化计划

> 基于对 `src/main/java` 全部客户端渲染代码的审计。仅生成计划文档，不做代码修改。
> `RenderOverrideEvent` 作为外部扩展点保留，不在优化范围内。

---

## P0-1: VmdAnimation.setupAnim() 帧内缓存

**问题**

`VmdAnimation.get3DTransform()` (line 150) 每次调用 `this.setupAnim(tickDelta)`。
player-anim 库对 6 个身体部位查询 4 种变换 (POSITION/ROTATION/SCALE/BEND) = 24 次，
加上 `LayerMainBlade.setUserPose()` 的 2 次 body 查询 = **每帧 ~26 次**。

每次 `setupAnim` 执行完整 MMD 管线：`setVmd` + `updateMotion`（骨骼重置 → 关键帧插值 →
矩阵更新 ×2 → IK 求解 → 蒙皮矩阵 → 顶点蒙皮）。同一帧内 `currentTick` 和 `tickDelta`
不变，26 次调用结果完全相同。

**方案**

在 `VmdAnimation` 类中添加帧内缓存字段，在 `setupAnim()` 头部做 early-return。

**文件**: `src/main/java/mods/flammpfeil/slashblade/compat/playerAnim/VmdAnimation.java`

```java
// 新增字段
private int lastCachedTick = -1;
private float lastCachedPartial = -1.0f;

// 修改 setupAnim()
@Override
public void setupAnim(float tickDelta) {
    if (this.currentTick == this.lastCachedTick
        && Float.floatToIntBits(tickDelta) == Float.floatToIntBits(this.lastCachedPartial)) {
        return;
    }
    this.lastCachedTick = this.currentTick;
    this.lastCachedPartial = tickDelta;

    // ... 原有逻辑不变
}
```

注意：使用 `Float.floatToIntBits` 比较避免 `NaN` 和 `-0.0` 的边界问题。

**验证**

- `./gradlew compileJava` 通过
- `./gradlew runClient` 进入第三人称，持刀攻击动画正常
- 可通过在 `setupAnim` 头部加 `SlashBlade.LOGGER.debug("setupAnim called")` 验证频率下降

---

## P0-2: 跳过 updateMotion() 中不需要的顶点蒙皮

**问题**

`MmdMotionPlayer.updateMotion()` (line 174-222) 最后执行：

```java
// line 218-221
for (int i = 0; i < bone_array.length; i++) {
    bone_array[i].updateSkinningMat(this._skinning_mat[i]);
}
this.onUpdateSkinningMatrix(this._skinning_mat);  // 全顶点蒙皮
```

`onUpdateSkinningMatrix()` (MmdMotionPlayerGL2.java:93-127) 对每个顶点做矩阵-向量
乘法（位置 + 法线），写入 `_fbuf[]`。但：

- `MmdMotionPlayerGL2.render()` 在整个代码库中 **从未被调用**（唯一出现在
  `SlashBladeTEISR.java:104` 的注释中）
- `_fbuf[]` 和 Material 数组是死代码
- `LayerMainBlade` 只读 `_skinning_mat[hardpointA]` 和 `_skinning_mat[hardpointB]`
- `VmdAnimation` 只读 `bone.m_vec3Position` 和 `bone.m_vec4Rotate`

两条路径都不需要顶点蒙皮。但 `updateMotion()` 每次调用都执行完整的
`onUpdateSkinningMatrix()`，这是 O(vertices) 的纯浪费。
在 `VmdAnimation` 路径中被重复 ~26 次/帧。

**方案 A（推荐）：在 MmdMotionPlayer 中添加轻量级更新方法**

**文件**: `src/main/java/jp/nyatla/nymmd/MmdMotionPlayer.java`

```java
// 新增方法：只更新骨骼，不计算蒙皮矩阵和顶点蒙皮
public void updateMotionBonesOnly(float i_position_in_msec) throws MmdException {
    final PmdIK[] ik_array = this._ref_pmd_model.getIKArray();
    final PmdBone[] bone_array = this._ref_pmd_model.getBoneArray();
    assert i_position_in_msec >= 0;
    float frame = (float) (i_position_in_msec / (100.0 / 3));
    if (frame > this._ref_vmd_motion.getMaxFrame()) {
        frame = this._ref_vmd_motion.getMaxFrame();
    }
    this.updateFace(frame);
    for (PmdBone bone : bone_array) {
        bone.reset();
    }
    this.updateBone(frame);
    for (int i = 0; i < bone_array.length; i++) {
        bone_array[i].updateMatrix();
    }
    for (int i = 0; i < ik_array.length; i++) {
        ik_array[i].update();
    }
    for (int i = 0; i < bone_array.length; i++) {
        bone_array[i].updateMatrix();
    }
    if (this._lookme_enabled) {
        this.updateNeckBone();
    }
    // 不调用 updateSkinningMat 和 onUpdateSkinningMatrix
}
```

然后修改 `VmdAnimation.setupAnim()` 调用 `updateMotionBonesOnly` 代替 `updateMotion`。

**文件**: `src/main/java/mods/flammpfeil/slashblade/compat/playerAnim/VmdAnimation.java`

```java
// line 310: 替换
mmp.updateMotion((float) time);
// 为
mmp.updateMotionBonesOnly((float) time);
```

**方案 B（更彻底）：拆分 MmdMotionPlayer 的更新逻辑**

将 `updateMotion()` 拆分为三个可独立调用的阶段：

1. `updateBones(float frame)` — 骨骼重置 + 关键帧插值 + 矩阵更新 + IK
2. `updateSkinningMatrices()` — 蒙皮矩阵计算
3. `updateSkinningVertices()` — 顶点蒙皮（原 `onUpdateSkinningMatrix`）

`updateMotion()` 保持不变（调用全部三个）。`VmdAnimation` 只调用 `updateBones()`。
`LayerMainBlade` 调用 `updateBones()` + `updateSkinningMatrices()`（因为它需要
`_skinning_mat` 但不需要 `_fbuf`）。

**验证**

- `./gradlew compileJava` 通过
- `./gradlew runClient` 第三人称动画和刀刃附着位置正常
- 验证 `LayerMainBlade` 的 hardpointA/B 矩阵读取正确

---

## P0-3: 移除 updateMotion 中无订阅者的 EventBus.post

**问题**

`MmdMotionPlayer.java:194` 和 `206`：

```java
eventBus.post(new UpdateBoneEvent.Pre(this._ref_pmd_model.getBoneArray(), this));
// ... IK 更新 ...
eventBus.post(new UpdateBoneEvent.Post(this._ref_pmd_model.getBoneArray(), this));
```

代码库中没有任何地方调用 `eventBus.register()`。每次 post 创建 2 个事件对象
+ 事件总线遍历，纯浪费。在 `VmdAnimation` 路径中每帧重复 ~26 次。

**方案**

注释掉或移除这两行。如果 `MmdMotionPlayer` 的 `eventBus` 字段不再被使用，
也可以一并移除 `eventBus` 字段和 `UpdateBoneEvent` 内部类。

**文件**: `src/main/java/jp/nyatla/nymmd/MmdMotionPlayer.java`

```java
// line 194: 移除
// eventBus.post(new UpdateBoneEvent.Pre(this._ref_pmd_model.getBoneArray(), this));

// line 206: 移除
// eventBus.post(new UpdateBoneEvent.Post(this._ref_pmd_model.getBoneArray(), this));
```

**验证**

- `./gradlew compileJava` 通过
- 搜索代码确认无 `eventBus.register` 调用

---

## P2-1: Face.addFaceForRender 的 Matrix3f 分配

**问题**

`Face.java:78-79`：

```java
public void addFaceForRender(VertexConsumer tessellator, float textureOffset,
    Matrix4f transform, int light, int color) {
    addFaceForRender(tessellator, textureOffset, transform,
        new Matrix3f(transform), light, color);  // ← 每面分配
}
```

**方案**

将 `Matrix3f` 提升为 ThreadLocal 或在 `GroupObject.render()` 中预计算并传入。

**方案 A：ThreadLocal**

```java
private static final ThreadLocal<Matrix3f> NORMAL_MATRIX_TMP =
    ThreadLocal.withInitial(Matrix3f::new);

public void addFaceForRender(VertexConsumer tessellator, float textureOffset,
    Matrix4f transform, int light, int color) {
    Matrix3f normalMatrix = NORMAL_MATRIX_TMP.get().set(transform);
    addFaceForRender(tessellator, textureOffset, transform, normalMatrix, light, color);
}
```

注意：需要验证 `Matrix3f.set(Matrix4f)` 是否存在。如果不存在，使用
`new Matrix3f(transform)` 但通过 ThreadLocal 避免分配。

**方案 B：在调用侧预计算**

修改 `GroupObject.render()` 传入预计算的 `Matrix3f`：

```java
public void render(VertexConsumer tessellator, PoseStack matrixStack, int light, int color) {
    if (!faces.isEmpty()) {
        PoseStack.Pose pose = matrixStack.last();
        for (Face face : faces) {
            face.addFaceForRender(tessellator, 0.0005F, pose.pose(), pose.normal(), light, color);
        }
    }
}
```

当前 `GroupObject.render()` 已经传入 `PoseStack`，`Face.addFaceForRender` 的
三参数重载 (line 72-75) 已经使用 `pose.pose()` 和 `pose.normal()`。所以这个路径
已经避免了 `new Matrix3f`。只有直接调用四参数重载的外部代码才会触发分配。

**验证**

- `./gradlew compileJava` 通过
- 渲染结果无视觉差异

---

## P2-2: LayerMainBlade 每帧对象分配

**问题**

`LayerMainBlade.java` 每帧每实体分配：

| 行号 | 分配 | 优化方向 |
|------|------|---------|
| 310, 342 | `new float[16]` | 提升为实例字段，复用 |
| 318, 350 | `new Matrix3f(mat).invert().transpose()` | 提升为实例字段，复用 |
| 148, 157, 162, 167, 176 | `new Quaternionf().rotateZYX(...)` | 提升为静态 final 常量（CarryType 旋转不变） |

**方案**

```java
// 新增实例字段（在类顶部）
private final float[] _boneMatrixBuf = new float[16];
private final Matrix3f _normalMatrixTmp = new Matrix3f();

// CarryType 旋转改为静态常量
private static final Quaternionf CARRY_ROTATION_PSO2 =
    new Quaternionf().rotateZYX(-0.122173F, 0, 0);
private static final Quaternionf CARRY_ROTATION_KATANA =
    new Quaternionf().rotateZYX(3.1415927F, 1.570796f, 0.261799F);
// ... 其他 CarryType
```

**验证**

- `./gradlew compileJava` 通过
- 各 CarryType 的刀刃位置/旋转正确

---

## P2-3: 字符串拼接 part + "_luminous" 缓存

**问题**

`LayerMainBlade.java:200,204,334,358` 和 `SlashBladeTEISR.java:164,365,394`：

```java
BladeRenderState.renderOverridedLuminous(stack, obj, part + "_luminous", ...);
```

每帧每实体拼接字符串。

**方案**

将常用的发光部件名预计算为常量：

```java
private static final String BLADE_LUMINOUS = "blade_luminous";
private static final String SHEATH_LUMINOUS = "sheath_luminous";
private static final String BLADE_DAMAGED_LUMINOUS = "blade_damaged_luminous";
```

**验证**

- `./gradlew compileJava` 通过

---

## P3-1: WavefrontObject.tessellateOnly 的 equalsIgnoreCase

**问题**

`WavefrontObject.java:138-146` 对每组每名做 `equalsIgnoreCase`。

**方案**

在 `WavefrontObject` 构造完成后构建 `Map<String, GroupObject>`：

```java
private Map<String, GroupObject> groupObjectMap;

// 在构造函数末尾或首次访问时构建
private Map<String, GroupObject> getGroupObjectMap() {
    if (groupObjectMap == null) {
        groupObjectMap = new HashMap<>();
        for (GroupObject go : groupObjects) {
            groupObjectMap.put(go.name.toLowerCase(), go);
        }
    }
    return groupObjectMap;
}

public void tessellateOnly(VertexConsumer tessellator, PoseStack matrixStack,
    int light, int color, String... groupNames) {
    Map<String, GroupObject> map = getGroupObjectMap();
    for (String groupName : groupNames) {
        GroupObject go = map.get(groupName.toLowerCase());
        if (go != null) {
            go.render(tessellator, matrixStack, light, color);
        }
    }
}
```

注意：需要确认 OBJ 文件中组名大小写是否一致。如果不一致，toLowerCase 可能
导致匹配失败。需要先检查实际的 .obj 文件。

**验证**

- `./gradlew compileJava` 通过
- 所有刀刃模型渲染正确（组名匹配无遗漏）

---

## P3-2: LockonCircleRender 的 capability 检查优化

**问题**

`LockonCircleRender.java:109` 订阅 `RenderLivingEvent.Post`，对所有渲染的
LivingEntity 执行 `BladeStateAccess.of(stack)` + `getTargetEntity()` 比较。

**方案**

在 `onRenderLiving` 头部先检查 `cachedLockOnTarget` 是否为 null，如果是则
直接返回（无锁定目标时跳过所有实体的 capability 查找）：

```java
@SubscribeEvent
public void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
    if (cachedLockOnTarget == null) return;  // 无锁定目标，跳过
    // ... 原有逻辑
}
```

当前代码在 line 66 已经有 `if (target == null) { cachedLockOnTarget = null; }`
的逻辑，但 `onRenderLiving` 仍然对每个实体执行 capability 查找。优化后，
无锁定目标时直接跳过。

**验证**

- `./gradlew compileJava` 通过
- 锁定功能正常

---

## P3-3: BladeModelManager / BladeMotionManager 缓存策略

**问题**

两个缓存都使用 `LoadingCache` + `asyncReloading(newCachedThreadPool())`，无
`maximumSize`。所有加载过的模型/动作永远留在内存中。

**方案**

添加 `maximumSize` 限制：

```java
// BladeModelManager.java
cache = CacheBuilder.newBuilder()
    .maximumSize(256)  // 限制缓存大小
    .build(...);

// BladeMotionManager.java
cache = CacheBuilder.newBuilder()
    .maximumSize(64)  // VMD 文件较少，可以更小
    .build(...);
```

将 `newCachedThreadPool()` 改为固定大小线程池：

```java
Executors.newFixedThreadPool(2)  // 限制并发加载线程数
```

**验证**

- `./gradlew compileJava` 通过
- 资源包加载正常，内存占用可控

---

## 实施顺序建议

1. **P0-1** (VmdAnimation 缓存) — 最小改动，最大收益
2. **P0-2** (跳过顶点蒙皮) — 需要修改 MmdMotionPlayer，收益最大
3. **P0-3** (移除无用 EventBus.post) — 一行改动
4. **P2-2** (LayerMainBlade 分配优化) — 直接减少 GC 压力
5. **P2-3** (字符串常量化) — 简单改动
6. **P3-1** (tessellateOnly Map 查找) — 小优化
7. **P3-2** (LockonCircleRender 提前返回) — 一行改动
8. **P3-3** (缓存大小限制) — 防止内存问题
9. **P2-1** (Face Matrix3f) — 最低优先级，当前路径可能已避免

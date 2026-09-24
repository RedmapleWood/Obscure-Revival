package redmaple.obscurerevival.modified_arrow.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

/**
 * 特异效果枚举。
 * <p>职责：作为数据组件的序列化标识符，绑定对应的行为策略实例，并分配稳定的位索引用于网络同步。
 */
public enum ArrowSpecialEffect {

    /* 新增特效只能在末尾追加，分配新的 bitIndex，绝不插队、不删除、不重命名 */
    UNDERWATER_NO_DRAG(new UnderwaterNoDragBehavior(), 0), // 0b_0000_0001
    WIND_PUSH(new WindPushBehavior(),                  1), // 0b_0000_0010
    ARMOR_PIERCE(new ArmorPierceBehavior(),            2), // 0b_0000_0100
    ACCURACY_BOOST(new AccuracyBoostBehavior(),        3), // 0b_0000_1000
    DRAG_REDUCE(new DragReduceBehavior(),              4), // 0b_0001_0000
    LIGHT(new LightBehavior(),                         5), // 0b_0010_0000
    DAMAGE_INCREASE(new DamageIncreaseBehavior(),      6); // 0b_0100_0000

    private final SpecialEffectBehavior behavior;
    private final int bitIndex;
    private final long bitMask;

    ArrowSpecialEffect(SpecialEffectBehavior behavior, int bitIndex) {
        this.behavior = behavior;
        this.bitIndex = bitIndex;
        this.bitMask = 1L << bitIndex;
    }

    public SpecialEffectBehavior behavior() { return this.behavior; }
    public int bitIndex() { return this.bitIndex; }
    public long bitMask() { return this.bitMask; }

    /**使用 flatXmap 拦截未知枚举名，防止坏数据导致实体加载崩溃*/
    public static final Codec<ArrowSpecialEffect> CODEC = Codec.STRING.flatXmap(
            name -> {
                try {
                    return DataResult.success(ArrowSpecialEffect.valueOf(name));
                } catch (IllegalArgumentException e) {
                    return DataResult.error(() -> "Unknown arrow special effect: " + name);
                }
            },
            effect -> DataResult.success(effect.name())
    );
}
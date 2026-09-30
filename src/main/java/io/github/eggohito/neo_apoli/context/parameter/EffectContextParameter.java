package io.github.eggohito.neo_apoli.context.parameter;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

public record EffectContextParameter(ResourceLocation name) implements ContextParameter<MobEffectInstance> {

	public static final Codec<EffectContextParameter> CODEC = ContextParameter.codec(NeoApoli.MOD_NAMESPACE, EffectContextParameter::new);
	public static final StreamCodec<ByteBuf, EffectContextParameter> STREAM_CODEC = ContextParameter.streamCodec(EffectContextParameter::new);

	@Override
	public Type<?, ?> getType() {
		return NeoApoliContextParameterTypes.EFFECT;
	}

}

package io.github.eggohito.neo_apoli.context.parameter;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record FloatContextParameter(ResourceLocation name) implements ContextParameter<Float> {

	public static final Codec<FloatContextParameter> CODEC = ContextParameter.codec(NeoApoli.MOD_NAMESPACE, FloatContextParameter::new);
	public static final StreamCodec<ByteBuf, FloatContextParameter> STREAM_CODEC = ContextParameter.streamCodec(FloatContextParameter::new);

	@Override
	public Type<?, ?> getType() {
		return NeoApoliContextParameterTypes.FLOAT;
	}

}

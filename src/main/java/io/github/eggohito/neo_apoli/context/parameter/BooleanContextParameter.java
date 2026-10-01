package io.github.eggohito.neo_apoli.context.parameter;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record BooleanContextParameter(ResourceLocation name) implements ContextParameter<Boolean> {

	public static final Codec<BooleanContextParameter> CODEC = ContextParameter.codec(NeoApoli.MOD_NAMESPACE, BooleanContextParameter::new);
	public static final StreamCodec<ByteBuf, BooleanContextParameter> STREAM_CODEC = ContextParameter.streamCodec(BooleanContextParameter::new);

	@Override
	public Type<?, ?> getType() {
		return NeoApoliContextParameterTypes.BOOLEAN;
	}

}

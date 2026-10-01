package io.github.eggohito.neo_apoli.context.parameter;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record StringContextParameter(ResourceLocation name) implements ContextParameter<String> {

	public static final Codec<StringContextParameter> CODEC = ContextParameter.codec(NeoApoli.MOD_NAMESPACE, StringContextParameter::new);
	public static final StreamCodec<ByteBuf, StringContextParameter> STREAM_CODEC = ContextParameter.streamCodec(StringContextParameter::new);

	@Override
	public Type<?, ?> getType() {
		return NeoApoliContextParameterTypes.STRING;
	}

}

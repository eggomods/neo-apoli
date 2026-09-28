package io.github.eggohito.neo_apoli.context.parameter;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record IntContextParameter(ResourceLocation name) implements Context.Parameter<Integer> {

	public static final Codec<IntContextParameter> CODEC = Context.Parameter.codec(NeoApoli.MOD_NAMESPACE, IntContextParameter::new);
	public static final StreamCodec<ByteBuf, IntContextParameter> STREAM_CODEC = Context.Parameter.streamCodec(IntContextParameter::new);

	@Override
	public Type<?, ?> getType() {
		return NeoApoliContextParameterTypes.INT;
	}

}

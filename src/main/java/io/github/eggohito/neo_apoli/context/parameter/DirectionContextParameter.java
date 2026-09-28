package io.github.eggohito.neo_apoli.context.parameter;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record DirectionContextParameter(ResourceLocation name) implements Context.Parameter<Direction> {

	public static final Codec<DirectionContextParameter> CODEC = Context.Parameter.codec(NeoApoli.MOD_NAMESPACE, DirectionContextParameter::new);
	public static final StreamCodec<ByteBuf, DirectionContextParameter> STREAM_CODEC = Context.Parameter.streamCodec(DirectionContextParameter::new);

	@Override
	public Type<?, ?> getType() {
		return NeoApoliContextParameterTypes.DIRECTION;
	}

}

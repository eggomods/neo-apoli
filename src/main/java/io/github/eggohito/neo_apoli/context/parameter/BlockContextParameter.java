package io.github.eggohito.neo_apoli.context.parameter;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record BlockContextParameter(ResourceLocation name) implements Context.Parameter<CachedBlock> {

	public static final Codec<BlockContextParameter> CODEC = Context.Parameter.codec(NeoApoli.MOD_NAMESPACE, BlockContextParameter::new);
	public static final StreamCodec<ByteBuf, BlockContextParameter> STREAM_CODEC = Context.Parameter.streamCodec(BlockContextParameter::new);

	@Override
	public Type<?, ?> getType() {
		return NeoApoliContextParameterTypes.BLOCK;
	}

}

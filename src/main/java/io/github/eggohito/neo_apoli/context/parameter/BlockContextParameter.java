package io.github.eggohito.neo_apoli.context.parameter;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record BlockContextParameter(ResourceLocation name) implements ContextParameter<CachedBlock> {

	public static final Codec<BlockContextParameter> CODEC = ContextParameter.codec(NeoApoli.MOD_NAMESPACE, BlockContextParameter::new);
	public static final StreamCodec<ByteBuf, BlockContextParameter> STREAM_CODEC = ContextParameter.streamCodec(BlockContextParameter::new);

	@Override
	public Type<?, ?> getType() {
		return NeoApoliContextParameterTypes.BLOCK;
	}

}

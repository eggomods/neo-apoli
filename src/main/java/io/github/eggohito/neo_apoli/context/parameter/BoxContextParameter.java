package io.github.eggohito.neo_apoli.context.parameter;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public record BoxContextParameter(ResourceLocation name) implements ContextParameter<AABB> {

	public static final Codec<BoxContextParameter> CODEC = ContextParameter.codec(NeoApoli.MOD_NAMESPACE, BoxContextParameter::new);
	public static final StreamCodec<ByteBuf, BoxContextParameter> STREAM_CODEC = ContextParameter.streamCodec(BoxContextParameter::new);

	@Override
	public Type<?, ?> getType() {
		return NeoApoliContextParameterTypes.BOX;
	}

}

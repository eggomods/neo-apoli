package io.github.eggohito.neo_apoli.context.parameter;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SlotAccess;

public record SlotContextParameter(ResourceLocation name) implements Context.Parameter<SlotAccess> {

	public static final Codec<SlotContextParameter> CODEC = Context.Parameter.codec(NeoApoli.MOD_NAMESPACE, SlotContextParameter::new);
	public static final StreamCodec<ByteBuf, SlotContextParameter> STREAM_CODEC = Context.Parameter.streamCodec(SlotContextParameter::new);

	@Override
	public Type<?, ?> getType() {
		return NeoApoliContextParameterTypes.SLOT;
	}

}

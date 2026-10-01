package io.github.eggohito.neo_apoli.context.parameter;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record NbtContextParameter(ResourceLocation name) implements ContextParameter<Tag> {

	public static final Codec<NbtContextParameter> CODEC = ContextParameter.codec(NeoApoli.MOD_NAMESPACE, NbtContextParameter::new);
	public static final StreamCodec<ByteBuf, NbtContextParameter> STREAM_CODEC = ContextParameter.streamCodec(NbtContextParameter::new);

	@Override
	public Type<?, ?> getType() {
		return NeoApoliContextParameterTypes.NBT;
	}

}

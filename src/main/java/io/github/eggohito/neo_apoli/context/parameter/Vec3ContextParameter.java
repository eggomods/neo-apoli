package io.github.eggohito.neo_apoli.context.parameter;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public record Vec3ContextParameter(ResourceLocation name) implements ContextParameter<Vec3> {

	public static final Codec<Vec3ContextParameter> CODEC = ContextParameter.codec(NeoApoli.MOD_NAMESPACE, Vec3ContextParameter::new);
	public static final StreamCodec<ByteBuf, Vec3ContextParameter> STREAM_CODEC = ContextParameter.streamCodec(Vec3ContextParameter::new);

	@Override
	public Type<?, ?> getType() {
		return NeoApoliContextParameterTypes.VEC3;
	}

}

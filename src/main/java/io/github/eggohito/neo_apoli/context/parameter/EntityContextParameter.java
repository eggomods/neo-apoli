package io.github.eggohito.neo_apoli.context.parameter;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public record EntityContextParameter(ResourceLocation name) implements ContextParameter<Entity> {

	public static final Codec<EntityContextParameter> CODEC = ContextParameter.codec(NeoApoli.MOD_NAMESPACE, EntityContextParameter::new);
	public static final StreamCodec<ByteBuf, EntityContextParameter> STREAM_CODEC = ContextParameter.streamCodec(EntityContextParameter::new);

	@Override
	public Type<?, ?> getType() {
		return NeoApoliContextParameterTypes.ENTITY;
	}

}

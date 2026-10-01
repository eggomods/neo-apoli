package io.github.eggohito.neo_apoli.context.parameter;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.netty.buffer.ByteBuf;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record CommandSourceContextParameter(ResourceLocation name) implements ContextParameter<CommandSourceStack> {

	public static final Codec<CommandSourceContextParameter> CODEC = ContextParameter.codec(NeoApoli.MOD_NAMESPACE, CommandSourceContextParameter::new);
	public static final StreamCodec<ByteBuf, CommandSourceContextParameter> STREAM_CODEC = ContextParameter.streamCodec(CommandSourceContextParameter::new);

	@Override
	public Type<?, ?> getType() {
		return NeoApoliContextParameterTypes.COMMAND_SOURCE;
	}

}

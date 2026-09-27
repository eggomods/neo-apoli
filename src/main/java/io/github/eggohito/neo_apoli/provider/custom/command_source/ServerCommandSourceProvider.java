package io.github.eggohito.neo_apoli.provider.custom.command_source;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.config.NeoApoliCommonConfig;
import io.github.eggohito.neo_apoli.context.Context;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public enum ServerCommandSourceProvider implements SimpleCommandSourceProvider<ServerCommandSourceProvider> {

	INSTANCE;

	public static final MapCodec<ServerCommandSourceProvider> CODEC = MapCodec.unit(INSTANCE);
	public static final StreamCodec<RegistryFriendlyByteBuf, ServerCommandSourceProvider> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public MapCodec<ServerCommandSourceProvider> mapCodec() {
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ServerCommandSourceProvider> streamCodec() {
		return STREAM_CODEC;
	}

	@Override
	public Optional<CommandSourceStack> getValue(Context context) {
		return Optional.ofNullable(context.level().getServer())
			.map(MinecraftServer::createCommandSourceStack)
			.map(NeoApoliCommonConfig.INSTANCE.command.get()::sanitizeSource);
	}

}

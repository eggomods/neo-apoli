package io.github.eggohito.neo_apoli.provider.custom.command_source;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.config.NeoApoliCommonConfig;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.block.BlockProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliCommandSourceProviderTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public record BlockCommandSourceProvider(BlockProvider block) implements CommandSourceProvider {

	public static final MapCodec<BlockCommandSourceProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(BlockProvider.CODEC.fieldOf("block").forGetter(BlockCommandSourceProvider::block))
		.apply(instance, BlockCommandSourceProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, BlockCommandSourceProvider> STREAM_CODEC = StreamCodec.composite(
		BlockProvider.STREAM_CODEC, BlockCommandSourceProvider::block,
		BlockCommandSourceProvider::new
	);

	@Override
	public CommandSourceProvider.@NotNull Type<?> getType() {
		return NeoApoliCommandSourceProviderTypes.BLOCK;
	}

	@Override
	public Optional<CommandSourceStack> getValue(Context context) {

		if (context.level() instanceof ServerLevel serverLevel) {
			return block().getValue(context.forChild(".block"))
				.map(block -> this.getCommandSource(serverLevel, block))
				.map(NeoApoliCommonConfig.INSTANCE.command.get()::sanitizeSource);
		}

		else {
			return Optional.empty();
		}

	}

	@Override
	public void validate(ContextValidator validator) {
		CommandSourceProvider.super.validate(validator);
		block().validate(validator.forChild(".block"));
	}

	private CommandSourceStack getCommandSource(ServerLevel level, CachedBlock block) {
		Component blockName = Component.translatable(block.state().getBlock().getDescriptionId());
		return new CommandSourceStack(
			CommandSource.NULL,
			block.pos().getCenter(),
			Vec2.ZERO,
			level,
			0,
			blockName.getString(),
			blockName,
			level.getServer(),
			null
		);
	}

}

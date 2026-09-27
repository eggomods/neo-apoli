package io.github.eggohito.neo_apoli.provider.custom.entity;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.config.NeoApoliCommonConfig;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliEntityProviderTypes;
import io.github.eggohito.neo_apoli.util.ParsedArgument;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record SelectorEntityProvider(ParsedArgument<EntitySelector> selector) implements EntityProvider {

	public static final MapCodec<SelectorEntityProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(NeoApoliCodecs.ENTITY_SELECTOR.fieldOf("selector").forGetter(SelectorEntityProvider::selector))
		.apply(instance, SelectorEntityProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, SelectorEntityProvider> STREAM_CODEC = StreamCodec.composite(
		NeoApoliStreamCodecs.ENTITY_SELECTOR, SelectorEntityProvider::selector,
		SelectorEntityProvider::new
	);

	@Override
	public EntityProvider.@NotNull Type<?> getType() {
		return NeoApoliEntityProviderTypes.SELECTOR;
	}

	@Override
	public Optional<Entity> getValue(Context context) {

		Level level = context.level();
		MinecraftServer server = level.getServer();

		if (server == null) {
			return Optional.empty();
		}

		//noinspection UnstableApiUsage
		CommandSourceStack source = server.createCommandSourceStack()
			.withSource(NeoApoliCommonConfig.INSTANCE.command.get().showOutput() ? server : CommandSource.NULL)
			.withPermission(NeoApoliCommonConfig.INSTANCE.command.get().permissionLevel());

		try {
			return Optional.of(selector().argument().findSingleEntity(source));
		}

		catch (CommandSyntaxException e) {
			context.reportProblem(e.getMessage());
		}

		return Optional.empty();

	}

}

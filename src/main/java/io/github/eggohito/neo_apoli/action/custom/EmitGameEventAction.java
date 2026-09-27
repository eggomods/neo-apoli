package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.block.BlockProvider;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record EmitGameEventAction(Holder<GameEvent> gameEvent, Vec3Provider position, Optional<EntityProvider> entitySource, Optional<BlockProvider> blockSource) implements Action {

	public static final MapCodec<EmitGameEventAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		GameEvent.CODEC.fieldOf("game_event").forGetter(EmitGameEventAction::gameEvent),
		Vec3Provider.CODEC.fieldOf("position").forGetter(EmitGameEventAction::position),
		EntityProvider.CODEC.optionalFieldOf("entity_source").forGetter(EmitGameEventAction::entitySource),
		BlockProvider.CODEC.optionalFieldOf("block_source").forGetter(EmitGameEventAction::blockSource)
	).apply(instance, EmitGameEventAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, EmitGameEventAction> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.holderRegistry(Registries.GAME_EVENT), EmitGameEventAction::gameEvent,
		Vec3Provider.STREAM_CODEC, EmitGameEventAction::position,
		ByteBufCodecs.optional(EntityProvider.STREAM_CODEC), EmitGameEventAction::entitySource,
		ByteBufCodecs.optional(BlockProvider.STREAM_CODEC), EmitGameEventAction::blockSource,
		EmitGameEventAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.EMIT_GAME_EVENT;
	}

	@Override
	public void execute(Context context) {

		Vec3 position = position()
			.getValue(context.forChild(".position"))
			.orElse(null);

		if (position == null) {
			return;
		}

		Entity entitySource = entitySource()
			.flatMap(p -> p.getValue(context.forChild(".entity_source")))
			.orElse(null);
		BlockState blockSource = blockSource()
			.flatMap(p -> p.getValue(context.forChild(".block_source")))
			.map(CachedBlock::state)
			.orElse(null);

		context.level().gameEvent(gameEvent(), position, new GameEvent.Context(entitySource, blockSource));

	}

	@Override
	public void validate(Context.Validator validator) {
		Action.super.validate(validator);
		position().validate(validator.forChild(".position"));
		entitySource().ifPresent(p -> p.validate(validator.forChild(".entity_source")));
		blockSource().ifPresent(p -> p.validate(validator.forChild(".block_source")));
	}

}

package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public record TameAction(EntityProvider tameable, EntityProvider owner) implements Action {

	public static final MapCodec<TameAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityProvider.CODEC.fieldOf("tameable").forGetter(TameAction::tameable),
		EntityProvider.CODEC.fieldOf("owner").forGetter(TameAction::owner)
	).apply(instance, TameAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, TameAction> STREAM_CODEC = StreamCodec.composite(
		EntityProvider.STREAM_CODEC, TameAction::tameable,
		EntityProvider.STREAM_CODEC, TameAction::owner,
		TameAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.TAME;
	}

	@Override
	public void execute(Context context) {

		if (context.level().isClientSide()) {
			return;
		}

		Entity tameable = tameable().getValue(context.forChild(".tameable")).orElse(null);
		Entity owner = owner().getValue(context.forChild(".owner")).orElse(null);

		if (owner instanceof ServerPlayer serverPlayer) {

			switch (tameable) {
				case TamableAnimal animal ->
					animal.tame(serverPlayer);
				case AbstractHorse horse ->
					horse.tameWithName(serverPlayer);
				case null, default -> {
					//  No-op; either null or unsupported
				}
			}

		}

	}

	@Override
	public void validate(Context.Validator validator) {
		Action.super.validate(validator);
		tameable().validate(validator.forChild(".tameable"));
		owner().validate(validator.forChild(".owner"));
	}

}

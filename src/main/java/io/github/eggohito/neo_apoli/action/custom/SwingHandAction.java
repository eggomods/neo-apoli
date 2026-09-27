package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public record SwingHandAction(Optional<InteractionHand> hand, EntityProvider entity) implements Action {

	public static final MapCodec<SwingHandAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		NeoApoliCodecs.HAND.optionalFieldOf("hand").forGetter(SwingHandAction::hand),
		EntityProvider.CODEC.fieldOf("entity").forGetter(SwingHandAction::entity)
	).apply(instance, SwingHandAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, SwingHandAction> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(NeoApoliStreamCodecs.HAND), SwingHandAction::hand,
		EntityProvider.STREAM_CODEC, SwingHandAction::entity,
		SwingHandAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.SWING_HAND;
	}

	@Override
	public void execute(Context context) {
		entity().getValue(context.forChild(".entity"))
			.filter(LivingEntity.class::isInstance)
			.map(LivingEntity.class::cast)
			.ifPresent(this::swingHand);
	}

	@Override
	public void validate(Context.Validator validator) {
		Action.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

	private void swingHand(LivingEntity entity) {
		entity.swing(hand().orElseGet(entity::getUsedItemHand), entity instanceof ServerPlayer);
	}

}

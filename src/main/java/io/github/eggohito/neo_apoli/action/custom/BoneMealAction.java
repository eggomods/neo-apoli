package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.bool.BooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.bool.ConstantBooleanProvider;
import io.github.eggohito.neo_apoli.provider.custom.direction.DirectionProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.LevelEvent;

import java.util.Optional;

public record BoneMealAction(Vec3Provider position, BooleanProvider showEffects, Optional<DirectionProvider> offsetDirection) implements Action {

	public static final MapCodec<BoneMealAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Vec3Provider.CODEC.fieldOf("position").forGetter(BoneMealAction::position),
		BooleanProvider.CODEC.optionalFieldOf("show_effects", new ConstantBooleanProvider(true)).forGetter(BoneMealAction::showEffects),
		DirectionProvider.CODEC.optionalFieldOf("offset_direction").forGetter(BoneMealAction::offsetDirection)
	).apply(instance, BoneMealAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, BoneMealAction> STREAM_CODEC = StreamCodec.composite(
		Vec3Provider.STREAM_CODEC, BoneMealAction::position,
		BooleanProvider.STREAM_CODEC, BoneMealAction::showEffects,
		ByteBufCodecs.optional(DirectionProvider.STREAM_CODEC), BoneMealAction::offsetDirection,
		BoneMealAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.BONE_MEAL;
	}

	@Override
	public void execute(Context context) {

		if (!context.level().isClientSide()) {
			position().getValue(context.forChild(".position"))
				.map(BlockPos::containing)
				.ifPresent(position -> this.apply(context, position));
		}

	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		position().validate(validator.forChild(".position"));
		showEffects().validate(validator.forChild(".show_effects"));
		offsetDirection().ifPresent(offsetDirection -> offsetDirection.validate(validator.forChild(".offset_direction")));
	}

	private void apply(Context context, BlockPos position) {

		if (BoneMealItem.growCrop(ItemStack.EMPTY, context.level(), position)) {
			this.showEffects(context, position);
		}

		else {

			Direction offsetDirection = offsetDirection()
				.flatMap(self -> self.getValue(context.forChild(".offset_direction")))
				.orElse(null);

			if (offsetDirection != null && context.level().getBlockState(position).isFaceSturdy(context.level(), position, offsetDirection) && BoneMealItem.growWaterPlant(ItemStack.EMPTY, context.level(), position.relative(offsetDirection), offsetDirection)) {
				this.showEffects(context, position);
			}

		}

	}

	private void showEffects(Context context, BlockPos position) {

		if (showEffects().getBoolean(context.forChild(".show_effects"))) {
			context.level().levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, position, 15);
		}

	}

}

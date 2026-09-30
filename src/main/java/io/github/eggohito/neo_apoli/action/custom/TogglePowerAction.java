package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.PowerIdentifier;
import io.github.eggohito.neo_apoli.power.custom.TogglePower;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

public record TogglePowerAction(PowerIdentifier power, EntityProvider entity) implements Action {

	public static final MapCodec<TogglePowerAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		PowerIdentifier.CODEC.fieldOf("power").forGetter(TogglePowerAction::power),
		EntityProvider.CODEC.fieldOf("entity").forGetter(TogglePowerAction::entity)
	).apply(instance, TogglePowerAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, TogglePowerAction> STREAM_CODEC = StreamCodec.composite(
		PowerIdentifier.STREAM_CODEC, TogglePowerAction::power,
		EntityProvider.STREAM_CODEC, TogglePowerAction::entity,
		TogglePowerAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.TOGGLE_POWER;
	}

	@Override
	public void execute(Context context) {

		Entity entity = entity().getValue(context.forChild(".entity")).orElse(null);
		Power.Instance<?> instance = Powers.getOptional(entity)
			.map(powers -> powers.getInstance(this.power()))
			.orElse(null);

		if (entity != null && instance instanceof TogglePower.Instance toggleInstance) {
			toggleInstance.toggle(entity, context);
		}

	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		power().validate(validator.forChild(".power"), TogglePower.class, () -> power().asDisplayString() + " cannot be toggled!");
		entity().validate(validator.forChild(".entity"));
	}

}

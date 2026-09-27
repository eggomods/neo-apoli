package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record SetEntityOnFireAction(IntProvider ticks, EntityProvider entity) implements Action {

	public static final MapCodec<SetEntityOnFireAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		IntProvider.CODEC.fieldOf("ticks").forGetter(SetEntityOnFireAction::ticks),
		EntityProvider.CODEC.fieldOf("entity").forGetter(SetEntityOnFireAction::entity)
	).apply(instance, SetEntityOnFireAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, SetEntityOnFireAction> STREAM_CODEC = StreamCodec.composite(
		IntProvider.STREAM_CODEC, SetEntityOnFireAction::ticks,
		EntityProvider.STREAM_CODEC, SetEntityOnFireAction::entity,
		SetEntityOnFireAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.SET_ENTITY_ON_FIRE;
	}

	@Override
	public void execute(Context context) {
		entity()
			.getValue(context.forChild(".entity"))
			.ifPresent(entity -> entity.igniteForTicks(ticks().getInt(context.forChild(".ticks"))));
	}

	@Override
	public void validate(Context.Validator validator) {
		Action.super.validate(validator);
		ticks().validate(validator.forChild(".ticks"));
		entity().validate(validator.forChild(".entity"));
	}

}

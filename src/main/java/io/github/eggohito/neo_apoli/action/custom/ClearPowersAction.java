package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.power.entity.MutablePowers;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record ClearPowersAction(EntityProvider entity) implements Action {

	public static final MapCodec<ClearPowersAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityProvider.CODEC.fieldOf("entity").forGetter(ClearPowersAction::entity)
	).apply(instance, ClearPowersAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ClearPowersAction> STREAM_CODEC = StreamCodec.composite(
		EntityProvider.STREAM_CODEC, ClearPowersAction::entity,
		ClearPowersAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.CLEAR_POWERS;
	}

	@Override
	public void execute(Context context) {
		entity().getValue(context.forChild(".entity"))
			.flatMap(MutablePowers::getOptional)
			.ifPresent(this::clear);
	}

	@Override
	public void validate(Context.Validator validator) {
		Action.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

	private void clear(MutablePowers mutable) {

		try (mutable) {

			for (var holder : mutable.getAll()) {

				for (var source : mutable.getSources(holder.id())) {
					mutable.revoke(holder.id(), source);
				}

			}

		}

	}

}

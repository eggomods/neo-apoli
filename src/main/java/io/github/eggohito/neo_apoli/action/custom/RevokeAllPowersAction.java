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
import net.minecraft.resources.ResourceLocation;

public record RevokeAllPowersAction(ResourceLocation source, EntityProvider entity) implements Action {

	public static final MapCodec<RevokeAllPowersAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		ResourceLocation.CODEC.fieldOf("source").forGetter(RevokeAllPowersAction::source),
		EntityProvider.CODEC.fieldOf("entity").forGetter(RevokeAllPowersAction::entity)
	).apply(instance, RevokeAllPowersAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, RevokeAllPowersAction> STREAM_CODEC = StreamCodec.composite(
		ResourceLocation.STREAM_CODEC, RevokeAllPowersAction::source,
		EntityProvider.STREAM_CODEC, RevokeAllPowersAction::entity,
		RevokeAllPowersAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.REVOKE_ALL_POWERS;
	}

	@Override
	public void execute(Context context) {
		entity().getValue(context.forChild(".entity"))
			.flatMap(MutablePowers::getOptional)
			.ifPresent(this::revokeAll);
	}

	@Override
	public void validate(Context.Validator validator) {
		Action.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

	private void revokeAll(MutablePowers mutable) {

		try (mutable) {

			for (var holder : mutable.getAllFromSource(source())) {
				mutable.revoke(holder.id(), source());
			}

		}

	}

}

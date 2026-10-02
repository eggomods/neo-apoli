package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

public record DismountAction(EntityProvider entity) implements Action {

	public static final MapCodec<DismountAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(EntityProvider.CODEC.fieldOf("entity").forGetter(DismountAction::entity))
		.apply(instance, DismountAction::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, DismountAction> STREAM_CODEC = StreamCodec.composite(
		EntityProvider.STREAM_CODEC, DismountAction::entity,
		DismountAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.DISMOUNT;
	}

	@Override
	public void execute(Context context) {
		entity().getValue(context.forChild(".entity")).ifPresent(Entity::stopRiding);
	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

}

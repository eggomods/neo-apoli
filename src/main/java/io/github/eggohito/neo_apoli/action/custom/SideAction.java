package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.util.Environment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record SideAction(Environment side, Action action) implements Action {

	public static final MapCodec<SideAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Environment.CODEC.fieldOf("side").forGetter(SideAction::side),
		Action.CODEC.fieldOf("action").forGetter(SideAction::action)
	).apply(instance, SideAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, SideAction> STREAM_CODEC = StreamCodec.composite(
		Environment.STREAM_CODEC, SideAction::side,
		Action.STREAM_CODEC, SideAction::action,
		SideAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.SIDE;
	}

	@Override
	public void execute(Context context) {

		if ((side() == Environment.CLIENT) == context.level().isClientSide()) {
			action().execute(context.forChild(".action"));
		}

	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		action().validate(validator.forChild(".action"));
	}

}

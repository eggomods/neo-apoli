package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidatable;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record SequenceAction(List<Action> actions) implements Action {

	public static final MapCodec<SequenceAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(Action.CODEC.listOf().fieldOf("actions").forGetter(SequenceAction::actions))
		.apply(instance, SequenceAction::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, SequenceAction> STREAM_CODEC = StreamCodec.composite(
		Action.STREAM_CODEC.apply(ByteBufCodecs.list()), SequenceAction::actions,
		SequenceAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.SEQUENCE;
	}

	@Override
	public void execute(Context context) {
		MiscUtil.iterateList(actions(), (index, action) -> action.execute(context.forChild(".actions[" + index + "]")));
	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		ContextValidatable.validate(actions(), validator, index -> ".actions[" + index + "]");
	}

}

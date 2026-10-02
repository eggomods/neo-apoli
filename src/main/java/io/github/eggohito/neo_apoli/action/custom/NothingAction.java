package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public enum NothingAction implements Action {

	INSTANCE;

	public static final MapCodec<NothingAction> CODEC = MapCodec.unit(INSTANCE);
	public static final StreamCodec<RegistryFriendlyByteBuf, NothingAction> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.NOTHING;
	}

	@Override
	public void execute(Context context) {

	}

	@Override
	public void validate(ContextValidator validator) {

	}

}

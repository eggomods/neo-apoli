package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.key.manager.KeyStateManager;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.provider.custom.string.StringProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record KeyPressedTimeIntProvider(StringProvider id, EntityProvider entity) implements IntProvider {

	public static final MapCodec<KeyPressedTimeIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		StringProvider.CODEC.fieldOf("id").forGetter(KeyPressedTimeIntProvider::id),
		EntityProvider.CODEC.fieldOf("entity").forGetter(KeyPressedTimeIntProvider::entity)
	).apply(instance, KeyPressedTimeIntProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, KeyPressedTimeIntProvider> STREAM_CODEC = StreamCodec.composite(
		StringProvider.STREAM_CODEC, KeyPressedTimeIntProvider::id,
		EntityProvider.STREAM_CODEC, KeyPressedTimeIntProvider::entity,
		KeyPressedTimeIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.KEY_PRESSED_TIME;
	}

	@Override
	public Optional<Integer> getValue(Context context) {
		return id().getValue(context.forChild(".id"))
			.flatMap(id -> entity().getValue(context.forChild(".entity"))
				.flatMap(entity -> KeyStateManager.getInstance().getCurrentState(entity.getUUID(), id)
					.map(state -> (int) state.pressedTime())));
	}

	@Override
	public void validate(ContextValidator validator) {
		IntProvider.super.validate(validator);
		id().validate(validator.forChild(".id"));
		entity().validate(validator.forChild(".entity"));
	}

}

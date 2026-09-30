package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record SlotIdIntProvider(SlotRange slot) implements IntProvider {

	public static final MapCodec<SlotIdIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(SlotRanges.CODEC.fieldOf("slot").forGetter(SlotIdIntProvider::slot))
		.apply(instance, SlotIdIntProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, SlotIdIntProvider> STREAM_CODEC = StreamCodec.composite(
		NeoApoliStreamCodecs.SLOT_RANGE, SlotIdIntProvider::slot,
		SlotIdIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.SLOT_ID;
	}

	@Override
	public Optional<Integer> getValue(Context context) {
		return Optional.of(slot().slots().getFirst());
	}

	@Override
	public void validate(ContextValidator validator) {

		IntProvider.super.validate(validator);

		if (slot().size() > 1) {
			validator.forChild(".slot").reportProblem("Slot with multiple IDs is not allowed!");
		}

	}

}

package io.github.eggohito.neo_apoli.provider.custom.slot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParams;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliSlotProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.SlotAccess;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public record ContextSlotProvider(Context.Parameter<SlotAccess> parameter) implements SlotProvider {

	public static final MapCodec<ContextSlotProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(NeoApoliContextParams.Codecs.SLOT.fieldOf("parameter").forGetter(ContextSlotProvider::parameter))
		.apply(instance, ContextSlotProvider::new)
	);

	public static final Codec<ContextSlotProvider> INLINE_CODEC = NeoApoliContextParams.Codecs.SLOT.xmap(
		ContextSlotProvider::new,
		ContextSlotProvider::parameter
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ContextSlotProvider> STREAM_CODEC = StreamCodec.composite(
		NeoApoliContextParams.StreamCodecs.SLOT, ContextSlotProvider::parameter,
		ContextSlotProvider::new
	);

	@Override
	public SlotProvider.@NotNull Type<?> getType() {
		return NeoApoliSlotProviderTypes.CONTEXT;
	}

	@Override
	public Optional<SlotAccess> getValue(Context context) {
		return context.getOptional(parameter());
	}

	@Override
	public Set<ContextKey<?>> getRequiredParameters() {
		return Set.of(parameter());
	}

}

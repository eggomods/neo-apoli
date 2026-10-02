package io.github.eggohito.neo_apoli.provider.custom.box;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.provider.ConditionalValueProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliBoxProviderTypes;
import io.github.eggohito.neo_apoli.util.MapCodecUtil;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public record ConditionalBoxProvider(Condition condition, BoxProvider onTrue, BoxProvider onFalse) implements BoxProvider, ConditionalValueProvider<AABB, BoxProvider> {

	public static final MapCodec<ConditionalBoxProvider> CODEC = MapCodecUtil.lazy(ConditionalBoxProvider.class.getSimpleName(), () -> ConditionalValueProvider.mapCodec(BoxProvider.CODEC, ConditionalBoxProvider::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ConditionalBoxProvider> STREAM_CODEC = StreamCodecUtil.lazy(ConditionalBoxProvider.class.getSimpleName(), () -> ConditionalValueProvider.streamCodec(BoxProvider.STREAM_CODEC, ConditionalBoxProvider::new));

	@Override
	public @NotNull BoxProvider.Type<?> getType() {
		return NeoApoliBoxProviderTypes.CONDITIONAL;
	}

}

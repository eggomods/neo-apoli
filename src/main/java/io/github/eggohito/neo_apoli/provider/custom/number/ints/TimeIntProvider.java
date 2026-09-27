package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record TimeIntProvider(Optional<IntProvider> period) implements IntProvider {

	public static final MapCodec<TimeIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(IntProvider.CODEC.optionalFieldOf("period").forGetter(TimeIntProvider::period))
		.apply(instance, TimeIntProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, TimeIntProvider> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(IntProvider.STREAM_CODEC), TimeIntProvider::period,
		TimeIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.TIME;
	}

	@Override
	public Optional<Integer> getValue(Context context) {

		Level level = context.level();
		int time = Mth.clamp((int) level.getGameTime(), Integer.MIN_VALUE, Integer.MAX_VALUE);

		if (period().isPresent()) {

			Context periodContext = context.forChild(".period");
			int period = period().get().getInt(periodContext);

			if (!periodContext.hasProblems()) {
				time %= period;
			}

		}

		return Optional.of(time);

	}

	@Override
	public void validate(Context.Validator validator) {
		IntProvider.super.validate(validator);
		period().ifPresent(period -> period.validate(validator.forChild(".period")));
	}

}

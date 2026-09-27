package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record LightLevelIntProvider(Optional<LightLayer> lightType, Vec3Provider position) implements IntProvider {

	public static final MapCodec<LightLevelIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		NeoApoliCodecs.LIGHT_TYPE.optionalFieldOf("light_type").forGetter(LightLevelIntProvider::lightType),
		Vec3Provider.CODEC.fieldOf("position").forGetter(LightLevelIntProvider::position)
	).apply(instance, LightLevelIntProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, LightLevelIntProvider> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(NeoApoliStreamCodecs.LIGHT_TYPE), LightLevelIntProvider::lightType,
		Vec3Provider.STREAM_CODEC, LightLevelIntProvider::position,
		LightLevelIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.LIGHT_LEVEL;
	}

	@Override
	public Optional<Integer> getValue(Context context) {

		BlockPos position = position().getValue(context.forChild(".position"))
			.map(BlockPos::containing)
			.orElse(null);

		if (position == null) {
			return Optional.empty();
		}

		Level level = context.level();
		return Optional.of(lightType()
			.map(lightType -> level.getBrightness(lightType, position))
			.orElseGet(() -> level.getMaxLocalRawBrightness(position)));

	}

	@Override
	public void validate(Context.Validator validator) {
		IntProvider.super.validate(validator);
		position().validate(validator.forChild(".position"));
	}

}

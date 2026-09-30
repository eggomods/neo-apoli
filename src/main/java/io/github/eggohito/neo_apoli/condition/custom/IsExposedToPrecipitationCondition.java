package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.exception.PosOutOfBoundsException;
import io.github.eggohito.neo_apoli.exception.PosUnloadedException;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public record IsExposedToPrecipitationCondition(Biome.Precipitation precipitation, Vec3Provider position) implements Condition {

	public static final MapCodec<IsExposedToPrecipitationCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		NeoApoliCodecs.PRECIPITATION.fieldOf("precipitation").forGetter(IsExposedToPrecipitationCondition::precipitation),
		Vec3Provider.CODEC.fieldOf("position").forGetter(IsExposedToPrecipitationCondition::position)
	).apply(instance, IsExposedToPrecipitationCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, IsExposedToPrecipitationCondition> STREAM_CODEC = StreamCodec.composite(
		NeoApoliStreamCodecs.PRECIPITATION, IsExposedToPrecipitationCondition::precipitation,
		Vec3Provider.STREAM_CODEC, IsExposedToPrecipitationCondition::position,
		IsExposedToPrecipitationCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.IS_EXPOSED_TO_PRECIPITATION;
	}

	@Override
	public boolean test(Context context) {

		exposureCheck: try {

			BlockPos position = position().getValue(context.forChild(".position"))
				.map(BlockPos::containing)
				.orElse(null);

			if (position == null) {
				break exposureCheck;
			}

			Level level = context.level();
			CachedBlock block = CachedBlock.fromLoadedPos(level, position);

			BlockPos pos = block.pos();
			Biome biome = level.getBiome(pos).value();

			return level.canSeeSky(block.pos())
				&& level.isRainingAt(block.pos())
				&& biome.getPrecipitationAt(pos, level.getSeaLevel()) == precipitation();

		}

		catch (PosUnloadedException | PosOutOfBoundsException ignored) {
			//  No-op
		}

		return false;

	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		position().validate(validator.forChild(".position"));
	}

}

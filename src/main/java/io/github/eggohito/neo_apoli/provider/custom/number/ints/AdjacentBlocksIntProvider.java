package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParams;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import io.github.eggohito.neo_apoli.util.MapCodecUtil;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record AdjacentBlocksIntProvider(Condition condition, Vec3Provider position) implements IntProvider {

	public static final Context.Parameter<CachedBlock> ADJACENT_BLOCK = NeoApoliContextParams.registerSimpleInternal("adjacent_block", CachedBlock.class);
	public static final ContextKeySet CONDITION_PARAMETER_SET = new ContextKeySet.Builder().required(ADJACENT_BLOCK).build();

	public static final MapCodec<AdjacentBlocksIntProvider> CODEC = MapCodecUtil.lazy(AdjacentBlocksIntProvider.class.getSimpleName(), () -> RecordCodecBuilder.mapCodec(instance -> instance.group(
		Condition.CODEC.fieldOf("condition").forGetter(AdjacentBlocksIntProvider::condition),
		Vec3Provider.CODEC.fieldOf("position").forGetter(AdjacentBlocksIntProvider::position)
	).apply(instance, AdjacentBlocksIntProvider::new)));

	public static final StreamCodec<RegistryFriendlyByteBuf, AdjacentBlocksIntProvider> STREAM_CODEC = StreamCodecUtil.lazy(AdjacentBlocksIntProvider.class.getSimpleName(), () -> StreamCodec.composite(
		Condition.STREAM_CODEC, AdjacentBlocksIntProvider::condition,
		Vec3Provider.STREAM_CODEC, AdjacentBlocksIntProvider::position,
		AdjacentBlocksIntProvider::new
	));

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.ADJACENT_BLOCKS;
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
		int matches = 0;

		for (var direction : Direction.values()) {

			CachedBlock adjacentBlock = CachedBlock
				.optionallyFromLoadedPos(level, position.relative(direction))
				.orElse(null);

			if (adjacentBlock == null) {
				continue;
			}

			Context blockContext = new Context.Builder(context)
				.withRequired(ADJACENT_BLOCK, adjacentBlock)
				.build(level);

			if (condition().test(blockContext.forChild(".condition"))) {
				matches++;
			}

		}

		return Optional.of(matches);

	}

	@Override
	public void validate(Context.Validator validator) {
		IntProvider.super.validate(validator);
		condition().validate(validator.withAdditionalKeysFromSets(CONDITION_PARAMETER_SET).forChild(".condition"));
		position().validate(validator.forChild(".position"));
	}

}

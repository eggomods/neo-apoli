package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextParams;
import io.github.eggohito.neo_apoli.context.parameter.BlockContextParameter;
import io.github.eggohito.neo_apoli.provider.custom.box.BoxProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record BlocksIntersectingBoxIntProvider(Condition condition, BoxProvider box) implements IntProvider {

	public static final BlockContextParameter BLOCK_INTERSECTING_BOX = new BlockContextParameter(NeoApoli.id("block_intersecting_box"));
	public static final ContextParams CONDITION_PARAMETER_SET = new ContextParams.Builder().required(BLOCK_INTERSECTING_BOX).build();

	public static final MapCodec<BlocksIntersectingBoxIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Condition.CODEC.fieldOf("condition").forGetter(BlocksIntersectingBoxIntProvider::condition),
		BoxProvider.CODEC.fieldOf("box").forGetter(BlocksIntersectingBoxIntProvider::box)
	).apply(instance, BlocksIntersectingBoxIntProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, BlocksIntersectingBoxIntProvider> STREAM_CODEC = StreamCodec.composite(
		Condition.STREAM_CODEC, BlocksIntersectingBoxIntProvider::condition,
		BoxProvider.STREAM_CODEC, BlocksIntersectingBoxIntProvider::box,
		BlocksIntersectingBoxIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.BLOCKS_INTERSECTING_BOX;
	}

	@Override
	public Optional<Integer> getValue(Context context) {

		Context boxContext = context.forChild(".box");
		AABB box = box().getValue(boxContext).orElse(null);

		if (box == null) {
			return Optional.empty();
		}

		Level level = context.level();
		int matches = 0;

		for (var position : BlockPos.betweenClosed(box)) {

			CachedBlock blockIntersectingBox = CachedBlock
				.optionallyFromLoadedPos(level, position)
				.orElse(null);

			if (blockIntersectingBox == null) {
				continue;
			}

			Context blockContext = new Context.Builder(context)
				.withRequired(BLOCK_INTERSECTING_BOX, blockIntersectingBox)
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
		condition().validate(validator.withParams(CONDITION_PARAMETER_SET).forChild(".condition"));
		box().validate(validator.forChild(".box"));
	}

}

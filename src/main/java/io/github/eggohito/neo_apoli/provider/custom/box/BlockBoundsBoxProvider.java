package io.github.eggohito.neo_apoli.provider.custom.box;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.block.BlockProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliBoxProviderTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record BlockBoundsBoxProvider(ClipContext.Block shapeType, BlockProvider block) implements BoxProvider {

	public static final MapCodec<BlockBoundsBoxProvider> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		NeoApoliCodecs.BLOCK_CLIP_CONTEXT.optionalFieldOf("shape_type", ClipContext.Block.OUTLINE).forGetter(BlockBoundsBoxProvider::shapeType),
		BlockProvider.CODEC.fieldOf("block").forGetter(BlockBoundsBoxProvider::block)
	).apply(instance, BlockBoundsBoxProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, BlockBoundsBoxProvider> STREAM_CODEC = StreamCodec.composite(
		NeoApoliStreamCodecs.BLOCK_CLIP_CONTEXT, BlockBoundsBoxProvider::shapeType,
		BlockProvider.STREAM_CODEC, BlockBoundsBoxProvider::block,
		BlockBoundsBoxProvider::new
	);

	@Override
	public @NotNull BoxProvider.Type<?> getType() {
		return NeoApoliBoxProviderTypes.BLOCK_BOUNDS;
	}

	@Override
	public Optional<AABB> getValue(Context context) {
		return block()
			.getValue(context.forChild(".block"))
			.flatMap(block -> this.getBlockBounds(context, block));
	}

	@Override
	public void validate(ContextValidator validator) {
		BoxProvider.super.validate(validator);
		block().validate(validator.forChild(".block"));
	}

	private Optional<AABB> getBlockBounds(Context context, CachedBlock block) {
		VoxelShape shape = shapeType().get(block.state(), context.level(), block.pos(), CollisionContext.empty());
		return shape.isEmpty()
			? Optional.empty()
			: Optional.of(shape.bounds().move(block.pos()));
	}

}

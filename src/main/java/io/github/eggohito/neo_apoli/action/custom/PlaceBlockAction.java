package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.direction.DirectionProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.util.CodecUtil;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public record PlaceBlockAction(Vec3Provider position, BlockInput block, Mode mode, Optional<DirectionProvider> offsetDirection) implements Action {

	public static final MapCodec<PlaceBlockAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		Vec3Provider.CODEC.fieldOf("position").forGetter(PlaceBlockAction::position),
		NeoApoliCodecs.REGULAR_OR_STRINGIFIED_BLOCK_INPUT.fieldOf("block").forGetter(PlaceBlockAction::block),
		Mode.CODEC.optionalFieldOf("mode", Mode.DEFAULT).forGetter(PlaceBlockAction::mode),
		DirectionProvider.CODEC.optionalFieldOf("offset_direction").forGetter(PlaceBlockAction::offsetDirection)
	).apply(instance, PlaceBlockAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PlaceBlockAction> STREAM_CODEC = StreamCodec.composite(
		Vec3Provider.STREAM_CODEC, PlaceBlockAction::position,
		NeoApoliStreamCodecs.BLOCK_INPUT, PlaceBlockAction::block,
		Mode.STREAM_CODEC, PlaceBlockAction::mode,
		ByteBufCodecs.optional(DirectionProvider.STREAM_CODEC), PlaceBlockAction::offsetDirection,
		PlaceBlockAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.PLACE_BLOCK;
	}

	@Override
	public void execute(Context context) {

		if (!(context.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		BlockPos position = position().getValue(context.forChild(".position"))
			.map(BlockPos::containing)
			.orElse(null);

		if (position == null) {
			return;
		}

		boolean placeBlock = true;
		boolean updateNeighbors = true;

		switch (mode()) {
			case DESTROY -> {
				serverLevel.destroyBlock(position, true);
				placeBlock = !block().getState().isAir() || !serverLevel.isEmptyBlock(position);
			}
			case KEEP ->
				placeBlock = serverLevel.isEmptyBlock(position);
			case DEFAULT -> {

				Direction offsetDirection = offsetDirection().flatMap(self -> self.getValue(context.forChild(".offset_direction"))).orElse(null);
				placeBlock = serverLevel.isEmptyBlock(position);

				if (!placeBlock && offsetDirection != null) {
					position = position.relative(offsetDirection);
					placeBlock = serverLevel.isEmptyBlock(position);
				}

			}
			case STRICT ->
				updateNeighbors = false;
		}

		if (placeBlock && block().place(serverLevel, position, mode().flag()) && updateNeighbors) {
			serverLevel.updateNeighborsAt(position, block().getState().getBlock());
		}

	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		position().validate(validator.forChild(".position"));
		offsetDirection().ifPresent(offsetDirection -> offsetDirection.validate(validator.forChild(".offset_direction")));
	}

	@Accessors(fluent = true)
	@AllArgsConstructor
	@Getter
	public enum Mode {

		DESTROY(Block.UPDATE_CLIENTS | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS),
		KEEP(Block.UPDATE_CLIENTS | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS),
		REPLACE(Block.UPDATE_CLIENTS | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS),
		DEFAULT(Block.UPDATE_CLIENTS | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS),
		STRICT(Block.UPDATE_CLIENTS | Block.UPDATE_SKIP_ALL_SIDEEFFECTS);

		public static final Codec<Mode> CODEC = CodecUtil.enumType(Mode.class);
		public static final StreamCodec<ByteBuf, Mode> STREAM_CODEC = StreamCodecUtil.enumType(Mode.class);

		final int flag;

	}

}

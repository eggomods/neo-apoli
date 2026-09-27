package io.github.eggohito.neo_apoli.provider.custom.vec3;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.block.BlockProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliVec3ProviderTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record BlockPositionVec3Provider(BlockProvider block) implements Vec3Provider {

	public static final MapCodec<BlockPositionVec3Provider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(BlockProvider.CODEC.fieldOf("block").forGetter(BlockPositionVec3Provider::block))
		.apply(instance, BlockPositionVec3Provider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, BlockPositionVec3Provider> STREAM_CODEC = StreamCodec.composite(
		BlockProvider.STREAM_CODEC, BlockPositionVec3Provider::block,
		BlockPositionVec3Provider::new
	);

	@Override
	public @NotNull Vec3Provider.Type<?> getType() {
		return NeoApoliVec3ProviderTypes.BLOCK_POSITION;
	}

	@Override
	public Optional<Vec3> getValue(Context context) {
		return block().getValue(context.forChild(".block"))
			.map(CachedBlock::pos)
			.map(BlockPos::getCenter);
	}

	@Override
	public void validate(Context.Validator validator) {
		Vec3Provider.super.validate(validator);
		block().validate(validator.forChild(".block"));
	}

}

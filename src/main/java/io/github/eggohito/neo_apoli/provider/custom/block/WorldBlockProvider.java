package io.github.eggohito.neo_apoli.provider.custom.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliBlockProviderTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record WorldBlockProvider(Vec3Provider position) implements BlockProvider {

	public static final MapCodec<WorldBlockProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(Vec3Provider.CODEC.fieldOf("position").forGetter(WorldBlockProvider::position))
		.apply(instance, WorldBlockProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, WorldBlockProvider> STREAM_CODEC = StreamCodec.composite(
		Vec3Provider.STREAM_CODEC, WorldBlockProvider::position,
		WorldBlockProvider::new
	);

	@Override
	public BlockProvider.@NotNull Type<?> getType() {
		return NeoApoliBlockProviderTypes.WORLD;
	}

	@Override
	public Optional<CachedBlock> getValue(Context context) {
		return position().getValue(context.forChild(".position"))
			.map(BlockPos::containing)
			.flatMap(position -> CachedBlock.optionallyFromLoadedPos(context.level(), position));
	}

	@Override
	public void validate(Context.Validator validator) {
		BlockProvider.super.validate(validator);
		position().validate(validator.forChild(".position"));
	}

}

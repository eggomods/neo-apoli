package io.github.eggohito.neo_apoli.provider.custom.box;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliBoxProviderTypes;
import net.minecraft.Util;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record ConstantBoxProvider(Vec3 min, Vec3 max) implements BoxProvider {

	public static final MapCodec<ConstantBoxProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		NeoApoliCodecs.VECTOR_3_DOUBLE.fieldOf("min").forGetter(ConstantBoxProvider::min),
		NeoApoliCodecs.VECTOR_3_DOUBLE.fieldOf("max").forGetter(ConstantBoxProvider::max)
	).apply(instance, ConstantBoxProvider::new));

	public static final Codec<ConstantBoxProvider> INLINE_CODEC = Codec.DOUBLE.listOf().comapFlatMap(
		doubles -> Util.fixedSize(doubles, 6).map(values -> new ConstantBoxProvider(values.getFirst(), values.get(1), values.get(2), values.get(3), values.get(4), values.getLast())),
		constant -> List.of(constant.min().x(), constant.min().y(), constant.min().z(), constant.max().x(), constant.max().y(), constant.max().z())
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, ConstantBoxProvider> STREAM_CODEC = StreamCodec.composite(
		Vec3.STREAM_CODEC, ConstantBoxProvider::min,
		Vec3.STREAM_CODEC, ConstantBoxProvider::max,
		ConstantBoxProvider::new
	);

	public ConstantBoxProvider(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
		this(new Vec3(minX, minY, minZ), new Vec3(maxX, maxY, maxZ));
	}

	@Override
	public @NotNull BoxProvider.Type<?> getType() {
		return NeoApoliBoxProviderTypes.CONSTANT;
	}

	@Override
	public Optional<AABB> getValue(Context context) {
		return Optional.of(new AABB(min(), max()));
	}

}

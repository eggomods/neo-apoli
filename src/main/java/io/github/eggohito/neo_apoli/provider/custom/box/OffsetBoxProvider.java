package io.github.eggohito.neo_apoli.provider.custom.box;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliBoxProviderTypes;
import io.github.eggohito.neo_apoli.util.MapCodecUtil;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record OffsetBoxProvider(BoxProvider box, BoxProvider offset) implements BoxProvider {

	public static final MapCodec<OffsetBoxProvider> MAP_CODEC = MapCodecUtil.lazy(OffsetBoxProvider.class.getSimpleName(), () -> RecordCodecBuilder.mapCodec(instance -> instance.group(
		BoxProvider.CODEC.fieldOf("box").forGetter(OffsetBoxProvider::box),
		BoxProvider.CODEC.fieldOf("offset").forGetter(OffsetBoxProvider::offset)
	).apply(instance, OffsetBoxProvider::new)));

	public static final StreamCodec<RegistryFriendlyByteBuf, OffsetBoxProvider> STREAM_CODEC = StreamCodecUtil.lazy(OffsetBoxProvider.class.getSimpleName(), () -> StreamCodec.composite(
		BoxProvider.STREAM_CODEC, OffsetBoxProvider::box,
		BoxProvider.STREAM_CODEC, OffsetBoxProvider::offset,
		OffsetBoxProvider::new
	));

	@Override
	public @NotNull BoxProvider.Type<?> getType() {
		return NeoApoliBoxProviderTypes.OFFSET;
	}

	@Override
	public Optional<AABB> getValue(Context context) {
		return box().getValue(context.forChild(".box"))
			.flatMap(box -> offset().getValue(context.forChild(".offset"))
				.map(offset -> this.offset(box, offset)));
	}

	@Override
	public void validate(ContextValidator validator) {

		BoxProvider.super.validate(validator);

		box().validate(validator.forChild(".box"));
		offset().validate(validator.forChild(".offset"));

	}

	private AABB offset(AABB box, AABB offset) {
		return new AABB(
			box.minX + offset.minX,
			box.minY + offset.minY,
			box.minZ + offset.minZ,
			box.maxX + offset.maxX,
			box.maxY + offset.maxY,
			box.maxZ + offset.maxZ
		);
	}

}

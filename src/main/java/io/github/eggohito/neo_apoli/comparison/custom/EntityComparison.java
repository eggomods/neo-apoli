package io.github.eggohito.neo_apoli.comparison.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.comparison.Comparison;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliComparisonTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

import java.util.Objects;

public record EntityComparison(EntityProvider first, EntityProvider second) implements Comparison {

	public static final MapCodec<EntityComparison> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityProvider.CODEC.fieldOf("first").forGetter(EntityComparison::first),
		EntityProvider.CODEC.fieldOf("second").forGetter(EntityComparison::second)
	).apply(instance, EntityComparison::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, EntityComparison> STREAM_CODEC = StreamCodec.composite(
		EntityProvider.STREAM_CODEC, EntityComparison::first,
		EntityProvider.STREAM_CODEC, EntityComparison::second,
		EntityComparison::new
	);

	@Override
	public Type<?> type() {
		return NeoApoliComparisonTypes.ENTITY;
	}

	@Override
	public boolean compare(Context context) {

		Entity first = first().getValue(context.forChild(".first")).orElse(null);
		Entity second = second().getValue(context.forChild(".second")).orElse(null);

		return first != null
			&& second != null
			&& Objects.equals(first, second);

	}

	@Override
	public void validate(Context.Validator validator) {
		Comparison.super.validate(validator);
		first().validate(validator.forChild(".first"));
		second().validate(validator.forChild(".second"));
	}

}

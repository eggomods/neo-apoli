package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TraceableEntity;

import java.util.Objects;

public record IsEntityOwnedByOtherCondition(EntityProvider first, EntityProvider second) implements Condition {

	public static final MapCodec<IsEntityOwnedByOtherCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityProvider.CODEC.fieldOf("first").forGetter(IsEntityOwnedByOtherCondition::first),
		EntityProvider.CODEC.fieldOf("second").forGetter(IsEntityOwnedByOtherCondition::second)
	).apply(instance, IsEntityOwnedByOtherCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, IsEntityOwnedByOtherCondition> STREAM_CODEC = StreamCodec.composite(
		EntityProvider.STREAM_CODEC, IsEntityOwnedByOtherCondition::first,
		EntityProvider.STREAM_CODEC, IsEntityOwnedByOtherCondition::second,
		IsEntityOwnedByOtherCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.IS_ENTITY_OWNED_BY_OTHER;
	}

	@Override
	public boolean test(Context context) {

		Entity first = first().getValue(context.forChild(".first")).orElse(null);
		Entity second = second().getValue(context.forChild(".second")).orElse(null);

		return second != null
			&& this.isOwnedBy(first, second);

	}

	@Override
	public void validate(Context.Validator validator) {
		Condition.super.validate(validator);
		first().validate(validator.forChild(".first"));
		second().validate(validator.forChild(".second"));
	}

	private boolean isOwnedBy(Entity first, Entity second) {
		return (first instanceof OwnableEntity ownable && Objects.equals(second, ownable.getOwner()))
			|| (first instanceof TraceableEntity traceableEntity && Objects.equals(second, traceableEntity.getOwner()));
	}

}

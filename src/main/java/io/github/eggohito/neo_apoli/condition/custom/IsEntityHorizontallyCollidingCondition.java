package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record IsEntityHorizontallyCollidingCondition(EntityProvider entity) implements Condition {

	public static final MapCodec<IsEntityHorizontallyCollidingCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(EntityProvider.CODEC.fieldOf("entity").forGetter(IsEntityHorizontallyCollidingCondition::entity))
		.apply(instance, IsEntityHorizontallyCollidingCondition::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, IsEntityHorizontallyCollidingCondition> STREAM_CODEC = StreamCodec.composite(
		EntityProvider.STREAM_CODEC, IsEntityHorizontallyCollidingCondition::entity,
		IsEntityHorizontallyCollidingCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.IS_ENTITY_HORIZONTALLY_COLLIDING;
	}

	@Override
	public boolean test(Context context) {
		return entity().getValue(context.forChild(".entity"))
			.stream()
			.anyMatch(entity -> entity.horizontalCollision);
	}

	@Override
	public void validate(Context.Validator validator) {
		Condition.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

}

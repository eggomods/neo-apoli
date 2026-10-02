package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.context.parameter.EntityContextParameter;
import io.github.eggohito.neo_apoli.provider.custom.entity.ContextEntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

public record IsEntitySneakingCondition(EntityProvider entity) implements Condition {

	public static final MapCodec<IsEntitySneakingCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(EntityProvider.CODEC.fieldOf("entity").forGetter(IsEntitySneakingCondition::entity))
		.apply(instance, IsEntitySneakingCondition::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, IsEntitySneakingCondition> STREAM_CODEC = StreamCodec.composite(
		EntityProvider.STREAM_CODEC, IsEntitySneakingCondition::entity,
		IsEntitySneakingCondition::new
	);

	public IsEntitySneakingCondition(EntityContextParameter entity) {
		this(new ContextEntityProvider(entity));
	}

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.IS_ENTITY_SNEAKING;
	}

	@Override
	public boolean test(Context context) {

		try {
			return entity().getValue(context.forChild(".entity"))
				.stream()
				.filter(entity -> context.visitor().push(this))
				.anyMatch(Entity::isShiftKeyDown);
		}

		finally {
			context.visitor().pop(this);
		}

	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

}

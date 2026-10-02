package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextUser;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.context.visitor.Visitor;
import io.github.eggohito.neo_apoli.power.PowerIdentifier;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;

public record EntityHasActivePowerCondition(PowerIdentifier power, EntityProvider entity) implements Condition {

	public static final MapCodec<EntityHasActivePowerCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		PowerIdentifier.CODEC.fieldOf("power").forGetter(EntityHasActivePowerCondition::power),
		EntityProvider.CODEC.fieldOf("entity").forGetter(EntityHasActivePowerCondition::entity)
	).apply(instance, EntityHasActivePowerCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, EntityHasActivePowerCondition> STREAM_CODEC = StreamCodec.composite(
		PowerIdentifier.STREAM_CODEC, EntityHasActivePowerCondition::power,
		EntityProvider.STREAM_CODEC, EntityHasActivePowerCondition::entity,
		EntityHasActivePowerCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.ENTITY_HAS_ACTIVE_POWER;
	}

	@Override
	public boolean test(Context context) {

		Visitor<ContextUser> visitor = context.visitor();
		Entity entity = entity().getValue(context.forChild(".entity")).orElse(null);

		try {

			if (visitor.push(this)) {
				Powers powers = Powers.getNullable(entity);
				return powers != null
					&& powers.hasInstance(this.power())
					&& powers.getInstance(this.power()).isActive(context.forChild(".power"));
			}

			else {
				return false;
			}

		}

		finally {
			visitor.pop(this);
		}

	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		power().validate(validator.forChild(".power"));
		entity().validate(validator.forChild(".entity"));
	}

}

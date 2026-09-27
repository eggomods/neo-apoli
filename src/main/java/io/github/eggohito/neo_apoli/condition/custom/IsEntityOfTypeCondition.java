package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public record IsEntityOfTypeCondition(EntityType<?> entityType, EntityProvider entity) implements Condition {

	public static final MapCodec<IsEntityOfTypeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityType.CODEC.fieldOf("entity_type").forGetter(IsEntityOfTypeCondition::entityType),
		EntityProvider.CODEC.fieldOf("entity").forGetter(IsEntityOfTypeCondition::entity)
	).apply(instance, IsEntityOfTypeCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, IsEntityOfTypeCondition> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.registry(Registries.ENTITY_TYPE), IsEntityOfTypeCondition::entityType,
		EntityProvider.STREAM_CODEC, IsEntityOfTypeCondition::entity,
		IsEntityOfTypeCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.IS_ENTITY_OF_TYPE;
	}

	@Override
	public boolean test(Context context) {
		return entity().getValue(context.forChild(".entity"))
			.stream()
			.map(Entity::getType)
			.anyMatch(entityType()::equals);
	}

	@Override
	public void validate(Context.Validator validator) {
		Condition.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

}

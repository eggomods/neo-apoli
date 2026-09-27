package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.power.PowerIdentifier;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record EntityHasPowerCondition(PowerIdentifier power, Optional<ResourceLocation> source, EntityProvider entity) implements Condition {

	public static final MapCodec<EntityHasPowerCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		PowerIdentifier.CODEC.fieldOf("power").forGetter(EntityHasPowerCondition::power),
		ResourceLocation.CODEC.optionalFieldOf("source").forGetter(EntityHasPowerCondition::source),
		EntityProvider.CODEC.fieldOf("entity").forGetter(EntityHasPowerCondition::entity)
	).apply(instance, EntityHasPowerCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, EntityHasPowerCondition> STREAM_CODEC = StreamCodec.composite(
		PowerIdentifier.STREAM_CODEC, EntityHasPowerCondition::power,
		ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), EntityHasPowerCondition::source,
		EntityProvider.STREAM_CODEC, EntityHasPowerCondition::entity,
		EntityHasPowerCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.ENTITY_HAS_POWER;
	}

	@Override
	public boolean test(Context context) {

		Powers powers = entity().getValue(context.forChild(".entity"))
			.flatMap(Powers::getOptional)
			.orElse(null);

		return powers != null
			&& this.testInternal(powers);

	}

	@Override
	public void validate(Context.Validator validator) {
		Condition.super.validate(validator);
		power().validate(validator.forChild(".power"));
		entity().validate(validator.forChild(".entity"));
	}

	private boolean testInternal(Powers powers) {
		return this.source()
			.map(self -> powers.hasInstance(power(), self))
			.orElseGet(() -> powers.hasInstance(power()));
	}

}

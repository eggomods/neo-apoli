package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.power.PowerIdentifier;
import io.github.eggohito.neo_apoli.power.custom.misc.CooldownPower;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record PowerCooldownProgressFloatProvider(PowerIdentifier power, EntityProvider entity) implements FloatProvider {

	public static final MapCodec<PowerCooldownProgressFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		PowerIdentifier.CODEC.fieldOf("power").forGetter(PowerCooldownProgressFloatProvider::power),
		EntityProvider.CODEC.fieldOf("entity").forGetter(PowerCooldownProgressFloatProvider::entity)
	).apply(instance, PowerCooldownProgressFloatProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PowerCooldownProgressFloatProvider> STREAM_CODEC = StreamCodec.composite(
		PowerIdentifier.STREAM_CODEC, PowerCooldownProgressFloatProvider::power,
		EntityProvider.STREAM_CODEC, PowerCooldownProgressFloatProvider::entity,
		PowerCooldownProgressFloatProvider::new
	);

	@Override
	public @NotNull FloatProvider.Type<?> getType() {
		return NeoApoliFloatProviderTypes.POWER_COOLDOWN_PROGRESS;
	}

	@Override
	public Optional<Float> getValue(Context context) {

		Context entityContext = context.forChild(".entity");
		Entity entity = entity().getValue(entityContext).orElse(null);

		CooldownPower.Instance<?> cooldownInstance = Powers.getOptional(entity)
			.flatMap(powers -> powers.getOptionalInstance(power()))
			.filter(CooldownPower.Instance.class::isInstance)
			.map(CooldownPower.Instance.class::cast)
			.orElse(null);

		if (entity == null || cooldownInstance == null) {

			if (entity == null) {
				entityContext.reportProblem("Entity doesn't exist!");
			}

			if (cooldownInstance == null) {
				context.reportProblem(power().asDisplayString() + " does not have a cooldown!");
			}

			return Optional.empty();

		}

		else {
			return Optional.of((float) cooldownInstance.getProgress(context.forChild(".power")));
		}

	}

	@Override
	public void validate(ContextValidator validator) {
		FloatProvider.super.validate(validator);
		power().validate(validator.forChild(".power"), CooldownPower.class, () -> power.asDisplayString() + " doesn't have a cooldown!");
		entity().validate(validator.forChild(".entity"));
	}

}

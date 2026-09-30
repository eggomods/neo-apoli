package io.github.eggohito.neo_apoli.provider.custom.vec3;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.floats.ConstantFloatProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliVec3ProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record EntityViewVec3Provider(EntityProvider entity, FloatProvider delta) implements Vec3Provider {

	public static final MapCodec<EntityViewVec3Provider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityProvider.CODEC.fieldOf("entity").forGetter(EntityViewVec3Provider::entity),
		FloatProvider.CODEC.optionalFieldOf("delta", new ConstantFloatProvider(1.0F)).forGetter(EntityViewVec3Provider::delta)
	).apply(instance, EntityViewVec3Provider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, EntityViewVec3Provider> STREAM_CODEC = StreamCodec.composite(
		EntityProvider.STREAM_CODEC, EntityViewVec3Provider::entity,
		FloatProvider.STREAM_CODEC, EntityViewVec3Provider::delta,
		EntityViewVec3Provider::new
	);

	@Override
	public @NotNull Vec3Provider.Type<?> getType() {
		return NeoApoliVec3ProviderTypes.ENTITY_VIEW;
	}

	@Override
	public Optional<Vec3> getValue(Context context) {
		return entity()
			.getValue(context.forChild(".entity"))
			.map(entity -> entity.getViewVector(delta().getFloat(context.forChild(".delta"))));
	}

	@Override
	public void validate(ContextValidator validator) {
		Vec3Provider.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
		delta().validate(validator.forChild(".delta"));
	}

}

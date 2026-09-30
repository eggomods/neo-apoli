package io.github.eggohito.neo_apoli.provider.custom.string;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliStringProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record EntityUuidStringProvider(EntityProvider entity) implements StringProvider {

	public static final MapCodec<EntityUuidStringProvider> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(EntityProvider.CODEC.fieldOf("entity").forGetter(EntityUuidStringProvider::entity))
		.apply(instance, EntityUuidStringProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, EntityUuidStringProvider> STREAM_CODEC = StreamCodec.composite(
		EntityProvider.STREAM_CODEC, EntityUuidStringProvider::entity,
		EntityUuidStringProvider::new
	);

	@Override
	public @NotNull StringProvider.Type<?> getType() {
		return NeoApoliStringProviderTypes.ENTITY_UUID;
	}

	@Override
	public Optional<String> getValue(Context context) {
		return entity()
			.getValue(context.forChild(".entity"))
			.map(Entity::getStringUUID);
	}

	@Override
	public void validate(ContextValidator validator) {
		StringProvider.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

}

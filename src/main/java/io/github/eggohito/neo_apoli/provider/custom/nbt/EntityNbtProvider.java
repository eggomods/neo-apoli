package io.github.eggohito.neo_apoli.provider.custom.nbt;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliNbtProviderTypes;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record EntityNbtProvider(EntityProvider entity) implements NbtProvider {

	public static final MapCodec<EntityNbtProvider> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		EntityProvider.CODEC.fieldOf("entity").forGetter(EntityNbtProvider::entity)
	).apply(instance, EntityNbtProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, EntityNbtProvider> STREAM_CODEC = StreamCodec.composite(
		EntityProvider.STREAM_CODEC, EntityNbtProvider::entity,
		EntityNbtProvider::new
	);

	@Override
	public @NotNull NbtProvider.Type<?> getType() {
		return NeoApoliNbtProviderTypes.ENTITY;
	}

	@Override
	public Optional<Tag> getValue(Context context) {
		return entity()
			.getValue(context.forChild(".entity"))
			.map(NbtPredicate::getEntityTagToCompare);
	}

	@Override
	public void validate(Context.Validator validator) {
		NbtProvider.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

}

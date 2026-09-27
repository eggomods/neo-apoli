package io.github.eggohito.neo_apoli.provider.custom.number.floats;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliFloatProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record PlayerSaturationFloatProvider(EntityProvider entity) implements FloatProvider {

	public static final MapCodec<PlayerSaturationFloatProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(EntityProvider.CODEC.fieldOf("entity").forGetter(PlayerSaturationFloatProvider::entity))
		.apply(instance, PlayerSaturationFloatProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, PlayerSaturationFloatProvider> STREAM_CODEC = StreamCodec.composite(
		EntityProvider.STREAM_CODEC, PlayerSaturationFloatProvider::entity,
		PlayerSaturationFloatProvider::new
	);

	@Override
	public @NotNull Type<?> getType() {
		return NeoApoliFloatProviderTypes.PLAYER_SATURATION;
	}

	@Override
	public Optional<Float> getValue(Context context) {
		return entity().getValue(context.forChild(".entity"))
			.filter(Player.class::isInstance)
			.map(Player.class::cast)
			.map(player -> player.getFoodData().getSaturationLevel());
	}

	@Override
	public void validate(Context.Validator validator) {
		FloatProvider.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

}

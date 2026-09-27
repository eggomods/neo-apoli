package io.github.eggohito.neo_apoli.provider.custom.number.ints;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliIntProviderTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record PlayerFoodIntProvider(EntityProvider entity) implements IntProvider {

	public static final MapCodec<PlayerFoodIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(EntityProvider.CODEC.fieldOf("entity").forGetter(PlayerFoodIntProvider::entity))
		.apply(instance, PlayerFoodIntProvider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, PlayerFoodIntProvider> STREAM_CODEC = StreamCodec.composite(
		EntityProvider.STREAM_CODEC, PlayerFoodIntProvider::entity,
		PlayerFoodIntProvider::new
	);

	@Override
	public @NotNull IntProvider.Type<?> getType() {
		return NeoApoliIntProviderTypes.PLAYER_FOOD;
	}

	@Override
	public Optional<Integer> getValue(Context context) {
		return entity().getValue(context.forChild(".entity"))
			.filter(Player.class::isInstance)
			.map(Player.class::cast)
			.map(player -> player.getFoodData().getFoodLevel());
	}

	@Override
	public void validate(Context.Validator validator) {
		IntProvider.super.validate(validator);
		entity().validate(validator.forChild(".entity"));
	}

}

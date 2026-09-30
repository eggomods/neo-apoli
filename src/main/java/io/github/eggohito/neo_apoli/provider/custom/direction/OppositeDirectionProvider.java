package io.github.eggohito.neo_apoli.provider.custom.direction;

import com.mojang.serialization.MapCodec;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliDirectionProviderTypes;
import io.github.eggohito.neo_apoli.util.MapCodecUtil;
import io.github.eggohito.neo_apoli.util.StreamCodecUtil;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record OppositeDirectionProvider(DirectionProvider direction) implements DirectionProvider {

	public static final MapCodec<OppositeDirectionProvider> CODEC = MapCodecUtil.lazy(OppositeDirectionProvider.class.getSimpleName(), () -> DirectionProvider.CODEC.fieldOf("direction").xmap(OppositeDirectionProvider::new, OppositeDirectionProvider::direction));
	public static final StreamCodec<RegistryFriendlyByteBuf, OppositeDirectionProvider> STREAM_CODEC = StreamCodecUtil.lazy(OppositeDirectionProvider.class.getSimpleName(), () -> DirectionProvider.STREAM_CODEC.map(OppositeDirectionProvider::new,  OppositeDirectionProvider::direction));

	@Override
	public @NotNull Type<?> getType() {
		return NeoApoliDirectionProviderTypes.OPPOSITE;
	}

	@Override
	public Optional<Direction> getValue(Context context) {
		return direction().getValue(context.forChild(".direction")).map(Direction::getOpposite);
	}

	@Override
	public void validate(ContextValidator validator) {
		DirectionProvider.super.validate(validator);
		direction().validate(validator.forChild(".direction"));
	}

}

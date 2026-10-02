package io.github.eggohito.neo_apoli.provider.custom.vec3;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.direction.DirectionProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliVec3ProviderTypes;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record DirectionVec3Provider(DirectionProvider direction) implements Vec3Provider {

	public static final MapCodec<DirectionVec3Provider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
		.group(DirectionProvider.CODEC.fieldOf("direction").forGetter(DirectionVec3Provider::direction))
		.apply(instance, DirectionVec3Provider::new)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, DirectionVec3Provider> STREAM_CODEC = StreamCodec.composite(
		DirectionProvider.STREAM_CODEC, DirectionVec3Provider::direction,
		DirectionVec3Provider::new
	);

	@Override
	public @NotNull Vec3Provider.Type<?> getType() {
		return NeoApoliVec3ProviderTypes.DIRECTION;
	}

	@Override
	public Optional<Vec3> getValue(Context context) {
		return direction()
			.getValue(context.forChild(".direction"))
			.map(Direction::getUnitVec3);
	}

	@Override
	public void validate(ContextValidator validator) {
		Vec3Provider.super.validate(validator);
		direction().validate(validator.forChild(".direction"));
	}

}

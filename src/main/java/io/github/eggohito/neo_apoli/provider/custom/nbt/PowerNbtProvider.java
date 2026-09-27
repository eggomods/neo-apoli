package io.github.eggohito.neo_apoli.provider.custom.nbt;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.power.PowerIdentifier;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliNbtProviderTypes;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record PowerNbtProvider(PowerIdentifier power, EntityProvider entity) implements NbtProvider {

	public static final MapCodec<PowerNbtProvider> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		PowerIdentifier.CODEC.fieldOf("power").forGetter(PowerNbtProvider::power),
		EntityProvider.CODEC.fieldOf("entity").forGetter(PowerNbtProvider::entity)
	).apply(instance, PowerNbtProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PowerNbtProvider> STREAM_CODEC = StreamCodec.composite(
		PowerIdentifier.STREAM_CODEC, PowerNbtProvider::power,
		EntityProvider.STREAM_CODEC, PowerNbtProvider::entity,
		PowerNbtProvider::new
	);

	@Override
	public @NotNull NbtProvider.Type<?> getType() {
		return NeoApoliNbtProviderTypes.POWER;
	}

	@Override
	public Optional<Tag> getValue(Context context) {
		return entity().getValue(context.forChild(".entity"))
			.flatMap(Powers::getOptional)
			.flatMap(powers -> this.getAndCreate(context, powers));
	}

	@Override
	public void validate(Context.Validator validator) {
		NbtProvider.super.validate(validator);
		power().validate(validator.forChild(".power"));
		entity().validate(validator.forChild(".entity"));
	}

	private Optional<Tag> getAndCreate(Context context, Powers powers) {
		RegistryOps<Tag> ops = context.level().registryAccess().createSerializationContext(NbtOps.INSTANCE);
		return powers.getOptionalInstance(this.power())
			.flatMap(instance -> instance.encodeData(ops)
				.resultOrPartial(err -> context.reportProblem("Error while encoding and providing data of " + power().asDisplayString(false) + ": " + err)));
	}

}

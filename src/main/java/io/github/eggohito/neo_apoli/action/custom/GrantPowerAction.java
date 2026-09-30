package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.command.argument.PowerArgument;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.power.entity.MutablePowers;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.util.ParsedArgument;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public record GrantPowerAction(ParsedArgument<PowerArgument.Result> power, ResourceLocation source, EntityProvider entity) implements Action {

	public static final MapCodec<GrantPowerAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		NeoApoliCodecs.POWER_OR_TAG_ARGUMENT.fieldOf("power").forGetter(GrantPowerAction::power),
		ResourceLocation.CODEC.fieldOf("source").forGetter(GrantPowerAction::source),
		EntityProvider.CODEC.fieldOf("entity").forGetter(GrantPowerAction::entity)
	).apply(instance, GrantPowerAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, GrantPowerAction> STREAM_CODEC = StreamCodec.composite(
		NeoApoliStreamCodecs.POWER_OR_TAG_ARGUMENT, GrantPowerAction::power,
		ResourceLocation.STREAM_CODEC, GrantPowerAction::source,
		EntityProvider.STREAM_CODEC, GrantPowerAction::entity,
		GrantPowerAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.GRANT_POWER;
	}

	@Override
	public void execute(Context context) {
		entity().getValue(context.forChild(".entity"))
			.map(MutablePowers::create)
			.ifPresent(mutable -> this.grant(mutable, context::reportProblem));
	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		power().argument().validate(validator.forChild(".power"));
		entity().validate(validator.forChild(".entity"));
	}

	private void grant(MutablePowers mutable, Consumer<String> errorHandler) {

		try (mutable) {

			for (var holder : power().argument().get()) {
				mutable.grant(holder.id(), source());
			}

		}

		catch (CommandSyntaxException e) {
			errorHandler.accept(e.getMessage());
		}

	}

}

package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.command.argument.PowerArgument;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.power.entity.MutablePowers;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.util.ParsedArgument;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Consumer;

public record RemovePowerAction(ParsedArgument<PowerArgument.Result> power, EntityProvider entity) implements Action {

	public static final MapCodec<RemovePowerAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		NeoApoliCodecs.POWER_OR_TAG_ARGUMENT.fieldOf("power").forGetter(RemovePowerAction::power),
		EntityProvider.CODEC.fieldOf("entity").forGetter(RemovePowerAction::entity)
	).apply(instance, RemovePowerAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, RemovePowerAction> STREAM_CODEC = StreamCodec.composite(
		NeoApoliStreamCodecs.POWER_OR_TAG_ARGUMENT, RemovePowerAction::power,
		EntityProvider.STREAM_CODEC, RemovePowerAction::entity,
		RemovePowerAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.REMOVE_POWER;
	}

	@Override
	public void execute(Context context) {
		entity().getValue(context.forChild(".entity"))
			.flatMap(MutablePowers::getOptional)
			.ifPresent(mutable -> this.remove(mutable, context::reportProblem));
	}

	@Override
	public void validate(Context.Validator validator) {
		Action.super.validate(validator);
		power().argument().validate(validator.forChild(".power"));
		entity().validate(validator.forChild(".entity"));
	}

	private void remove(MutablePowers mutable, Consumer<String> errorHandler) {

		try (mutable) {

			for (var holder : power().argument().get()) {

				for (var source : mutable.getSources(holder.id())) {
					mutable.revoke(holder.id(), source);
				}

			}

		}

		catch (CommandSyntaxException e) {
			errorHandler.accept(e.getMessage());
		}

	}

}

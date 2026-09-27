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
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public record RevokePowerAction(ParsedArgument<PowerArgument.Result> power, ResourceLocation source, EntityProvider entity) implements Action {

	public static final MapCodec<RevokePowerAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		NeoApoliCodecs.POWER_OR_TAG_ARGUMENT.fieldOf("power").forGetter(RevokePowerAction::power),
		ResourceLocation.CODEC.fieldOf("source").forGetter(RevokePowerAction::source),
		EntityProvider.CODEC.fieldOf("entity").forGetter(RevokePowerAction::entity)
	).apply(instance, RevokePowerAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, RevokePowerAction> STREAM_CODEC = StreamCodec.composite(
		NeoApoliStreamCodecs.POWER_OR_TAG_ARGUMENT, RevokePowerAction::power,
		ResourceLocation.STREAM_CODEC, RevokePowerAction::source,
		EntityProvider.STREAM_CODEC, RevokePowerAction::entity,
		RevokePowerAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.REVOKE_POWER;
	}

	@Override
	public void execute(Context context) {
		entity().getValue(context.forChild(".entity"))
			.flatMap(MutablePowers::getOptional)
			.ifPresent(mutable -> this.revoke(mutable, context::reportProblem));
	}

	@Override
	public void validate(Context.Validator validator) {
		Action.super.validate(validator);
		power().argument().validate(validator.forChild(".power"));
		entity().validate(validator.forChild(".entity"));
	}

	private void revoke(MutablePowers mutable, Consumer<String> errorHandler) {

		try (mutable) {

			for (var holder : power.argument().get()) {
				mutable.revoke(holder.id(), source());
			}

		}

		catch (CommandSyntaxException e) {
			errorHandler.accept(e.getMessage());
		}

	}

}

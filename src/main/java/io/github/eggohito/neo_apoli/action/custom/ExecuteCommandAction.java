package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.command_source.CommandSourceProvider;
import io.github.eggohito.neo_apoli.provider.custom.string.StringProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record ExecuteCommandAction(CommandSourceProvider source, StringProvider command) implements Action {

	public static final MapCodec<ExecuteCommandAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		CommandSourceProvider.CODEC.fieldOf("source").forGetter(ExecuteCommandAction::source),
		StringProvider.CODEC.fieldOf("command").forGetter(ExecuteCommandAction::command)
	).apply(instance, ExecuteCommandAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ExecuteCommandAction> STREAM_CODEC = StreamCodec.composite(
		CommandSourceProvider.STREAM_CODEC, ExecuteCommandAction::source,
		StringProvider.STREAM_CODEC, ExecuteCommandAction::command,
		ExecuteCommandAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.EXECUTE_COMMAND;
	}

	@Override
	public void execute(Context context) {
		source().getValue(context.forChild(".source"))
			.ifPresent(source -> command().getValue(context.forChild(".command"))
				.ifPresent(command -> source.getServer().getCommands().performPrefixedCommand(source, command)));
	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		source().validate(validator.forChild(".source"));
		command().validate(validator.forChild(".command"));
	}

}

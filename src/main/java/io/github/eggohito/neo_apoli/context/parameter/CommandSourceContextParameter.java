package io.github.eggohito.neo_apoli.context.parameter;

import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;

public record CommandSourceContextParameter(ResourceLocation name) implements ContextParameter<CommandSourceStack> {

	@Override
	public Type<?> getType() {
		return NeoApoliContextParameterTypes.COMMAND_SOURCE;
	}

}

package io.github.eggohito.neo_apoli.context.parameter;

import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import net.minecraft.resources.ResourceLocation;

public record StringContextParameter(ResourceLocation name) implements ContextParameter<String> {

	@Override
	public Type<?> getType() {
		return NeoApoliContextParameterTypes.STRING;
	}

}

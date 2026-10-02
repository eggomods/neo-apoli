package io.github.eggohito.neo_apoli.context.parameter;

import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import net.minecraft.resources.ResourceLocation;

public record BooleanContextParameter(ResourceLocation name) implements ContextParameter<Boolean> {

	@Override
	public Type<?> getType() {
		return NeoApoliContextParameterTypes.BOOLEAN;
	}

}

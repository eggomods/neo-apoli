package io.github.eggohito.neo_apoli.context.parameter;

import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import net.minecraft.resources.ResourceLocation;

public record IntContextParameter(ResourceLocation name) implements ContextParameter<Integer> {

	@Override
	public Type<?> getType() {
		return NeoApoliContextParameterTypes.INT;
	}

}

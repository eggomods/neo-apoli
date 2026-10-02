package io.github.eggohito.neo_apoli.context.parameter;

import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import net.minecraft.resources.ResourceLocation;

public record FloatContextParameter(ResourceLocation name) implements ContextParameter<Float> {

	@Override
	public Type<?> getType() {
		return NeoApoliContextParameterTypes.FLOAT;
	}

}

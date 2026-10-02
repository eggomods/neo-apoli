package io.github.eggohito.neo_apoli.context.parameter;

import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import io.github.eggohito.neo_apoli.util.CachedBlock;
import net.minecraft.resources.ResourceLocation;

public record BlockContextParameter(ResourceLocation name) implements ContextParameter<CachedBlock> {

	@Override
	public Type<?> getType() {
		return NeoApoliContextParameterTypes.BLOCK;
	}

}

package io.github.eggohito.neo_apoli.context.parameter;

import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public record DirectionContextParameter(ResourceLocation name) implements ContextParameter<Direction> {

	@Override
	public Type<?> getType() {
		return NeoApoliContextParameterTypes.DIRECTION;
	}

}

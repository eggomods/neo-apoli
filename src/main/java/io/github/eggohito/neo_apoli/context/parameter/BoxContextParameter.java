package io.github.eggohito.neo_apoli.context.parameter;

import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

public record BoxContextParameter(ResourceLocation name) implements ContextParameter<AABB> {

	@Override
	public Type<?> getType() {
		return NeoApoliContextParameterTypes.BOX;
	}

}

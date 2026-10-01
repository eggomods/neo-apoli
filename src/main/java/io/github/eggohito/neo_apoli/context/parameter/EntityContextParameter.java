package io.github.eggohito.neo_apoli.context.parameter;

import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public record EntityContextParameter(ResourceLocation name) implements ContextParameter<Entity> {

	@Override
	public Type<?> getType() {
		return NeoApoliContextParameterTypes.ENTITY;
	}

}

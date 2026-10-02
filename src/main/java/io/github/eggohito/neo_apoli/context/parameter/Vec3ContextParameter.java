package io.github.eggohito.neo_apoli.context.parameter;

import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public record Vec3ContextParameter(ResourceLocation name) implements ContextParameter<Vec3> {

	@Override
	public Type<?> getType() {
		return NeoApoliContextParameterTypes.VEC3;
	}

}

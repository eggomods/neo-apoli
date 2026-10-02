package io.github.eggohito.neo_apoli.context.parameter;

import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SlotAccess;

public record SlotContextParameter(ResourceLocation name) implements ContextParameter<SlotAccess> {

	@Override
	public Type<?> getType() {
		return NeoApoliContextParameterTypes.SLOT;
	}

}

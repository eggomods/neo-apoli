package io.github.eggohito.neo_apoli.context.parameter;

import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public record ItemContextParameter(ResourceLocation name) implements ContextParameter<ItemStack> {

	@Override
	public Type<?> getType() {
		return NeoApoliContextParameterTypes.ITEM;
	}

}

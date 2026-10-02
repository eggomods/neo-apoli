package io.github.eggohito.neo_apoli.context.parameter;

import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public record NbtContextParameter(ResourceLocation name) implements ContextParameter<Tag> {

	@Override
	public Type<?> getType() {
		return NeoApoliContextParameterTypes.NBT;
	}

}

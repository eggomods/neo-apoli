package io.github.eggohito.neo_apoli.context.parameter;

import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;

public record DamageSourceContextParameter(ResourceLocation name) implements ContextParameter<DamageSource> {

	@Override
	public Type<?> getType() {
		return NeoApoliContextParameterTypes.DAMAGE_SOURCE;
	}

}

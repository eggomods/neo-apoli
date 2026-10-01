package io.github.eggohito.neo_apoli.context.parameter;

import io.github.eggohito.neo_apoli.registry.context.NeoApoliContextParameterTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

public record EffectContextParameter(ResourceLocation name) implements ContextParameter<MobEffectInstance> {

	@Override
	public Type<?> getType() {
		return NeoApoliContextParameterTypes.EFFECT;
	}

}

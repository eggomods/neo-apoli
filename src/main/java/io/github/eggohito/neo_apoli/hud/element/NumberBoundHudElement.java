package io.github.eggohito.neo_apoli.hud.element;

import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.context.parameter.IntContextParameter;
import io.github.eggohito.neo_apoli.provider.custom.number.IntProvider;

import java.util.Optional;

public interface NumberBoundHudElement extends HudElement {

	IntContextParameter CURRENT_VALUE = new IntContextParameter(NeoApoli.id("hud/value"));

	IntContextParameter MAX_VALUE = new IntContextParameter(NeoApoli.id("hud/max_value"));

	IntContextParameter MIN_VALUE = new IntContextParameter(NeoApoli.id("hud/min_value"));

	Optional<IntProvider> value();

	Optional<IntProvider> min();

	Optional<IntProvider> max();

	@Override
	default void validate(ContextValidator validator) {

		HudElement.super.validate(validator);

		validateKeyAndField(validator, CURRENT_VALUE, value(), "value");
		validateKeyAndField(validator, MAX_VALUE, max(), "max");
		validateKeyAndField(validator, MIN_VALUE, min(), "min");

	}

	static void validateKeyAndField(ContextValidator validator, ContextParameter<?> key, Optional<IntProvider> fieldMethod, String fieldName) {

		boolean keyIsAllowed = validator.parameters().allowed().contains(key);
		boolean fieldIsPresent = fieldMethod.isPresent();

		if (keyIsAllowed == fieldIsPresent) {
			validator.reportProblem("Either the parameter \"" + key.name() + "\" must be provided or the field \"" + fieldName + "\" be defined" + (fieldIsPresent ? ", not both" : "") + "!");
		}

	}

}

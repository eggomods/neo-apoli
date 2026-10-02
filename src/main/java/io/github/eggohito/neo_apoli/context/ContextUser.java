package io.github.eggohito.neo_apoli.context;

import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;

import java.util.Set;

public interface ContextUser extends ContextValidatable {

	default Set<ContextParameter<?>> getRequiredParameters() {
		return Set.of();
	}

	@Override
	default void validate(ContextValidator validator) {
		validator.validate(this);
	}

}

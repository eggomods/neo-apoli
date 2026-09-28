package io.github.eggohito.neo_apoli.context;

import java.util.Set;

public interface ContextUser extends ContextValidatable {

	default Set<Context.Parameter<?>> getRequiredParameters() {
		return Set.of();
	}

	@Override
	default void validate(Context.Validator validator) {
		validator.validate(this);
	}

}

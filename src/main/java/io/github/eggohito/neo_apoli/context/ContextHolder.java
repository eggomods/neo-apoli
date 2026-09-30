package io.github.eggohito.neo_apoli.context;

import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

public interface ContextHolder {

	Set<ContextParameter<?>> parameters();

	@Nullable
	<T> T getNullable(ContextParameter<T> parameter);

	default <T> T getRequired(ContextParameter<T> parameter) {

		T object = this.getNullable(parameter);
		if (object == null) {
			throw new NoSuchElementException(parameter.name().toString());
		}

		return object;

	}

	default <T> Optional<T> getOptional(ContextParameter<T> parameter) {
		return Optional.ofNullable(this.getNullable(parameter));
	}

	default boolean hasParameter(ContextParameter<?> parameter) {
		return this.getNullable(parameter) != null;
	}

	default boolean hasAllParameters(Collection<ContextParameter<?>> parameters) {
		return hasAllParameters(parameters.toArray(ContextParameter[]::new));
	}

	default boolean hasAllParameters(ContextParameter<?>... parameters) {

		for (var parameter : parameters) {

			if (!this.hasParameter(parameter)) {
				return false;
			}

		}

		return true;

	}

	default boolean hasAnyParameters(Collection<ContextParameter<?>> parameters) {
		return hasAnyParameters(parameters.toArray(ContextParameter[]::new));
	}

	default boolean hasAnyParameters(ContextParameter<?>... parameters) {

		for (var parameter : parameters) {

			if (hasParameter(parameter)) {
				return true;
			}

		}

		return false;

	}

}

package io.github.eggohito.neo_apoli.context;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

public interface ContextHolder {

	Set<Context.Parameter<?>> parameters();

	@Nullable
	<T> T getNullable(Context.Parameter<T> parameter);

	default <T> T getRequired(Context.Parameter<T> parameter) {

		T object = this.getNullable(parameter);
		if (object == null) {
			throw new NoSuchElementException(parameter.name().toString());
		}

		return object;

	}

	default <T> Optional<T> getOptional(Context.Parameter<T> parameter) {
		return Optional.ofNullable(this.getNullable(parameter));
	}

	default boolean hasParameter(Context.Parameter<?> parameter) {
		return this.getNullable(parameter) != null;
	}

	default boolean hasAllParameters(Collection<Context.Parameter<?>> parameters) {
		return hasAllParameters(parameters.toArray(Context.Parameter[]::new));
	}

	default boolean hasAllParameters(Context.Parameter<?>... parameters) {

		for (var parameter : parameters) {

			if (!this.hasParameter(parameter)) {
				return false;
			}

		}

		return true;

	}

	default boolean hasAnyParameters(Collection<Context.Parameter<?>> parameters) {
		return hasAnyParameters(parameters.toArray(Context.Parameter[]::new));
	}

	default boolean hasAnyParameters(Context.Parameter<?>... parameters) {

		for (var parameter : parameters) {

			if (hasParameter(parameter)) {
				return true;
			}

		}

		return false;

	}

}

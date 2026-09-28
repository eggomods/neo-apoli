package io.github.eggohito.neo_apoli.context;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.Set;

@Accessors(fluent = true)
@Getter
public final class ContextParams {

	public static final ContextParams INTENTIONALLY_EMPTY = new ContextParams.Builder().build();

	private final Set<Context.Parameter<?>> required;
	private final Set<Context.Parameter<?>> allowed;

	private ContextParams(Set<Context.Parameter<?>> required, Set<Context.Parameter<?>> optional) {
		this.required = Set.copyOf(required);
		this.allowed = Sets.union(required, optional);
	}

	public ContextParams merge(ContextParams that) {

		Set<Context.Parameter<?>> required = Sets.union(this.required(), that.required());
		Set<Context.Parameter<?>> optional = Sets.difference(Sets.union(this.allowed(), that.allowed()), required);

		Builder builder = new Builder();

		required.forEach(builder::required);
		optional.forEach(builder::optional);

		return builder.build();

	}

	public ContextParams mergeAll(ContextParams... others) {
		return Arrays.stream(others).reduce(this, ContextParams::merge);
	}

	public static final class Builder {

		private final Set<Context.Parameter<?>> required = new ObjectOpenHashSet<>();
		private final Set<Context.Parameter<?>> optional = new ObjectOpenHashSet<>();

		public Builder required(Context.Parameter<?> parameter) {

			this.optional.remove(parameter);
			this.required.add(parameter);

			return this;

		}

		public Builder optional(Context.Parameter<?> parameter) {

			this.required.remove(parameter);
			this.optional.add(parameter);

			return this;

		}

		public ContextParams build() {
			return new ContextParams(this.required, this.optional);
		}

	}

}

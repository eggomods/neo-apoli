package io.github.eggohito.neo_apoli.context;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.github.eggohito.neo_apoli.context.parameter.ContextParameter;
import io.github.eggohito.neo_apoli.context.visitor.Visitor;
import io.github.eggohito.neo_apoli.util.Reporter;
import io.github.eggohito.neo_apoli.util.alias.ResourceLocationAlias;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("unchecked")
public final class Context implements ContextHolder {

	public static final ResourceLocationAlias ALIASES = new ResourceLocationAlias();

	@Getter
	private final Level level;
	@Getter
	private final Reporter reporter;

	private final ImmutableMap<ContextParameter<?>, Object> params;
	private final Set<ContextUser> visited;

	private Context(Level level, Reporter reporter, ImmutableMap<ContextParameter<?>, Object> params, Set<ContextUser> visited) {
		this.level = level;
		this.reporter = reporter;
		this.params = params;
		this.visited = visited;
	}

	@Override
	public ImmutableSet<ContextParameter<?>> parameters() {
		return params.keySet();
	}

	@Override
	public @Nullable <T> T getNullable(ContextParameter<T> parameter) {
		return (T) this.params.get(parameter);
	}

	public Visitor<ContextUser> visitor() {
		return new Visitor<>() {

			@Override
			public boolean contains(ContextUser element) {
				return visited.contains(element);
			}

			@Override
			public boolean push(ContextUser element) {
				return visited.add(element);
			}

			@Override
			public void pop(ContextUser element) {
				visited.remove(element);
			}

		};
	}

	public Context forChild(String path) {
		return new Context(this.level(), this.reporter().forChild(path), this.params, this.visited);
	}

	public void reportProblem(String message) {
		this.reporter.report(message);
	}

	public boolean hasProblems() {
		return reporter().hasProblems();
	}

	public static final class Builder implements ContextHolder {

		private final Map<ContextParameter<?>, Object> params;
		private final Set<ContextUser> visited;

		private Reporter reporter;

		Builder(Map<ContextParameter<?>, Object> params, Reporter reporter, Set<ContextUser> visited) {
			this.params = params;
			this.reporter = reporter;
			this.visited = visited;
		}

		public Builder(Context context) {
			this(new Object2ObjectLinkedOpenHashMap<>(context.params), context.reporter, context.visited);
		}

		public Builder() {
			this(new Object2ObjectLinkedOpenHashMap<>(), new Reporter(), new ObjectOpenHashSet<>());
		}

		@Override
		public Set<ContextParameter<?>> parameters() {
			return Set.copyOf(params.keySet());
		}

		@Override
		public @Nullable <T> T getNullable(ContextParameter<T> parameter) {
			return (T) this.params.get(parameter);
		}

		public <T> Builder withNullable(ContextParameter<T> parameter, @Nullable T value) {

			if (value != null) {
				this.params.put(parameter, value);
			}

			return this;

		}

		public <T> Builder withRequired(ContextParameter<T> key, @NotNull T value) {
			this.params.put(key, value);
			return this;
		}

		public <T> Builder withOptional(ContextParameter<T> key, Optional<T> value) {
			return this.withNullable(key, value.orElse(null));
		}

		public Builder withReporter(Reporter reporter) {
			this.reporter = reporter;
			return this;
		}

		public Context build(Level level) {
			return new Context(level, this.reporter, ImmutableMap.copyOf(this.params), this.visited);
		}

	}

}

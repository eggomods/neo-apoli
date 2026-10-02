package io.github.eggohito.neo_apoli.context.visitor;

import com.google.common.collect.Sets;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.Set;

public interface ClearableVisitor<T> extends Visitor<T>, AutoCloseable {

	@Override
	void close();

	static <T> ClearableVisitor<T> createThreadLocalized() {
		return new ClearableVisitor<>() {

			private final ThreadLocal<WeakReference<Set<T>>> visited = new ThreadLocal<>();

			@Override
			public boolean contains(T element) {
				return this.getVisited()
					.map(users -> users.contains(element))
					.orElse(false);
			}

			@Override
			public boolean push(T element) {

				Set<T> users = this.getVisited().orElseGet(Sets::newIdentityHashSet);
				this.visited.set(new WeakReference<>(users));

				return users.add(element);

			}

			@Override
			public void pop(T element) {
				this.getVisited().ifPresent(users -> users.remove(element));
			}

			@Override
			public void close() {
				this.visited.remove();
			}

			private Optional<Set<T>> getVisited() {
				return Optional.ofNullable(visited.get()).flatMap(ref -> Optional.ofNullable(ref.get()));
			}

		};
	}

}

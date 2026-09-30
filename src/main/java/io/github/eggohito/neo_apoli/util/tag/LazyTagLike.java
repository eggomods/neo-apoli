package io.github.eggohito.neo_apoli.util.tag;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.context.ContextUser;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagEntry;

import java.util.List;
import java.util.function.Supplier;

public class LazyTagLike<T> extends TagLike<T> implements ContextUser {

	private static final LazyTagLike<?> EMPTY = new LazyTagLike<>(ImmutableList.of(), Suppliers.memoize(() -> DataResult.success(ImmutableList.of())));
	private final Supplier<DataResult<ImmutableList<T>>> elementsGetter;

	private LazyTagLike(ImmutableList<TagEntry> entries, Supplier<DataResult<ImmutableList<T>>> elementsGetter) {
		super(entries, ImmutableList.of());
		this.elementsGetter = elementsGetter;
	}

	@Override
	public ImmutableList<T> elements() {
		return elementsGetter.get().resultOrPartial().orElse(ImmutableList.of());
	}

	@Override
	public void validate(ContextValidator validator) {
		elementsGetter.get().resultOrPartial(validator::reportProblem);
	}

	@SuppressWarnings("unchecked")
	public static <T> LazyTagLike<T> empty() {
		return (LazyTagLike<T>) EMPTY;
	}

	public static <B extends ByteBuf, T> StreamCodec<B, LazyTagLike<T>> createLazyStreamCodec(HolderLookup.RegistryLookup<T> registryLookup) {
		return createLazyStreamCodec(lookupFromRegistry(registryLookup));
	}

	public static <B extends ByteBuf, T> StreamCodec<B, LazyTagLike<T>> createLazyStreamCodec(TagEntry.Lookup<T> lookup) {
		return NeoApoliStreamCodecs.TAG_ENTRIES.map(tagEntries -> new Builder<>(tagEntries, lookup).build().getOrThrow(), LazyTagLike::entries).cast();
	}

	public static <T> Codec<LazyTagLike<T>> createLazyCodec(HolderLookup.RegistryLookup<T> registryLookup) {
		return createLazyCodec(lookupFromRegistry(registryLookup));
	}

	public static <T> Codec<LazyTagLike<T>> createLazyCodec(TagEntry.Lookup<T> lookup) {
		return TagEntry.CODEC.listOf().comapFlatMap(tagEntries -> new Builder<>(tagEntries, lookup).build(), LazyTagLike::entries);
	}

	@Getter
	public static final class Builder<T> extends AbstractBuilder<T, LazyTagLike<T>, Builder<T>> {

		Builder(List<TagEntry> entries, TagEntry.Lookup<T> lookup) {
			super(entries, lookup);
		}

		public Builder(TagEntry.Lookup<T> lookup) {
			super(lookup);
		}

		public Builder(HolderLookup.RegistryLookup<T> registryLookup) {
			super(registryLookup);
		}

		@Override
		public Builder<T> getThis() {
			return this;
		}

		@Override
		public DataResult<LazyTagLike<T>> build() {

			ImmutableList<TagEntry> entriesCopy = ImmutableList.copyOf(entries());
			Supplier<DataResult<ImmutableList<T>>> elementsGetter = Suppliers.memoize(() -> toElementsWithPartial(entries(), lookup()));

			return DataResult.success(new LazyTagLike<>(entriesCopy, elementsGetter));

		}

	}

}

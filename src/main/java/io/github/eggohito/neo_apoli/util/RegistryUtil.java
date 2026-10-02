package io.github.eggohito.neo_apoli.util;

import com.mojang.datafixers.util.Either;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Objects;

public final class RegistryUtil {

	public static <T> ResourceLocation getId(Registry<T> registry, T obj) {
		return Objects.requireNonNull(registry.getKey(obj));
	}

	public static <T> String getNamespace(Registry<T> registry, T obj) {
		return getId(registry, obj).getNamespace();
	}

	public static <T> String getPath(Registry<T> registry, T obj) {
		return getId(registry, obj).getPath();
	}

	public static <T> void validateKey(ContextValidator validator, ResourceKey<T> key) {
		validateKeyOrTag(validator, Either.left(key));
	}

	public static <T> void validateTag(ContextValidator validator, TagKey<T> tag) {
		validateKeyOrTag(validator, Either.right(tag));
	}

	public static <T> void validateKeyOrTag(ContextValidator validator, Either<ResourceKey<T>, TagKey<T>> keyOrTag) {

		if (!validator.allowsReferences()) {
			validator.reportProblem("Validator doesn't allow resolving of references!");
		}

		else {

			ResourceKey<? extends Registry<T>> registryKey = keyOrTag.map(ResourceKey::registryKey, TagKey::registry);
			HolderLookup.RegistryLookup<T> lookup = validator.resolver()
				.lookup(registryKey)
				.orElse(null);

			if (lookup == null) {
				validator.reportProblem("Registry '" + registryKey.location() + "' doesn't exist!");
			}

			else if (keyOrTag.map(lookup::get, lookup::get).isEmpty()) {
				validator.reportProblem(keyOrTag.map(key -> "Element \"" + key.location() + "\"", tag -> "Tag \"" + tag.location() + "\"") + " from registry \"" + registryKey.location() + "\" doesn't exist!");
			}

		}

	}

}

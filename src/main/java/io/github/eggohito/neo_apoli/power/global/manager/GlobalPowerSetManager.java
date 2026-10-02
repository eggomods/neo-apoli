package io.github.eggohito.neo_apoli.power.global.manager;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.event.DependencyManager;
import io.github.eggohito.neo_apoli.event.ReloadableServerResourcesEvents;
import io.github.eggohito.neo_apoli.power.PowerHolder;
import io.github.eggohito.neo_apoli.power.entity.MutablePowers;
import io.github.eggohito.neo_apoli.power.global.GlobalPowerSet;
import io.github.eggohito.neo_apoli.power.manager.PowerManager;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistryKeys;
import io.github.eggohito.neo_apoli.resource.json.JsonFileToIdConverter;
import io.github.eggohito.neo_apoli.resource.json.JsonWithSource;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import io.github.eggohito.neo_apoli.util.Reporter;
import io.github.eggohito.neo_apoli.util.ResourceLocationUtil;
import io.github.eggohito.neo_apoli.util.manager.AbstractContentManager;
import io.github.eggohito.neo_apoli.util.tag.LazyTagLike;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public final class GlobalPowerSetManager extends AbstractContentManager<ResourceLocation, GlobalPowerSet> implements IdentifiableResourceReloadListener {

	private static final GlobalPowerSetManager INSTANCE = new GlobalPowerSetManager();

	public static final ResourceLocation ID = NeoApoli.id("manager/global_power_set");
	public static final Supplier<ImmutableSet<ResourceLocation>> DEPENDENCIES = Suppliers.memoize(() -> Util.make(ImmutableSet.builder(), DependencyManager.GLOBAL_POWER_SETS.invoker()::add).build());

	private static final JsonFileToIdConverter LOADER = JsonFileToIdConverter.registry(NeoApoliRegistryKeys.GLOBAL_POWER_SET);
	private static final Logger LOGGER = LoggerFactory.getLogger(GlobalPowerSetManager.class);

	private DynamicOps<JsonElement> ops = JsonOps.INSTANCE;

	private GlobalPowerSetManager() {

	}

	@Override
	public @NotNull CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, Executor backgroundExecutor, Executor gameExecutor) {
		return CompletableFuture.supplyAsync(() -> MiscUtil.collectJsonStack(manager, LOADER, ops, LOGGER::error), backgroundExecutor)
			.thenCompose(barrier::wait)
			.thenAcceptAsync(this::apply, gameExecutor);
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	@Override
	public Collection<ResourceLocation> getFabricDependencies() {
		return DEPENDENCIES.get();
	}

	public List<GlobalPowerSet> getApplicableSets(Entity entity) {

		List<GlobalPowerSet> applicableSets = new ObjectArrayList<>();

		for (var globalPower : values()) {

			if (globalPower.doesApply(entity)) {
				applicableSets.add(globalPower);
			}

		}

		applicableSets.sort(GlobalPowerSet::compareTo);
		return applicableSets;

	}

	@ApiStatus.Internal
	public void init() {

		ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(ID, this::withContext);
		DependencyManager.GLOBAL_POWER_SETS.register(ID, dependencies -> dependencies.add(PowerManager.ID));

		ReloadableServerResourcesEvents.TAGS_UPDATED.addPhaseOrdering(PowerManager.ID, ID);
		ReloadableServerResourcesEvents.TAGS_UPDATED.register(ID, this::finalize);

		ServerEntityEvents.ENTITY_LOAD.addPhaseOrdering(PowerManager.ID, ID);
		ServerEntityEvents.ENTITY_LOAD.register(ID, this::grant);

	}

	private GlobalPowerSetManager withContext(@NotNull HolderLookup.Provider provider) {
		this.ops = provider.createSerializationContext(JsonOps.INSTANCE);
		return this;
	}

	private Set<PowerHolder<?>> flattenPowers(Collection<GlobalPowerSet> sets) {

		Set<PowerHolder<?>> holders = new ObjectOpenHashSet<>();
		for (var set : sets) {
			holders.addAll(set.powers().elements());
		}

		return holders;

	}

	private void apply(Map<ResourceLocation, List<JsonWithSource>> pending) {

		if (ops == null) {
			return;
		}

		LOGGER.info("Parsing global power sets from data packs...");
		ImmutableMap.Builder<ResourceLocation, GlobalPowerSet> builder = ImmutableMap.builder();

		Map<ResourceLocation, List<GlobalPowerSet.WithSource>> parsed = new Object2ObjectOpenHashMap<>();
		pending.forEach((id, elementWithSources) -> {

			ResourceLocationUtil.setCurrent(id);
			elementWithSources.forEach(jsonWithSource -> GlobalPowerSet.CODEC.compressedDecode(ops, jsonWithSource.json())
				.ifError(error -> LOGGER.error("Error trying to parse global power set \"{}\" from data pack [{}] (skipping): {}", id, jsonWithSource.source(), error.message()))
				.ifSuccess(set -> parsed
					.computeIfAbsent(id, k -> new ObjectArrayList<>())
					.add(new GlobalPowerSet.WithSource(set, jsonWithSource.source()))));

			ResourceLocationUtil.setCurrent(null);

		});

		LOGGER.info("Finished parsing global power sets. Merging {} similar global power sets...", parsed.values().stream().mapToInt(Collection::size).sum());

		parsed.forEach((id, setWithSources) -> setWithSources
			.stream()
			.reduce((first, second) -> merge(id, first, second))
			.ifPresent(withSource -> builder.put(id, withSource.set())));

		this.contents = builder.build();
		LOGGER.info("Finished merging global power sets");

	}

	private void grant(Entity entity, ServerLevel serverLevel) {

		try (MutablePowers mutable = MutablePowers.create(entity)) {

			List<GlobalPowerSet> applicableSets = this.getApplicableSets(entity);
			Set<PowerHolder<?>> expectedHolders = this.flattenPowers(applicableSets);

			//  Revoke all powers that are from the global power source, but not without the expected
			//  set of powers collected from all global power sets
			for (var holder : mutable.getAllFromSource(GlobalPowerSet.POWER_SOURCE)) {

				if (!expectedHolders.contains(holder)) {
					mutable.revoke(holder.id(), GlobalPowerSet.POWER_SOURCE);
				}

			}

			//  Re-grant all the expected powers collected from all global power sets
			for (var expectedHolder : expectedHolders) {
				mutable.grant(expectedHolder.id(), GlobalPowerSet.POWER_SOURCE);
			}

		}

	}

	private void finalize(ReloadableServerResources resources) {

		if (contents.isEmpty()) {
			return;
		}

		ImmutableMap.Builder<ResourceLocation, GlobalPowerSet> builder = ImmutableMap.builder();
		int size = contents.size();

		LOGGER.info("Validating {} global power set(s)...", size);

		try (Reporter.Scoped reporter = new Reporter.Scoped(errors -> LOGGER.error("Found errors while validating the following global power sets:\n{}", errors))) {

			for (var entry : contents.entrySet()) {

				ResourceLocation id = entry.getKey();
				GlobalPowerSet set = entry.getValue();

				Reporter setReporter = reporter.forChild("{\"" + id + "\"}");
				ContextValidator validator = new ContextValidator(ContextValidator.Parameters.INTENTIONALLY_EMPTY, setReporter).withResolver(MiscUtil.getLookupProvider(resources));

				set.validate(validator);

				if (!setReporter.hasProblems()) {
					builder.put(id, set);
				}

			}

		}

		this.contents = builder.build();
		LOGGER.info("Finished validating {} global power set(s). Global power set manager contains {} global power set(s)", size, this.size());

	}

	public static GlobalPowerSetManager getInstance() {
		return INSTANCE;
	}

	private static GlobalPowerSet.WithSource merge(ResourceLocation id, GlobalPowerSet.WithSource first, GlobalPowerSet.WithSource second) {

		LazyTagLike.Builder<EntityType<?>> entityTypes = new LazyTagLike.Builder<>(BuiltInRegistries.ENTITY_TYPE);
		LazyTagLike.Builder<PowerHolder<?>> powers = new LazyTagLike.Builder<>(PowerManager.TAG_LOOKUP);

		boolean replace = second.set().replace();
		int order = first.set().order();

		if (replace) {

			LOGGER.warn("Global power set \"{}\" from data pack [{}] has been replaced with a similar one from data pack [{}]!", id, first.source(), second.source());

			order = second.set().order();

		}

		else {
			entityTypes.addAll(first.set().entityTypes().entries());
			powers.addAll(first.set().powers().entries());
		}

		entityTypes.addAll(second.set().entityTypes().entries());
		powers.addAll(second.set().powers().entries());

		GlobalPowerSet set = new GlobalPowerSet(
			entityTypes.build().resultOrPartial().orElseThrow(),
			powers.build().resultOrPartial().orElseThrow(),
			replace,
			order
		);

		return new GlobalPowerSet.WithSource(set, second.source());

	}

}

package io.github.eggohito.neo_apoli.power.global;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.context.ContextUser;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.power.PowerHolder;
import io.github.eggohito.neo_apoli.util.tag.LazyTagLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public record GlobalPowerSet(LazyTagLike<EntityType<?>> entityTypes, LazyTagLike<PowerHolder<?>> powers, boolean replace, int order) implements ContextUser, Comparable<GlobalPowerSet> {

	public static final ResourceLocation POWER_SOURCE = NeoApoli.id("global");

	public static final MapCodec<GlobalPowerSet> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		NeoApoliCodecs.LAZY_ENTITY_TYPE_TAG_LIKE.optionalFieldOf("entity_types", LazyTagLike.empty()).forGetter(GlobalPowerSet::entityTypes),
		NeoApoliCodecs.LAZY_POWER_TAG_LIKE.fieldOf("powers").forGetter(GlobalPowerSet::powers),
		Codec.BOOL.optionalFieldOf("replace", false).forGetter(GlobalPowerSet::replace),
		Codec.INT.optionalFieldOf("order", 0).forGetter(GlobalPowerSet::order)
	).apply(instance, GlobalPowerSet::new));

	@Override
	public void validate(ContextValidator validator) {
		entityTypes().validate(validator.forChild(".entity_types"));
		powers().validate(validator.forChild(".powers"));
	}

	@Override
	public int compareTo(@NotNull GlobalPowerSet that) {
		return Integer.compare(this.order(), that.order());
	}

	public boolean doesApply(Entity entity) {
		return entityTypes().isEmpty()
			|| entityTypes().contains(entity.getType());
	}

	public record WithSource(GlobalPowerSet set, String source) {

	}

}

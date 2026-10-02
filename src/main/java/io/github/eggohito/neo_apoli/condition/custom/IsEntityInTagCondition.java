package io.github.eggohito.neo_apoli.condition.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.condition.Condition;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.registry.NeoApoliConditionTypes;
import io.github.eggohito.neo_apoli.util.RegistryUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public record IsEntityInTagCondition(TagKey<EntityType<?>> tag, EntityProvider entity) implements Condition {

	public static final MapCodec<IsEntityInTagCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		TagKey.hashedCodec(Registries.ENTITY_TYPE).fieldOf("tag").forGetter(IsEntityInTagCondition::tag),
		EntityProvider.CODEC.fieldOf("entity").forGetter(IsEntityInTagCondition::entity)
	).apply(instance, IsEntityInTagCondition::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, IsEntityInTagCondition> STREAM_CODEC = StreamCodec.composite(
		TagKey.streamCodec(Registries.ENTITY_TYPE), IsEntityInTagCondition::tag,
		EntityProvider.STREAM_CODEC, IsEntityInTagCondition::entity,
		IsEntityInTagCondition::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliConditionTypes.IS_ENTITY_IN_TAG;
	}

	@Override
	public boolean test(Context context) {

		try {
			return entity().getValue(context.forChild(".entity"))
				.stream()
				.filter(entity -> context.visitor().push(this))
				.map(Entity::getType)
				.anyMatch(type -> type.is(this.tag));
		}

		finally {
			context.visitor().pop(this);
		}

	}

	@Override
	public void validate(ContextValidator validator) {
		Condition.super.validate(validator);
		RegistryUtil.validateTag(validator.forChild(".tag"), tag());
		entity().validate(validator.forChild(".entity"));
	}

}

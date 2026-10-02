package io.github.eggohito.neo_apoli.mixin.impl.power.custom.modify_entity_type_tag;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.eggohito.neo_apoli.duck.internal.EntityCache;
import io.github.eggohito.neo_apoli.power.custom.ModifyEntityTypeTagPower;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.ref.WeakReference;
import java.util.Optional;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin implements EntityCache {

	@Unique
	final ThreadLocal<WeakReference<Entity>> neo_apoli$currentEntity = new ThreadLocal<>();

	@Override
	public @Nullable Entity neo_apoli$getEntity() {
		return Optional.ofNullable(neo_apoli$currentEntity.get())
			.flatMap(reference -> Optional.ofNullable(reference.get()))
			.orElse(null);
	}

	@Override
	public void neo_apoli$setEntity(Entity entity) {

		if (entity == null) {
			this.neo_apoli$currentEntity.remove();
		}

		else {
			this.neo_apoli$currentEntity.set(new WeakReference<>(entity));
		}

	}

	@ModifyReturnValue(method = "is(Lnet/minecraft/tags/TagKey;)Z", at = @At("RETURN"))
	boolean tagProxy(boolean original, TagKey<EntityType<?>> tag) {

		try (ModifyEntityTypeTagPower.VISITOR) {
			return original || ModifyEntityTypeTagPower.modify(this.neo_apoli$getEntity(), tag);
		}

	}

	@ModifyReturnValue(method = "is(Lnet/minecraft/core/HolderSet;)Z", at = @At("RETURN"))
	boolean directTagProxy(boolean original, HolderSet<EntityType<?>> directTag) {

		try (ModifyEntityTypeTagPower.VISITOR) {
			return original || ModifyEntityTypeTagPower.modify(this.neo_apoli$getEntity(), directTag);
		}

	}

}

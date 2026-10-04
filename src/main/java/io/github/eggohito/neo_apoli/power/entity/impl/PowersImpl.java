package io.github.eggohito.neo_apoli.power.entity.impl;

import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.SetMultimap;
import io.github.eggohito.neo_apoli.attachment.entity.PowersAttachment;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.PowerIdentifier;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import io.github.eggohito.neo_apoli.registry.attachment.NeoApoliEntityAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public final class PowersImpl extends AbstractPowers {

	PowersImpl(Entity holder, Map<PowerIdentifier, Power.Instance<?>> instances, SetMultimap<PowerIdentifier, ResourceLocation> sources) {
		super(holder, instances, sources);
	}

	public static Powers of(@NotNull Entity holder) {
		PowersAttachment attachment = holder.getAttached(NeoApoliEntityAttachments.POWERS);
		return attachment != null
			? new PowersImpl(holder, attachment.instances(), attachment.sources())
			: new PowersImpl(holder, Map.of(), ImmutableSetMultimap.of());
	}

}

package io.github.eggohito.neo_apoli.registry.attachment;

import com.mojang.serialization.Codec;
import io.github.eggohito.neo_apoli.NeoApoli;
import io.github.eggohito.neo_apoli.attachment.entity.PowersAttachment;
import io.github.eggohito.neo_apoli.power.entity.Powers;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import org.jetbrains.annotations.ApiStatus;

@SuppressWarnings("UnstableApiUsage")
public final class NeoApoliEntityAttachments {

	/**
	 *  It's <b>recommended</b> to use any of the helper methods in {@link Powers} instead of using this data
	 *  attachment type directly.
	 */
	public static final AttachmentType<PowersAttachment> POWERS = AttachmentRegistry.create(Powers.ID, builder -> builder
		.persistent(PowersAttachment.CODEC)
		.syncWith(PowersAttachment.STREAM_CODEC, AttachmentSyncPredicate.all())
		.copyOnDeath()
	);

	/**
	 *  An attachment specifically for tracking whether a player is riding another player.
	 */
	@ApiStatus.Internal
	public static final AttachmentType<Boolean> IS_RIDING_PLAYER = AttachmentRegistry.createPersistent(NeoApoli.id("is_riding_player"), Codec.BOOL);

	public static void registerAll() {

	}

}

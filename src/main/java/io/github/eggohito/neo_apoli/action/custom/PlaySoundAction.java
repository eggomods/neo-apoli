package io.github.eggohito.neo_apoli.action.custom;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.codec.NeoApoliCodecs;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidatable;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.entity.EntityProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.FloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.number.floats.ConstantFloatProvider;
import io.github.eggohito.neo_apoli.provider.custom.vec3.Vec3Provider;
import io.github.eggohito.neo_apoli.registry.NeoApoliActionTypes;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record PlaySoundAction(Holder<SoundEvent> sound, SoundSource category, List<EntityProvider> targets, Vec3Provider position, FloatProvider volume, FloatProvider pitch, FloatProvider minVolume) implements Action {

	public static final MapCodec<PlaySoundAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		SoundEvent.CODEC.fieldOf("sound").forGetter(PlaySoundAction::sound),
		NeoApoliCodecs.SOUND_SOURCE.optionalFieldOf("category", SoundSource.MASTER).forGetter(PlaySoundAction::category),
		EntityProvider.CODEC.listOf().optionalFieldOf("targets", List.of()).forGetter(PlaySoundAction::targets),
		Vec3Provider.CODEC.fieldOf("position").forGetter(PlaySoundAction::position),
		FloatProvider.clamped(0.0F, Float.MAX_VALUE).optionalFieldOf("volume", new ConstantFloatProvider(1.0F)).forGetter(PlaySoundAction::pitch),
		FloatProvider.clamped(0.0F, 2.0F).optionalFieldOf("pitch", new ConstantFloatProvider(1.0F)).forGetter(PlaySoundAction::pitch),
		FloatProvider.clamped(0.0F, 1.0F).optionalFieldOf("min_volume", new ConstantFloatProvider(0.0F)).forGetter(PlaySoundAction::minVolume)
	).apply(instance, PlaySoundAction::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PlaySoundAction> STREAM_CODEC = StreamCodec.composite(
		SoundEvent.STREAM_CODEC, PlaySoundAction::sound,
		NeoApoliStreamCodecs.SOUND_SOURCE, PlaySoundAction::category,
		EntityProvider.STREAM_CODEC.apply(ByteBufCodecs.list()), PlaySoundAction::targets,
		Vec3Provider.STREAM_CODEC, PlaySoundAction::position,
		FloatProvider.STREAM_CODEC, PlaySoundAction::volume,
		FloatProvider.STREAM_CODEC, PlaySoundAction::pitch,
		FloatProvider.STREAM_CODEC, PlaySoundAction::minVolume,
		PlaySoundAction::new
	);

	@Override
	public Type<?> getType() {
		return NeoApoliActionTypes.PLAY_SOUND;
	}

	@Override
	public void execute(Context context) {

		if (!(context.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		Vec3 position = position()
			.getValue(context.forChild(".position"))
			.orElse(null);

		if (position == null) {
			return;
		}

		float volume = volume().getFloat(context.forChild(".volume"));
		float pitch = pitch().getFloat(context.forChild(".pitch"));
		float minVolume = minVolume().getFloat(context.forChild(".min_volume"));

		double range = sound().value().getRange(volume);
		long seed = serverLevel.getRandom().nextLong();

		for (var listener : this.getListeners(context, serverLevel)) {

			Vec3 delta = position.subtract(listener.position());
			double distance = delta.length();

			Vec3 actualPosition = position;
			float actualVolume = volume;

			if (distance > range) {

				if (minVolume <= 0.0F) {
					continue;
				}

				double distanceSqr = Math.sqrt(distance);

				actualPosition = new Vec3(listener.getX() + delta.x() / distanceSqr * 2.0, listener.getY() + delta.y() / distanceSqr * 2.0, listener.getZ() + delta.z() / distanceSqr * 2.0);
				actualVolume = minVolume;

			}

			listener.connection.send(new ClientboundSoundPacket(sound(), category(), actualPosition.x(), actualPosition.y(), actualPosition.z(), actualVolume, pitch, seed));

		}

	}

	@Override
	public void validate(ContextValidator validator) {
		Action.super.validate(validator);
		ContextValidatable.validate(targets(), validator, index -> ".targets[" + index + "]");
		position().validate(validator.forChild(".position"));
		volume().validate(validator.forChild(".volume"));
		pitch().validate(validator.forChild(".pitch"));
		minVolume().validate(validator.forChild(".min_volume"));
	}

	private List<ServerPlayer> getListeners(Context context, ServerLevel serverLevel) {

		if (targets().isEmpty()) {
			return serverLevel.getServer().getPlayerList().getPlayers();
		}

		else {

			List<ServerPlayer> listeners = new ObjectArrayList<>();
			MiscUtil.iterateList(targets(), (index, provider) -> provider.getValue(context.forChild(".targets[" + index + "]"))
				.filter(ServerPlayer.class::isInstance)
				.map(ServerPlayer.class::cast)
				.ifPresent(listeners::add));

			return listeners;

		}

	}

}

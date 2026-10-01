package io.github.eggohito.neo_apoli.provider.custom.string;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.eggohito.neo_apoli.codec.NeoApoliStreamCodecs;
import io.github.eggohito.neo_apoli.context.Context;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.provider.custom.nbt.NbtProvider;
import io.github.eggohito.neo_apoli.registry.provider.NeoApoliStringProviderTypes;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record NbtStringProvider(NbtProvider source, NbtPathArgument.NbtPath path) implements StringProvider {

	public static final MapCodec<NbtStringProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
		NbtProvider.CODEC.fieldOf("source").forGetter(NbtStringProvider::source),
		NbtPathArgument.NbtPath.CODEC.fieldOf("path").forGetter(NbtStringProvider::path)
	).apply(instance, NbtStringProvider::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, NbtStringProvider> STREAM_CODEC = StreamCodec.composite(
		NbtProvider.STREAM_CODEC, NbtStringProvider::source,
		NeoApoliStreamCodecs.NBT_PATH, NbtStringProvider::path,
		NbtStringProvider::new
	);

	@Override
	public @NotNull StringProvider.Type<?> getType() {
		return NeoApoliStringProviderTypes.NBT;
	}

	@Override
	public Optional<String> getValue(Context context) {

		Tag source = source()
			.getValue(context.forChild(".source"))
			.orElse(null);

		if (source == null) {
			return Optional.empty();
		}

		try {

			List<Tag> elements = path().get(source);
			int size = elements.size();

			if (size == 1) {
				Tag element = elements.getFirst();
				return element.asString().or(() -> Optional.of(element.toString()));
			}

			else if (size > 1) {
				return Optional.of(Integer.toString(path().countMatching(source)));
			}

		}

		catch (CommandSyntaxException e) {
			context.reportProblem("Error transforming NBT \"" + source + "\" in NBT path \"" + path() + "\": " + e.getMessage());
		}

		return Optional.empty();

	}

	@Override
	public void validate(ContextValidator validator) {
		StringProvider.super.validate(validator);
		source().validate(validator.forChild(".source"));
	}

}

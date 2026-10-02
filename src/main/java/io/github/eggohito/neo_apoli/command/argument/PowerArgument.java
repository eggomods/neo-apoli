package io.github.eggohito.neo_apoli.command.argument;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;
import io.github.eggohito.neo_apoli.context.ContextValidatable;
import io.github.eggohito.neo_apoli.context.ContextValidator;
import io.github.eggohito.neo_apoli.power.Power;
import io.github.eggohito.neo_apoli.power.PowerHolder;
import io.github.eggohito.neo_apoli.power.PowerIdentifier;
import io.github.eggohito.neo_apoli.power.manager.PowerManager;
import io.github.eggohito.neo_apoli.registry.NeoApoliRegistryKeys;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public record PowerArgument(boolean allowTags) implements ArgumentType<PowerArgument.Result> {

	@Override
	public Result parse(StringReader reader) throws CommandSyntaxException {

		if (reader.canRead() && reader.peek() == '#' && allowTags()) {

			reader.skip();
			ResourceLocation id = ResourceLocation.read(reader);

			return new Result.Collection(TagKey.create(NeoApoliRegistryKeys.POWER, id));

		}

		else {
			PowerIdentifier powerId = PowerIdentifier.read(reader);
			return new Result.Singleton(powerId);
		}

	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {

		if (allowTags()) {
			SharedSuggestionProvider.suggestResource(PowerManager.getInstance().tags(), builder, "#");
		}

		return SharedSuggestionProvider.suggest(PowerManager.getInstance().keys(), builder, PowerIdentifier::toString, id -> Component.literal(id.toString()));

	}

	public static PowerArgument power() {
		return new PowerArgument(false);
	}

	public static PowerArgument powerOrTag() {
		return new PowerArgument(true);
	}

	public static Result getResult(CommandContext<CommandSourceStack> context, String name) {
		return context.getArgument(name, Result.class);
	}

	public static List<PowerHolder<?>> getPowers(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
		return context.getArgument(name, Result.class).get();
	}

	public static PowerHolder<?> getPower(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
		return switch (getResult(context, name)) {
			case Result.Singleton singleton ->
				singleton.get().getFirst();
			case Result.Collection collection ->
				throw MiscUtil.createCommandException(() -> "Expected a power, but got tag \"#" + collection.id() + "\"");
		};
	}

	public static List<PowerHolder<?>> getTag(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
		return switch (getResult(context, name)) {
			case Result.Singleton singleton ->
				throw MiscUtil.createCommandException(() -> "Expected a tag, but got power " + singleton.id() + "!");
			case Result.Collection collection ->
				collection.get();
		};
	}

	public static Either<Result.Singleton, Result.Collection> getPowerOrTag(CommandContext<CommandSourceStack> context, String name) {
		return switch (getResult(context, name)) {
			case Result.Singleton singleton ->
				Either.left(singleton);
			case Result.Collection collection ->
				Either.right(collection);
		};
	}

	public sealed interface Result extends ContextValidatable permits Result.Singleton, Result.Collection {

		List<PowerHolder<?>> get() throws CommandSyntaxException;

		record Singleton(PowerIdentifier id) implements Result {

			@Override
			public List<PowerHolder<?>> get() throws CommandSyntaxException {
				return List.of(PowerManager.getInstance().getAsResult(id()).getOrThrow(error -> MiscUtil.createCommandException(() -> error)));
			}

			@Override
			public void validate(ContextValidator validator) {
				id().validate(validator);
			}

		}

		record Collection(TagKey<Power> tag) implements Result {

			@Override
			public List<PowerHolder<?>> get() throws CommandSyntaxException {
				return PowerManager.getInstance().getTagAsResult(this.id()).getOrThrow(error -> MiscUtil.createCommandException(() -> error));
			}

			@Override
			public void validate(ContextValidator validator) {
				PowerManager.getInstance().getTagAsResult(this.id()).ifError(error -> validator.reportProblem(error.message()));
			}

			public ResourceLocation id() {
				return tag().location();
			}

		}

	}

	public enum Info implements ArgumentTypeInfo<PowerArgument, Template> {

		INSTANCE;

		@Override
		public void serializeToNetwork(PowerArgument.Template template, FriendlyByteBuf buf) {
			buf.writeBoolean(template.allowTags());
		}

		@Override
		public @NotNull PowerArgument.Template deserializeFromNetwork(FriendlyByteBuf buf) {
			return new PowerArgument.Template(this, buf.readBoolean());
		}

		@Override
		public void serializeToJson(PowerArgument.Template template, JsonObject jsonObject) {
			jsonObject.addProperty("allow_tags", template.allowTags());
		}

		@Override
		public @NotNull PowerArgument.Template unpack(PowerArgument argumentType) {
			return new PowerArgument.Template(this, argumentType.allowTags());
		}

	}

	public record Template(Info type, boolean allowTags) implements ArgumentTypeInfo.Template<PowerArgument> {

		@Override
		public @NotNull PowerArgument instantiate(CommandBuildContext commandBuildContext) {
			return new PowerArgument(allowTags());
		}

	}
}

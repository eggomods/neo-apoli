package io.github.eggohito.neo_apoli.command;

import com.google.gson.JsonElement;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import io.github.eggohito.neo_apoli.action.Action;
import io.github.eggohito.neo_apoli.command.argument.ActionArgument;
import io.github.eggohito.neo_apoli.util.JsonTextFormatter;
import io.github.eggohito.neo_apoli.util.MiscUtil;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

//  FIXME: Bring me back!!
public class ActionCommand {

	public static void register(CommandBuildContext buildContext, CommandNode<CommandSourceStack> rootNode) {

		var baseNode = literal("action")
			.requires(source -> source.hasPermission(2))
			.build();

		baseNode.addChild(Dump.node(buildContext));
		baseNode.addChild(Execute.node(buildContext));

		rootNode.addChild(baseNode);

	}

	public static final class Dump {

		public static CommandNode<CommandSourceStack> node(CommandBuildContext buildContext) {

			var node = literal("dump")
				.then(argument("action", ActionArgument.id(buildContext))
					.executes(Dump::withDefaultIndent)
					.then(argument("indent", IntegerArgumentType.integer(0))
						.executes(Dump::withSpecificIndent)));

			return node.build();

		}

		private static int withDefaultIndent(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
			return execute(context, 4);
		}

		private static int withSpecificIndent(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
			return execute(context, IntegerArgumentType.getInteger(context, "indent"));
		}

		public static int execute(CommandContext<CommandSourceStack> context, int indent) throws CommandSyntaxException {

			CommandSourceStack source = context.getSource();
			Action action = ActionArgument.getActions(context, "action").getFirst();

			return switch (Action.CODEC.encodeStart(source.registryAccess().createSerializationContext(JsonOps.INSTANCE), action)) {
				case DataResult.Success<JsonElement> success -> {

					JsonElement jsonElement = success.value();
					source.sendSuccess(() -> JsonTextFormatter.format(jsonElement, indent), false);

					yield jsonElement.toString().length();

				}
				case DataResult.Error<JsonElement> error ->
					throw MiscUtil.createCommandException(error::message);
			};

		}

	}

	public static final class Execute {

		public static CommandNode<CommandSourceStack> node(CommandBuildContext buildContext) {

			var executeNode = literal("execute").build();
			var withNode = literal("with").build();
			var forNode = literal("for").build();
			var actionNode = argument("action", ActionArgument.idOrTagOrInline(buildContext)).executes(Execute::execute).build();

//			NeoApoliContextParameters.addAsArguments(buildContext, executeNode, withNode);

			forNode.addChild(actionNode);
			executeNode.addChild(withNode);
			executeNode.addChild(forNode);

			return executeNode;

		}

		public static int execute(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {

//			CommandSourceStack source = commandContext.getSource();
//			Context.Builder contextBuilder = source.neo_apoli$getContextBuilder();
//
//			List<Action> actions = ActionArgument.getActions(commandContext, "action");
//			int executed = 0;
//
//			for (var action : actions) {
//
//				String path = ActionManager.getInstance().getKeyAsResult(action).mapOrElse(id -> "{\"" + id + "\"}", ignored -> "{type: \"" + Util.getRegisteredName(NeoApoliRegistries.ACTION_TYPE, action.getType()) + "\"}");
//				Reporter reporter = new Reporter(path);
//
//				Context.Validator validator = new Context.Validator(contextBuilder.toKeySet(), reporter).withResolver(source.registryAccess());
//				action.validate(validator);
//
//				if (reporter.hasProblems()) {
//					throw MiscUtil.createCommandException(Component.literal("Found errors while validating the action\n" + reporter.getReport()));
//				}
//
//				Context context = contextBuilder.withReporter(reporter).build(source.getLevel());
//				action.execute(context);
//
//				if (reporter.hasProblems()) {
//					throw MiscUtil.createCommandException(Component.literal("Found errors while executing the action\n" + reporter.getReport()));
//				}
//
//				executed++;
//
//			}
//
//			commandContext.getSource().sendSuccess(() -> Component.literal("Successfully executed action!"), false);
//			return executed;

			return 0;

		}

	}

}

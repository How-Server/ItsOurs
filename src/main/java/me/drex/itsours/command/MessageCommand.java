package me.drex.itsours.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.util.Modify;
import me.drex.itsours.command.argument.ClaimArgument;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Map;

import static me.drex.message.api.LocalizedMessage.localized;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MessageCommand extends AbstractCommand {

    public static final MessageCommand INSTANCE = new MessageCommand();

    private static final String RESET = "reset";
    private static final RequiredArgumentBuilder<ServerCommandSource, String> MESSAGE_ARGUMENT = argument("message", StringArgumentType.greedyString()).suggests((context, builder) -> CommandSource.suggestMatching(List.of(RESET), builder));

    private MessageCommand() {
        super("message");
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.then(
            ClaimArgument.ownClaims()
                .then(
                    literal("enter")
                        .then(
                            MESSAGE_ARGUMENT
                                .executes(ctx -> execute(ctx.getSource(), ClaimArgument.getClaim(ctx), StringArgumentType.getString(ctx, "message"), true))
                        )
                )
                .then(
                    literal("leave")
                        .then(
                            MESSAGE_ARGUMENT
                                .executes(ctx -> execute(ctx.getSource(), ClaimArgument.getClaim(ctx), StringArgumentType.getString(ctx, "message"), false))
                        )

                )
        ).requires(src -> ItsOurs.checkPermission(src, "itsours.message", 2));
    }

    private int execute(ServerCommandSource src, AbstractClaim claim, String message, boolean enter) throws CommandSyntaxException {
        validateAction(src, claim, Flags.MODIFY, Modify.MESSAGE.node());
        boolean reset = message.equals(RESET);
        if (reset) message = null;
        if (enter) {
            claim.setEnterMessage(message);
        } else {
            claim.setLeaveMessage(message);
        }
        String id = enter ? "enter" : "leave";
        if (reset) {
            src.sendFeedback(() -> localized(String.format("text.itsours.commands.message.%s.reset", id)), false);
            return 0;
        } else {
            String finalMessage = message;
            src.sendFeedback(() -> localized(String.format("text.itsours.commands.message.%s", id), Map.of("message", Text.literal(finalMessage))), false);
            return 1;
        }
    }
}

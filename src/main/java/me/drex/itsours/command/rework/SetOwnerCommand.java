package me.drex.itsours.command.rework;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.ClaimList;
import me.drex.itsours.command.rework.argument.ClaimArgument;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;

public class SetOwnerCommand extends AbstractCommand {

    public static final SetOwnerCommand INSTANCE = new SetOwnerCommand();

    private SetOwnerCommand() {
        super("setOwner");
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.then(
                ClaimArgument.allClaims().then(
                        argument("owner", GameProfileArgumentType.gameProfile())
                                .executes(ctx -> execute(ctx.getSource(), ClaimArgument.getClaim(ctx), GameProfileArgumentType.getProfileArgument(ctx, "owner")))
                )
        ).requires(src -> Permissions.check(src, "itsours.setowner"));
    }

    private int execute(ServerCommandSource src, AbstractClaim claim, Collection<GameProfile> targets) throws CommandSyntaxException {
        if (targets.isEmpty()) throw EntityArgumentType.PLAYER_NOT_FOUND_EXCEPTION.create();
        if (targets.size() > 1) throw EntityArgumentType.TOO_MANY_PLAYERS_EXCEPTION.create();
        GameProfile profile = targets.iterator().next();
        claim.setOwner(profile.getId());
        src.sendFeedback(Text.translatable("text.itsours.commands.setOwner", claim.getFullName(), Texts.toText(profile)), false);
        return 1;
    }

}
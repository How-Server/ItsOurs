package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.claim.flags.node.ChildNode;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import static me.drex.message.api.LocalizedMessage.localized;

public abstract class AbstractCommand {

    public static final CommandSyntaxException NO_CLAIM_AT_POS = new SimpleCommandExceptionType(localized("text.itsours.argument.claim.notFoundPos")).create();
    public static final CommandSyntaxException MISSING_PERMISSION = new SimpleCommandExceptionType(localized("text.itsours.argument.general.missingPermission")).create();

    protected final String literal;

    public AbstractCommand(@NotNull String literal) {
        this.literal = literal;
    }

    public void registerCommand(LiteralArgumentBuilder<ServerCommandSource> literal) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgument = CommandManager.literal(this.literal);
        register(literalArgument);
        literal.then(literalArgument);
    }

    protected abstract void register(LiteralArgumentBuilder<ServerCommandSource> literal);

    public String getLiteral() {
        return literal;
    }

    public AbstractClaim getClaim(Entity entity) throws CommandSyntaxException {
        return ClaimList.getClaimAt(entity).orElseThrow(() -> NO_CLAIM_AT_POS);
    }

    public void validateAction(ServerCommandSource src, AbstractClaim claim, ChildNode... nodes) throws CommandSyntaxException {
        // Console
        if (src.getEntity() == null) return;
        // Throw exception if player doesn't have requested permissions
        if (!claim.checkAction(src.getPlayerOrThrow().getUuid(), nodes)) throw MISSING_PERMISSION;
    }

}

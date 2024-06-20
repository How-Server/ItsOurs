package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.user.ClaimTrackingPlayer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ShowCommand extends AbstractCommand {

    public static final ShowCommand SHOW = new ShowCommand("show", true);
    public static final ShowCommand HIDE = new ShowCommand("hide", false);

    private final boolean show;

    private ShowCommand(String literal, boolean show) {
        super(literal);
        this.show = show;
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        literal.executes(ctx -> execute(ctx.getSource()));
    }

    private int execute(ServerCommandSource src) throws CommandSyntaxException {
        ServerPlayerEntity player = src.getPlayerOrThrow();
        ClaimTrackingPlayer claimTrackingPlayer = (ClaimTrackingPlayer)player;
        if (show) {
            claimTrackingPlayer.trackClaims();
        } else {
            claimTrackingPlayer.unTrackClaims();
        }
        return 1;
    }

}

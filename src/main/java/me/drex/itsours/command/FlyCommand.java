package me.drex.itsours.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.drex.itsours.ItsOurs;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.list.ClaimList;
import me.drex.itsours.user.PlayerData;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class FlyCommand extends ToggleCommand {

    public static final FlyCommand INSTANCE = new FlyCommand();

    private FlyCommand() {
        super("fly", PlayerData::flight, PlayerData::setFlight, "text.itsours.commands.fly");
    }

    @Override
    protected void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        super.register(literal.requires(src -> ItsOurs.checkPermission(src, "itsours.fly", 2)));
    }

    @Override
    protected void afterToggle(ServerCommandSource src, boolean newValue) throws CommandSyntaxException {
        ServerPlayerEntity player = src.getPlayer();
        if (ClaimList.getClaimAt(player).isPresent()
            && ClaimList.getClaimAt(player).get().checkAction(null, Flags.GLIDE)
            && (player.getEntityWorld().getRegistryKey().equals(World.OVERWORLD)
            || player.getEntityWorld().getRegistryKey().equals(World.END)
            || player.getEntityWorld().getRegistryKey().equals(World.NETHER))) {
            player.interactionManager.getGameMode().setAbilities(player.getAbilities());
            if (newValue) {
                player.getAbilities().allowFlying = true;
            }
            player.networkHandler.sendPacket(new PlayerAbilitiesS2CPacket(player.getAbilities()));
        }
        super.afterToggle(src, newValue);
    }
}

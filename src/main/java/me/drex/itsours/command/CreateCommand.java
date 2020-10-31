package me.drex.itsours.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import me.drex.itsours.ItsOursMod;
import me.drex.itsours.claim.AbstractClaim;
import me.drex.itsours.claim.Claim;
import me.drex.itsours.claim.Subzone;
import me.drex.itsours.user.ClaimPlayer;
import me.drex.itsours.util.Color;
import me.drex.itsours.util.TextComponentUtil;
import net.kyori.adventure.text.Component;
import net.minecraft.block.Block;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class CreateCommand extends Command {


    public static void register(LiteralArgumentBuilder<ServerCommandSource> literal) {
        RequiredArgumentBuilder<ServerCommandSource, String> name = RequiredArgumentBuilder.argument("name", StringArgumentType.word());
        name.executes(ctx -> create(ctx.getSource(), StringArgumentType.getString(ctx, "name")));
        LiteralArgumentBuilder<ServerCommandSource> command = LiteralArgumentBuilder.literal("create");
        command.executes(ctx -> create(ctx.getSource(), ctx.getSource().getPlayer().getEntityName()));
        command.then(name);
        literal.then(command);
    }

    public static int create(ServerCommandSource source, String name) throws CommandSyntaxException {
        ClaimPlayer claimPlayer = (ClaimPlayer) source.getPlayer();
        if (claimPlayer.arePositionsSet()) {
            BlockPos min = new BlockPos(claimPlayer.getLeftPosition());
            min = new BlockPos(min.getX(), 1, min.getZ());
            BlockPos max = new BlockPos(claimPlayer.getRightPosition());
            max = new BlockPos(max.getX(), 256, max.getZ());
            if (!AbstractClaim.isNameValid(name))
                throw new SimpleCommandExceptionType(TextComponentUtil.error("Claim name is to long or contains invalid characters")).create();
            AbstractClaim claim = new Claim(name, source.getPlayer().getUuid(), min, max, source.getWorld(), null);
            if (claim.intersects()) {
                AbstractClaim parent = ItsOursMod.INSTANCE.getClaimList().get(source.getWorld(), min);
                validatePermission(parent, source.getPlayer().getUuid(), "modify.subzone");
                if (parent != null && parent.contains(max)) {
                    for (Subzone subzone : parent.getSubzones()) {
                        if (subzone.getName().equals(name))
                            throw new SimpleCommandExceptionType(TextComponentUtil.error("Claim name is already taken")).create();
                    }
                    claim = new Subzone(name, source.getPlayer().getUuid(), min, max, source.getWorld(), null, parent);
                } else {
                    throw new SimpleCommandExceptionType(TextComponentUtil.error("Claim couldn't be created, because it would overlap with another claim")).create();
                }
            } else {
                if (ItsOursMod.INSTANCE.getBlockManager().getBlocks(source.getPlayer().getUuid()) < claim.getArea())
                    throw new SimpleCommandExceptionType(TextComponentUtil.error("You don't have enough claim blocks")).create();
                if (ItsOursMod.INSTANCE.getClaimList().contains(name))
                    throw new SimpleCommandExceptionType(TextComponentUtil.error("Claim name is already taken")).create();
                BlockPos size = claim.getSize();
                ((ClaimPlayer) source.getPlayer()).sendMessage(Component.text("Claim " + name + " has been created (" + size.getX() + " x " + size.getY() + " x " + size.getZ() + ")").color(Color.LIGHT_GREEN));
            }
            if (claimPlayer.getLastShowClaim() != null) claimPlayer.getLastShowClaim().show(source.getPlayer(), false);
            claimPlayer.setLastShow(claim, source.getPlayer().getBlockPos(), source.getWorld());
            ItsOursMod.INSTANCE.getClaimList().add(claim);
            claim.show(source.getPlayer(), true);


            //reset positions
            claimPlayer.setLeftPosition(null);
            claimPlayer.setRightPosition(null);
            return 1;
        } else {
            claimPlayer.sendMessage(Component.text("You need to select the corners of your claim with a golden shovel (left- / rightclick) first.").color(Color.RED));
            return 0;
        }

    }
}

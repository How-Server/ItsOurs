package me.drex.itsours;

import eu.pb4.common.protection.api.CommonProtection;
import me.drex.itsours.claim.flags.Flags;
import me.drex.itsours.claim.flags.util.FlagBuilderUtil;
import me.drex.itsours.claim.util.ItsoursProtectionProvider;
import me.drex.itsours.command.CommandManager;
import me.drex.itsours.data.DataManager;
import me.drex.itsours.listener.PlayerEventListener;
import me.drex.itsours.util.ItsOursPlaceholders;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ItsOurs {

    public static final String MOD_ID = "itsours";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final ItsOurs INSTANCE = new ItsOurs();

    private ItsOurs() {
    }

    protected void registerEvents() {
        CommonProtection.register(Identifier.of(MOD_ID, "claim_protection"), ItsoursProtectionProvider.INSTANCE);
        ItsOursPlaceholders.register();
        FlagBuilderUtil.init();
        CommandRegistrationCallback.EVENT.register(CommandManager.INSTANCE::register);
        ServerLifecycleEvents.SERVER_STARTED.register(DataManager::load);
        PlayerEventListener.registerPlayerListeners();
    }

    public static boolean checkPermission(ServerCommandSource src, String permission, int fallback) {
        try {
            return Permissions.check(src, permission, fallback);
        } catch (Throwable ignored) {
            return src.hasPermissionLevel(fallback);
        }
    }

}

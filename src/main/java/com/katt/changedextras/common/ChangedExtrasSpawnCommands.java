package com.katt.changedextras.common;

import com.katt.changedextras.ChangedExtras;
import com.katt.changedextras.common.debug.LatexDebugManager;
import com.katt.changedextras.common.LatexSpawnRegistry;
import com.katt.changedextras.network.ChangedExtrasNetwork;
import com.katt.changedextras.network.OpenLatexSpawnControlScreenPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = ChangedExtras.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ChangedExtrasSpawnCommands {
    private ChangedExtrasSpawnCommands() {
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("changedextras")
                .then(Commands.literal("admin")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("spawns")
                                .executes(context -> openScreen(context.getSource()))))
                .then(Commands.literal("client")
                        .then(Commands.literal("debug")
                                .executes(context -> toggleClientDebug(context.getSource())))));
    }

    private static int openScreen(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ChangedExtrasNetwork.INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> player),
                new OpenLatexSpawnControlScreenPacket(
                        source.getServer().getGameRules().getBoolean(ChangedExtrasGameRules.LATEX_SPAWN_IN_DAY),
                        LatexSpawnRegistry.buildEntries(source.getServer())
                )
        );
        return 1;
    }

    private static int toggleClientDebug(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        boolean enabled = LatexDebugManager.toggle(player);
        source.sendSuccess(() -> Component.literal("Changed Extras client debug " + (enabled ? "enabled" : "disabled") + "."), false);
        return 1;
    }
}

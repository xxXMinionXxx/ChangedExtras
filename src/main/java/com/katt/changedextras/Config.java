package com.katt.changedextras;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ChangedExtras.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue SERVER_DISCOVERY_ENABLED = BUILDER
            .comment("When enabled, authorized clients can list this server in the Changed Extras discovery tab.")
            .define("serverDiscoveryEnabled", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean serverDiscoveryEnabled = true;

    private Config() {
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        serverDiscoveryEnabled = SERVER_DISCOVERY_ENABLED.get();
    }
}

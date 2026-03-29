package com.katt.changedextras.client;

import com.katt.changedextras.init.ChangedExtrasSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public final class JackpotSoundManager {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(JackpotSoundManager.class);

    private static final Set<UUID> activeJackpots = new HashSet<>();
    private static SoundInstance loopingSound = null;

    private JackpotSoundManager() {}

    public static void onJackpotStateChanged(UUID playerUuid, boolean active) {
        LOGGER.info("[JackpotSoundManager] onJackpotStateChanged: uuid={}, active={}", playerUuid, active);
        
        if (active) {
            activeJackpots.add(playerUuid);
        } else {
            activeJackpots.remove(playerUuid);
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            LOGGER.warn("[JackpotSoundManager] mc.player is null!");
            return;
        }

        if (playerUuid.equals(mc.player.getUUID())) {
            LOGGER.info("[JackpotSoundManager] Matching player, active={}", active);
            if (active) {
                startJackpotMusic(mc);
            } else {
                stopJackpotMusic(mc);
            }
        }
    }

    public static boolean isActive(UUID playerUuid) {
        return activeJackpots.contains(playerUuid);
    }

    public static void clear(Minecraft mc) {
        activeJackpots.clear();
        if (loopingSound != null) {
            mc.getSoundManager().stop(loopingSound);
            loopingSound = null;
        }
    }

    private static void startJackpotMusic(Minecraft mc) {
        LOGGER.info("[JackpotSoundManager] startJackpotMusic called");
        
        if (loopingSound != null) {
            mc.getSoundManager().stop(loopingSound);
        }

        loopingSound = new SimpleSoundInstance(
                ChangedExtrasSounds.JACKPOT_SONG.get().getLocation(),
                SoundSource.RECORDS,
                1.0f, 1.0f,
                RandomSource.create(),
                true,
                0,
                SoundInstance.Attenuation.NONE,
                0.0, 0.0, 0.0,
                true
        );
        LOGGER.info("[JackpotSoundManager] Playing sound: {}", ChangedExtrasSounds.JACKPOT_SONG.get().getLocation());
        mc.getSoundManager().play(loopingSound);
    }

    private static void stopJackpotMusic(Minecraft mc) {
        LOGGER.info("[JackpotSoundManager] stopJackpotMusic called");
        
        if (loopingSound != null) {
            mc.getSoundManager().stop(loopingSound);
            loopingSound = null;
        }
        mc.getSoundManager().play(
                SimpleSoundInstance.forUI(ChangedExtrasSounds.JACKPOT_STOP.get(), 1.0f)
        );
    }
}

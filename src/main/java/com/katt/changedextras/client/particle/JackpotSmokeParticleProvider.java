package com.katt.changedextras.client.particle;

import com.katt.changedextras.init.ChangedExtrasParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class JackpotSmokeParticleProvider implements ParticleProvider<SimpleParticleType> {
    private final SpriteSet sprites;

    public JackpotSmokeParticleProvider(SpriteSet sprites) {
        this.sprites = sprites;
    }

    @Override
    public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                   double x, double y, double z,
                                   double xSpeed, double ySpeed, double zSpeed) {

        // Search for the entity exactly at or very near the spawn coordinates
        // We inflate the search area by 0.5 blocks to ensure we catch the player hitbox
        AABB searchBox = new AABB(x, y, z, x, y, z).inflate(0.5D);
        List<Entity> entities = level.getEntities((Entity)null, searchBox, entity -> true);

        Entity targetEntity = null;
        if (!entities.isEmpty()) {
            targetEntity = entities.get(0); // Take the first entity found (usually the player)
        }

        // Pass the found entity to your constructor
        return new JackpotSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites, targetEntity);
    }

    public static void register(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(
                ChangedExtrasParticles.JACKPOT_AURA.get(),
                JackpotSmokeParticleProvider::new
        );
    }
}
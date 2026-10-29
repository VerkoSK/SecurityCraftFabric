package net.geforcemods.securitycraft.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;

public class FloorTrapCloudParticle extends TextureSheetParticle {
	private final SpriteSet sprites;

	public FloorTrapCloudParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
		super(level, x, y, z, 0.0D, 0.0D, 0.0D);
		this.sprites = sprites;
		setSize(0.01F, 0.01F);
		pickSprite(sprites);
		quadSize *= random.nextFloat() * 0.6F + 0.6F;
		lifetime = (int) (16.0D / (Math.random() * 0.8D + 0.2D));
		hasPhysics = false;
		friction = 1.0F;
		gravity = 0.0F;
	}

	@Override
	public void tick() {
		super.tick();
		setSpriteFromAge(sprites);
	}

	@Override
	protected int getLightColor(float partialTicks) {
		return LightTexture.FULL_BRIGHT;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	public static class Provider implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Provider(SpriteSet sprites) {
			this.sprites = sprites;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			double radius = 0.2D;

			return new FloorTrapCloudParticle(level, x + (level.random.nextDouble() * radius * 2 - radius), y + (level.random.nextDouble() * radius * 2 - radius), z + (level.random.nextDouble() * radius * 2 - radius), sprites);
		}
	}
}

package net.geforcemods.securitycraft.misc;

import net.geforcemods.securitycraft.SCContent;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * The mod's own sound events. 1:1 with upstream SCSounds, except that Fabric has no deferred registry, so
 * {@link #register()} has to be called from the mod initializer to put the events into the sound registry.
 */
public enum SCSounds {
	ELECTRIFIED("electrified"),
	LOCK("lock");

	public final String path;
	public final Identifier location;
	public final SoundEvent event;

	private SCSounds(String path) {
		this.path = path;
		location = SCContent.id(path);
		event = SoundEvent.createVariableRangeEvent(location);
	}

	public static void register() {
		for (SCSounds sound : values()) {
			Registry.register(BuiltInRegistries.SOUND_EVENT, sound.location, sound.event);
		}
	}
}

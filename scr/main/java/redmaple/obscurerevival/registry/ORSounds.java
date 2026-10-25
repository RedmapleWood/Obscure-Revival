package redmaple.obscurerevival.registry;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import redmaple.obscurerevival.ObscureRevival;

public final class ORSounds {

    public static final SoundEvent BLOCK_FLETCHING_TABLE_USE = register("block.fletching_table.use");

    private static SoundEvent register(String id) {
        Identifier identifier = Identifier.of(ObscureRevival.MOD_ID, id);
        return Registry.register(Registries.SOUND_EVENT, identifier, SoundEvent.of(identifier));
    }

    public static void register() {
        ObscureRevival.LOGGER.info("\"Sound Events\" registered successfully!");
    }

    private ORSounds() {}
}
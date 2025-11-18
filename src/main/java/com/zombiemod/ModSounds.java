package com.zombiemod;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
        DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, ZombieMod.MODID);

    // Mystery Box sound
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTERY_BOX = registerSound("mystery_box");

    // Round start sound
    public static final DeferredHolder<SoundEvent, SoundEvent> ROUND_START = registerSound("round_start");

    // Round end sound
    public static final DeferredHolder<SoundEvent, SoundEvent> ROUND_END = registerSound("round_end");

    // Jukebox music - 115
    public static final DeferredHolder<SoundEvent, SoundEvent> SONG_115 = registerSound("115");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(ZombieMod.MODID, name);
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}

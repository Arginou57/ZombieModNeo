package com.zombiemod;

import com.zombiemod.command.*;
import com.zombiemod.commands.ZombieHelpCommand;
import com.zombiemod.commands.ZombieMapCommand;
import com.zombiemod.config.ZombieConfig;
import com.zombiemod.config.ZombieDropsConfig;
import com.zombiemod.config.ZombieMobsConfig;
import com.zombiemod.event.*;
import com.zombiemod.client.ZombieHUD;
import com.zombiemod.manager.GameManager;
import com.zombiemod.map.MapManager;
import com.zombiemod.network.NetworkHandler;
import com.zombiemod.system.ServerJukeboxTracker;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod(ZombieMod.MODID)
public class ZombieMod {
    public static final String MODID = "zombiemod";

    public ZombieMod(IEventBus modEventBus) {
        // Register sounds
        ModSounds.register(modEventBus);

        // Register event handlers
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(ZombieEventHandler.class);
        NeoForge.EVENT_BUS.register(ChestInteractionHandler.class);
        NeoForge.EVENT_BUS.register(PlayerDeathHandler.class);
        NeoForge.EVENT_BUS.register(ZombieDropHandler.class);
        NeoForge.EVENT_BUS.register(PlayerConnectionHandler.class);
        NeoForge.EVENT_BUS.register(JukeboxInteractionHandler.class);
        NeoForge.EVENT_BUS.register(DoorInteractionHandler.class);

        // Register network packets
        modEventBus.addListener(this::onRegisterPackets);

        // Register client events if on client
        if (FMLEnvironment.dist == Dist.CLIENT) {
            NeoForge.EVENT_BUS.register(ZombieHUD.class);
            NeoForge.EVENT_BUS.register(com.zombiemod.client.ClientTickHandler.class);
        }
    }

    private void onRegisterPackets(RegisterPayloadHandlersEvent event) {
        NetworkHandler.registerPackets(event);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        System.out.println("Zombie Mod Server Starting!");

        // Initialiser la configuration
        ZombieConfig.init(event.getServer().getServerDirectory().resolve("config").toFile());

        // Initialiser la configuration des drops
        ZombieDropsConfig.init(event.getServer().getServerDirectory().resolve("config").toFile());

        // Initialiser la configuration des mobs
        ZombieMobsConfig.init(event.getServer().getServerDirectory().resolve("config").toFile());

        // Initialiser le gestionnaire de maps
        MapManager.init(event.getServer().getServerDirectory().resolve("config").toFile());

        // Initialiser le tracker de jukeboxes
        ServerJukeboxTracker.initialize(event.getServer().overworld());

        // Initialiser le tracker de portes
        com.zombiemod.system.ServerDoorTracker.initialize(event.getServer().overworld());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        GameCommands.register(event.getDispatcher());
        SpawnCommand.register(event.getDispatcher());
        RespawnCommand.register(event.getDispatcher());
        DoorCommand.register(event.getDispatcher());
        WeaponCrateCommand.register(event.getDispatcher());
        ZombieJukeboxCommand.register(event.getDispatcher());
        ZombieMapCommand.register(event.getDispatcher());
        ZombieHelpCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        ServerLevel overworld = event.getServer().overworld();
        GameManager.tick(overworld);

        // Tick des animations de weapon crates
        com.zombiemod.system.WeaponCrateAnimationManager.tick(overworld);
    }
}

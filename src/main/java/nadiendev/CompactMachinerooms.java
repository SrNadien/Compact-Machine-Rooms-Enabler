package nadiendev.compactmachinerooms;

import com.mojang.logging.LogUtils;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;

@Mod(CompactMachinerooms.MOD_ID)
public class CompactMachinerooms {
    public static final String MOD_ID = "crm";
    public static final Logger LOGGER = LogUtils.getLogger();
    
    // Nombre exacto del datapack de Compact Machines
    private static final String CM_DATAPACK = "mod/compactmachines:data/compactmachines/datapacks/basic_templates";

    public CompactMachinerooms(IEventBus modEventBus) {
        LOGGER.info("Compact Room Templates Auto-Enabler iniciado");
        
        // Registrar eventos
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }
    
    /**
     * Se ejecuta cuando el servidor termina de cargar
     */
    private void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        PackRepository packRepository = server.getPackRepository();
        
        LOGGER.info("Verificando estado del datapack de Compact Machines...");
        
        // Verificar si el pack existe
        Pack pack = packRepository.getPack(CM_DATAPACK);
        
        if (pack == null) {
            LOGGER.warn("El datapack '{}' no fue encontrado. ¿Está Compact Machines instalado?", CM_DATAPACK);
            return;
        }
        
        // Verificar si ya está habilitado
        if (packRepository.getSelectedPacks().contains(pack)) {
            LOGGER.info("El datapack ya está habilitado: {}", CM_DATAPACK);
            return;
        }
        
        // Habilitar el datapack
        LOGGER.info("Habilitando automáticamente el datapack: {}", CM_DATAPACK);
        
        // Intentar agregar el pack
        var selectedIds = new java.util.ArrayList<>(packRepository.getSelectedIds());
        if (!selectedIds.contains(CM_DATAPACK)) {
            selectedIds.add(CM_DATAPACK);
            packRepository.setSelected(selectedIds);
            
            // Recargar para aplicar cambios
            server.reloadResources(packRepository.getSelectedIds()).thenRun(() -> {
                LOGGER.info("Datapack habilitado exitosamente. Recarga de recursos completada.");
                
                // Notificar a los jugadores
                server.getPlayerList().getPlayers().forEach(player -> {
                    player.sendSystemMessage(
                        Component.literal("§a[Compact Room Templates] Datapack habilitado automáticamente.")
                    );
                });
            }).exceptionally(throwable -> {
                LOGGER.error("Error al recargar recursos", throwable);
                return null;
            });
        }
    }
    
    /**
     * Comando para verificar el estado del datapack
     */
    private void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("crm")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("status")
                    .executes(context -> {
                        MinecraftServer server = context.getSource().getServer();
                        PackRepository packRepository = server.getPackRepository();
                        Pack pack = packRepository.getPack(CM_DATAPACK);
                        
                        if (pack == null) {
                            context.getSource().sendFailure(
                                Component.literal("§cDatapack no encontrado. ¿Está Compact Machines instalado?")
                            );
                            return 0;
                        }
                        
                        boolean enabled = packRepository.getSelectedPacks().contains(pack);
                        
                        if (enabled) {
                            context.getSource().sendSuccess(() -> 
                                Component.literal("§aDatapack '§b" + CM_DATAPACK + "§a' está §lHABILITADO"), 
                                false
                            );
                        } else {
                            context.getSource().sendSuccess(() -> 
                                Component.literal("§eDatapack '§b" + CM_DATAPACK + "§e' está §cDESHABILITADO"), 
                                false
                            );
                        }
                        
                        return 1;
                    })
                )
                .then(Commands.literal("enable")
                    .executes(context -> {
                        MinecraftServer server = context.getSource().getServer();
                        PackRepository packRepository = server.getPackRepository();
                        Pack pack = packRepository.getPack(CM_DATAPACK);
                        
                        if (pack == null) {
                            context.getSource().sendFailure(
                                Component.literal("§cDatapack no encontrado")
                            );
                            return 0;
                        }
                        
                        if (packRepository.getSelectedPacks().contains(pack)) {
                            context.getSource().sendSuccess(() -> 
                                Component.literal("§eEl datapack ya está habilitado"), 
                                false
                            );
                            return 1;
                        }
                        
                        // Habilitar datapack
                        var selectedIds = new java.util.ArrayList<>(packRepository.getSelectedIds());
                        selectedIds.add(CM_DATAPACK);
                        packRepository.setSelected(selectedIds);
                        
                        context.getSource().sendSuccess(() -> 
                            Component.literal("§aDatapack habilitado. Usa §b/reload§a para aplicar cambios."), 
                            true
                        );
                        
                        return 1;
                    })
                )
        );
    }
}
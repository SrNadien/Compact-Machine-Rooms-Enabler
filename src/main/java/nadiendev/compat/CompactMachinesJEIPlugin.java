package nadiendev.compactmachinerooms.compat;

import com.mojang.logging.LogUtils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

@JeiPlugin
public class CompactMachinesJEIPlugin implements IModPlugin {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath("crm", "jei_plugin");
    
    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }
    
    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        LOGGER.info("Registrando subtipos de Compact Machines para JEI...");
        
        try {
            // Registrar subtipos para new_machine usando componentes
            registration.registerSubtypeInterpreter(
                VanillaTypes.ITEM_STACK,
                // Obtener el item de Compact Machines
                net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("compactmachines", "new_machine")
                ),
                (itemStack, context) -> {
                    if (itemStack.isEmpty()) {
                        return "";
                    }
                    
                    // Crear subtipo basado en los componentes del item
                    StringBuilder subtypeBuilder = new StringBuilder();
                    
                    // Obtener componente de color de máquina
                    var colorComponent = itemStack.get(
                        net.minecraft.core.component.DataComponents.DYED_COLOR
                    );
                    if (colorComponent != null) {
                        subtypeBuilder.append("color:").append(colorComponent.rgb());
                    }
                    
                    // Obtener componente de room template
                    // Esto puede variar según cómo Compact Machines almacene el template
                    var customData = itemStack.getComponents();
                    customData.forEach(entry -> {
                        String key = entry.type().toString();
                        if (key.contains("room_template") || key.contains("machine")) {
                            subtypeBuilder.append(";").append(key).append(":").append(entry.value());
                        }
                    });
                    
                    String subtype = subtypeBuilder.toString();
                    LOGGER.debug("Subtipo generado para Compact Machine: {}", subtype);
                    return subtype.isEmpty() ? "default" : subtype;
                }
            );
            
            LOGGER.info("Subtipos de Compact Machines registrados exitosamente en JEI");
            
        } catch (Exception e) {
            LOGGER.error("Error registrando subtipos de Compact Machines en JEI", e);
        }
    }
}
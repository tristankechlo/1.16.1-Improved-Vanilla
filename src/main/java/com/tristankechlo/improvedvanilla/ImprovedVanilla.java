package com.tristankechlo.improvedvanilla;

import com.tristankechlo.improvedvanilla.commands.ImprovedVanillaCommand;
import com.tristankechlo.improvedvanilla.config.ConfigManager;
import com.tristankechlo.improvedvanilla.eventhandler.CropRightClickHandler;
import com.tristankechlo.improvedvanilla.eventhandler.EasyPlantingHandler;
import com.tristankechlo.improvedvanilla.eventhandler.MobDropHandler;
import com.tristankechlo.improvedvanilla.eventhandler.SpawnerHandler;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = ImprovedVanilla.MOD_ID, useMetadata = true, acceptableRemoteVersions = "*")
public class ImprovedVanilla {

    public static final String MOD_ID = "improvedvanilla";
    public static final String MOD_NAME = "Improved Vanilla";
    public final static Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public ImprovedVanilla() {
        MinecraftForge.EVENT_BUS.register(new CropRightClickHandler());
        MinecraftForge.EVENT_BUS.register(new EasyPlantingHandler());
        MinecraftForge.EVENT_BUS.register(new MobDropHandler());
        MinecraftForge.EVENT_BUS.register(new SpawnerHandler());
    }

    @EventHandler
    public void start(FMLServerStartingEvent event) {
        ConfigManager.loadAndVerifyConfig();

        event.registerServerCommand(new ImprovedVanillaCommand());
        ImprovedVanilla.LOGGER.info("command /{} registered", ImprovedVanilla.MOD_ID);
    }

    public static ItemStack getMonsterEgg(String id) {
        if (!EntityList.ENTITY_EGGS.containsKey(new ResourceLocation(id))) {
            LOGGER.warn("Did not find a registered spawn-egg for '{}', item might not be usable", id);
        }
        // Create ItemStack with unspecified Spawn Egg
        ItemStack itemStack = new ItemStack(Items.SPAWN_EGG);

        // Do some NBT work to specify entity type
        NBTTagCompound nbttagcompound = itemStack.hasTagCompound() ? itemStack.getTagCompound() : new NBTTagCompound();
        NBTTagCompound nbttagcompound1 = new NBTTagCompound();
        nbttagcompound1.setString("id", id);
        nbttagcompound.setTag("EntityTag", nbttagcompound1);

        // Set new NBT Data to Item Stack
        itemStack.setTagCompound(nbttagcompound);
        return itemStack;
    }

}

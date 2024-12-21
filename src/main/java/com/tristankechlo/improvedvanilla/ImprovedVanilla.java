package com.tristankechlo.improvedvanilla;

import com.tristankechlo.improvedvanilla.commands.ImprovedVanillaCommand;
import com.tristankechlo.improvedvanilla.config.util.ConfigManager;
import com.tristankechlo.improvedvanilla.eventhandler.CropRightClickHandler;
import com.tristankechlo.improvedvanilla.eventhandler.EasyPlantingHandler;
import com.tristankechlo.improvedvanilla.eventhandler.MobDropHandler;
import com.tristankechlo.improvedvanilla.eventhandler.SpawnerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ImprovedVanilla.MOD_ID)
public class ImprovedVanilla {

    public static final String MOD_ID = "improvedvanilla";
    public static final String MOD_NAME = "Improved Vanilla";
    public final static Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public ImprovedVanilla() {
        MinecraftForge.EVENT_BUS.register(new CropRightClickHandler());
        MinecraftForge.EVENT_BUS.register(new EasyPlantingHandler());
        MinecraftForge.EVENT_BUS.register(new MobDropHandler());
        MinecraftForge.EVENT_BUS.register(new SpawnerHandler());

        // register commands
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);

        // setup configs
        MinecraftForge.EVENT_BUS.addListener(this::commonSetup);

        // make sure the mod being absent on the other network side does not cause the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }

    // setup configs
    private void commonSetup(final ServerAboutToStartEvent event) {
        ConfigManager.loadAndVerifyConfig();
    }

    // register commands
    private void registerCommands(final RegisterCommandsEvent event) {
        ImprovedVanillaCommand.register(event.getDispatcher());
    }

    public static ItemStack getMonsterEgg(String id, int count) {
        final ResourceLocation search = new ResourceLocation(id + "_spawn_egg");
        Item item = ForgeRegistries.ITEMS.getValue(search);
        if (item == null || item == Items.AIR) {
            LOGGER.warn("Did not find a spawn-egg for '{}', searched for '{}'", id, search);
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, count);
    }

    public static void dropItemStackInWorld(Level level, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        double d0 = (double) (level.random.nextFloat() * 0.5F) + 0.25;
        double d1 = (double) (level.random.nextFloat() * 0.5F) + 0.25;
        double d2 = (double) (level.random.nextFloat() * 0.5F) + 0.25;
        ItemEntity itementity = new ItemEntity(level, d0 + pos.getX(), d1 + pos.getY(), d2 + pos.getZ(), stack);
        itementity.setDefaultPickUpDelay();
        level.addFreshEntity(itementity);
    }

}

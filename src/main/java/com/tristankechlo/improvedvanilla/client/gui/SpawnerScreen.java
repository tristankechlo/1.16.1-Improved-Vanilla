package com.tristankechlo.improvedvanilla.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tristankechlo.improvedvanilla.ImprovedVanilla;
import com.tristankechlo.improvedvanilla.container.SpawnerContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpawnerScreen extends ContainerScreen<SpawnerContainer> {
	
    private ResourceLocation GUI = new ResourceLocation(ImprovedVanilla.MOD_ID, "textures/gui/spawner.png");
    private int textureXSize = 176;
    private int textureYSize = 166;

    public SpawnerScreen(SpawnerContainer container, PlayerInventory inv, ITextComponent name) {
        super(container, inv, name);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.func_230459_a_(matrixStack, mouseX, mouseY);
    }

	@Override
	protected void func_230450_a_(MatrixStack matrixstack, float p_230450_2_, int p_230450_3_, int p_230450_4_) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI);
        int relX = (this.width  - this.xSize) / 2;
        int relY = (this.height   - this.ySize) / 2;
        blit(matrixstack, relX, relY, 0, 0, this.xSize, this.ySize, this.textureXSize, this.textureYSize);
	}
}

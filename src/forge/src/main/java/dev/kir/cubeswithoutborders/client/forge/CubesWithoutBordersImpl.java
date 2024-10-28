package dev.kir.cubeswithoutborders.client.forge;

import dev.kir.cubeswithoutborders.client.compat.cloth.ClothConfigScreen;
import dev.kir.cubeswithoutborders.client.config.CubesWithoutBordersConfig;
import dev.kir.cubeswithoutborders.client.util.forge.ModLoaderUtilImpl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod(ModLoaderUtilImpl.MOD_ID)
public final class CubesWithoutBordersImpl {
    public CubesWithoutBordersImpl() {
        CubesWithoutBordersImpl.registerConfigScreen();
    }

    private static void registerConfigScreen() {
        if (
            !ModList.get().isLoaded("cloth_config") &&
            !ModList.get().isLoaded("cloth_config2") &&
            !ModList.get().isLoaded("cloth-config")
        ) {
            return;
        }

        ModLoadingContext.get().registerExtensionPoint(
            ExtensionPoint.CONFIGGUIFACTORY,
            () -> (__, parent) -> ClothConfigScreen.create(
                CubesWithoutBordersConfig.getInstance(),
                ModLoaderUtilImpl.MOD_ID.replace("_", "-"),
                parent
            )
        );
    }
}

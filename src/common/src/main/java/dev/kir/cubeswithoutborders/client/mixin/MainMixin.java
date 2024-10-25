package dev.kir.cubeswithoutborders.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import dev.kir.cubeswithoutborders.client.FullscreenMode;
import dev.kir.cubeswithoutborders.client.config.CubesWithoutBordersConfig;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.RunArgs;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Main.class)
abstract class MainMixin {
    @ModifyReceiver(method = { "main" }, at = @At(value = "INVOKE", target = "Ljoptsimple/OptionParser;parse([Ljava/lang/String;)Ljoptsimple/OptionSet;"), require = 0)
    private static OptionParser allowBorderlessOption(OptionParser parser, String[] args) {
        parser.accepts("borderless");
        return parser;
    }

    @Inject(method = { "main" }, at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;beginInitialization()V", ordinal = 0), require = 0)
    private static void readBorderlessOption(CallbackInfo ci, @Local(ordinal = 0) OptionSet options) {
        boolean isBorderless = options.has("borderless");
        if (!isBorderless) {
            return;
        }

        CubesWithoutBordersConfig config = CubesWithoutBordersConfig.getInstance();
        config.setFullscreenMode(FullscreenMode.BORDERLESS);
    }
}

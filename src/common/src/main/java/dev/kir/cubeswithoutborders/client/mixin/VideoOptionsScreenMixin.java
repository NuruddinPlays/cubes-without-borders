package dev.kir.cubeswithoutborders.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.kir.cubeswithoutborders.client.FullscreenManager;
import dev.kir.cubeswithoutborders.client.FullscreenMode;
import dev.kir.cubeswithoutborders.client.config.CubesWithoutBordersConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.VideoOptionsScreen;
import net.minecraft.client.gui.widget.ButtonListWidget;
import net.minecraft.client.option.BooleanOption;
import net.minecraft.client.option.CyclingOption;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.Option;
import net.minecraft.client.util.Window;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Environment(EnvType.CLIENT)
@Mixin(VideoOptionsScreen.class)
abstract class VideoOptionsScreenMixin extends GameOptionsScreen {
    private VideoOptionsScreenMixin(Screen parent, GameOptions gameOptions, Text title) {
        super(parent, gameOptions, title);
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void applyVideoMode(CallbackInfo ci) {
        // In cases where a user presses ESC to close this screen without
        // clicking on "Done" first, video mode changes won't be applied.
        //
        // See:
        //  - https://bugs.mojang.com/browse/MC-175437
        Window window = this.client == null ? null : this.client.getWindow();
        if (window != null) {
            window.applyVideoMode();
        }
    }

    @WrapOperation(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonListWidget;addAll([Lnet/minecraft/client/option/Option;)V", ordinal = 0))
    private void patchFullscreenOption(ButtonListWidget widget, Option[] widgetOptions, Operation<Void> addAll) {
        CubesWithoutBordersConfig config = CubesWithoutBordersConfig.getInstance();
        if (config.getBorderlessFullscreenType() == config.getFullscreenType()) {
            // If the user changes both regular fullscreen and borderless to use
            // the exact same underlying logic, there's no need to provide access
            // to the "Borderless" option, as it becomes meaningless in this context.
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        FullscreenManager window = (FullscreenManager)(Object)client.getWindow();
        Option[] options = Arrays.copyOf(widgetOptions, widgetOptions.length);
        BooleanOption booleanFullscreenOption = Option.FULLSCREEN;
        CyclingOption enumFullscreenOption = new CyclingOption(
            "options.fullscreen",
            (gameOptions, amount) -> {
                if (window == null) {
                    return;
                }

                FullscreenMode value = FullscreenMode.get(window.getFullscreenMode().getId() + amount);
                if (value == window.getFullscreenMode()) {
                    return;
                }

                window.setFullscreenMode(value);
                gameOptions.fullscreen = window.getFullscreenMode() != FullscreenMode.OFF;
            },
            (gameOptions, option) -> {
                FullscreenMode value = window == null ? FullscreenMode.OFF : window.getFullscreenMode();
                MutableText fullscreenText = new TranslatableText("options.fullscreen");
                MutableText valueText = new TranslatableText(value.getTranslationKey());
                return fullscreenText.append(": ").append(valueText);
            }
        );

        for (int i = 0; i < options.length; i++) {
            if (options[i] == booleanFullscreenOption) {
                options[i] = enumFullscreenOption;
                break;
            }
        }

        addAll.call(widget, options);
    }
}

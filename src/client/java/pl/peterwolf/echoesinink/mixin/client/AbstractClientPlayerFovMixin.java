package pl.peterwolf.echoesinink.mixin.client;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.peterwolf.echoesinink.item.ModItems;

/**
 * Force FOV zoom while holding the magnifying lens (spyglass-equivalent).
 * Complements {@code Player#isScoping} in case FOV uses a separate client path.
 */
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerFovMixin {
	@Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
	private void echoesInInk$magnifyingLensFov(CallbackInfoReturnable<Float> cir) {
		AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
		if (!self.isUsingItem()) {
			return;
		}
		ItemStack use = self.getUseItem();
		if (!use.is(ModItems.MAGNIFYING_LENS)) {
			return;
		}
		// Spyglass uses ~0.1; keep a readable but clear zoom for the lens.
		float current = cir.getReturnValue();
		cir.setReturnValue(Math.min(current, 0.12F));
	}
}

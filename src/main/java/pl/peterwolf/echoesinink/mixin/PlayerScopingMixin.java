package pl.peterwolf.echoesinink.mixin;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pl.peterwolf.echoesinink.item.ModItems;

/**
 * Treat the magnifying lens like a spyglass while the player is holding use,
 * so FOV zoom and spyglass overlay apply.
 */
@Mixin(Player.class)
public abstract class PlayerScopingMixin {
	@Inject(method = "isScoping", at = @At("HEAD"), cancellable = true)
	private void echoesInInk$magnifyingLensScope(CallbackInfoReturnable<Boolean> cir) {
		Player self = (Player) (Object) this;
		if (!self.isUsingItem()) {
			return;
		}
		ItemStack stack = self.getUseItem();
		if (stack.is(ModItems.MAGNIFYING_LENS)) {
			cir.setReturnValue(true);
		}
	}
}

package pl.peterwolf.echoesinink.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import pl.peterwolf.echoesinink.block.TypeCabinetBlock;
import pl.peterwolf.echoesinink.block.entity.TypeCabinetBlockEntity;
import pl.peterwolf.echoesinink.item.ModItems;

/**
 * Draws four thin drawers and makes stored items clearly visible — large and
 * floating above an open drawer, with a peek icon on closed occupied drawers.
 */
public class TypeCabinetRenderer implements BlockEntityRenderer<TypeCabinetBlockEntity, TypeCabinetRenderState> {
	private static final float MAX_PULL = 0.62F;
	/** Top → bottom drawer center Y. */
	private static final float[] DRAWER_Y = {
		13.5F / 16.0F,
		9.5F / 16.0F,
		5.5F / 16.0F,
		1.75F / 16.0F
	};

	private final ItemModelResolver itemModelResolver;

	public TypeCabinetRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModelResolver = context.itemModelResolver();
	}

	@Override
	public TypeCabinetRenderState createRenderState() {
		return new TypeCabinetRenderState();
	}

	@Override
	public void extractRenderState(
		TypeCabinetBlockEntity be,
		TypeCabinetRenderState state,
		float partialTick,
		Vec3 cameraPos,
		@Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress
	) {
		BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPos, breakProgress);
		state.facing = be.getBlockState().getValue(TypeCabinetBlock.FACING);
		int open = be.getBlockState().getValue(TypeCabinetBlock.OPEN_DRAWER);
		state.openDrawer = open <= 0 ? -1 : open - 1;
		float target = open <= 0 ? 0.0F : 1.0F;
		state.openAnim = Mth.clamp(Mth.lerp(0.45F, be.openAnim(), target), 0.0F, 1.0F);
		Level level = be.getLevel();
		int seed = be.getBlockPos().hashCode();
		for (int i = 0; i < TypeCabinetBlockEntity.DRAWER_COUNT; i++) {
			ItemStack stack = be.getItem(i).copy();
			state.drawerStacks[i] = stack;
			state.drawerItemStates[i] = resolveItem(stack, level, seed + i * 17);
		}
	}

	@Override
	public void submit(
		TypeCabinetRenderState state,
		PoseStack poseStack,
		SubmitNodeCollector collector,
		CameraRenderState camera
	) {
		poseStack.pushPose();
		poseStack.translate(0.5F, 0.0F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));

		ItemStackRenderState panel = resolveItem(new ItemStack(ModItems.WOODEN_PRINTING_MATRIX), null, 7);

		for (int i = 0; i < TypeCabinetBlockEntity.DRAWER_COUNT; i++) {
			boolean isOpen = state.openDrawer == i;
			float pull = isOpen ? state.openAnim * MAX_PULL : 0.0F;
			// Front is local -Z after facing rotation.
			float z = -0.28F - pull;

			// Drawer face / floor panel
			if (panel != null) {
				poseStack.pushPose();
				poseStack.translate(0.0F, DRAWER_Y[i], z);
				poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
				poseStack.scale(0.78F, isOpen ? 0.72F : 0.48F, 0.12F);
				panel.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
				poseStack.popPose();
			}

			boolean hasItem = state.drawerItemStates[i] != null
				&& state.drawerStacks[i] != null
				&& !state.drawerStacks[i].isEmpty();
			if (!hasItem) {
				continue;
			}

			// Open drawer: large, readable item sitting on the pulled tray.
			// Closed drawer: small peek icon on the front so occupancy is visible.
			poseStack.pushPose();
			if (isOpen && state.openAnim > 0.15F) {
				poseStack.translate(0.0F, DRAWER_Y[i] + 0.08F, z - 0.06F);
				poseStack.mulPose(Axis.XP.rotationDegrees(75.0F));
				float s = 0.55F;
				poseStack.scale(s, s, s);
			} else {
				// Peek on drawer front
				poseStack.translate(0.0F, DRAWER_Y[i] + 0.02F, -0.36F);
				poseStack.scale(0.28F, 0.28F, 0.28F);
			}
			state.drawerItemStates[i].submit(
				poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0
			);
			poseStack.popPose();
		}

		poseStack.popPose();
	}

	@Nullable
	private ItemStackRenderState resolveItem(ItemStack stack, @Nullable Level level, int seed) {
		if (stack.isEmpty()) {
			return null;
		}
		ItemStackRenderState itemRenderState = new ItemStackRenderState();
		itemModelResolver.updateForTopItem(
			itemRenderState,
			stack,
			ItemDisplayContext.FIXED,
			level,
			null,
			seed
		);
		return itemRenderState.isEmpty() ? null : itemRenderState;
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 48;
	}
}

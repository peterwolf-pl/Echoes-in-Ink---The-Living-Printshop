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
 * Draws four thin drawers sliding out of the cabinet body and any stored stack.
 */
public class TypeCabinetRenderer implements BlockEntityRenderer<TypeCabinetBlockEntity, TypeCabinetRenderState> {
	private static final float MAX_PULL = 0.52F;
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
		state.openAnim = Mth.clamp(Mth.lerp(0.35F, be.openAnim(), target), 0.0F, 1.0F);
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

		// Drawer fronts use a wooden matrix as a flat panel stand-in.
		ItemStackRenderState panel = resolveItem(
			new ItemStack(ModItems.WOODEN_PRINTING_MATRIX),
			null,
			7
		);

		for (int i = 0; i < TypeCabinetBlockEntity.DRAWER_COUNT; i++) {
			float pull = (state.openDrawer == i) ? state.openAnim * MAX_PULL : 0.0F;
			// Front of cabinet is local -Z after facing rotation (model faces north).
			float z = -0.30F - pull;

			// Thin drawer slab
			if (panel != null) {
				poseStack.pushPose();
				poseStack.translate(0.0F, DRAWER_Y[i], z);
				poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
				poseStack.scale(0.72F, 0.55F, 0.10F);
				panel.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
				poseStack.popPose();
			}

			// Contents resting in the drawer
			if (state.drawerItemStates[i] != null
				&& state.drawerStacks[i] != null
				&& !state.drawerStacks[i].isEmpty()) {
				poseStack.pushPose();
				poseStack.translate(0.0F, DRAWER_Y[i] + 0.04F, z + 0.02F);
				poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
				poseStack.scale(0.32F, 0.32F, 0.32F);
				state.drawerItemStates[i].submit(
					poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0
				);
				poseStack.popPose();
			}
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
}

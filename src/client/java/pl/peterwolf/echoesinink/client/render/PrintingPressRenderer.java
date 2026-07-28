package pl.peterwolf.echoesinink.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import pl.peterwolf.echoesinink.block.PressPhase;
import pl.peterwolf.echoesinink.block.PrintingPressBlock;
import pl.peterwolf.echoesinink.block.entity.PrintingPressBlockEntity;
import pl.peterwolf.echoesinink.item.ModItems;

/**
 * Client-only animation: carriage slide, platen descent, handle angle.
 * Server phase + animProgress are the sole authority; this only interpolates.
 */
public class PrintingPressRenderer implements BlockEntityRenderer<PrintingPressBlockEntity, PrintingPressRenderState> {
	private final ItemStackRenderState itemState = new ItemStackRenderState();

	public PrintingPressRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public PrintingPressRenderState createRenderState() {
		return new PrintingPressRenderState();
	}

	@Override
	public void extractRenderState(
		PrintingPressBlockEntity be,
		PrintingPressRenderState state,
		float partialTick,
		Vec3 cameraPos,
		@Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress
	) {
		BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPos, breakProgress);
		state.facing = be.getBlockState().getValue(PrintingPressBlock.FACING);
		state.phase = be.phase();
		state.animProgress = be.animProgress();
		state.hasScrew = be.hasScrew();
		state.hasHandle = be.hasHandle();
		state.hasPlaten = be.hasPlaten();
		state.hasCarriage = be.hasCarriage();
		state.matrix = be.getItem(PrintingPressBlockEntity.SLOT_MATRIX).copy();
		state.paper = be.getItem(PrintingPressBlockEntity.SLOT_PAPER).copy();
		state.output = be.getItem(PrintingPressBlockEntity.SLOT_OUTPUT).copy();
	}

	@Override
	public void submit(
		PrintingPressRenderState state,
		PoseStack poseStack,
		SubmitNodeCollector collector,
		CameraRenderState camera
	) {
		poseStack.pushPose();
		// Center of block, facing-aware.
		poseStack.translate(0.5F, 0.0F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));

		float carriageZ = carriageOffset(state);
		float platenY = platenOffset(state);
		float handleAngle = handleAngle(state);

		// Carriage (pressure plate look-alike)
		if (state.hasCarriage) {
			poseStack.pushPose();
			poseStack.translate(0.0F, 0.2F, carriageZ);
			poseStack.scale(0.7F, 0.12F, 0.55F);
			submitItem(poseStack, collector, state, new ItemStack(ModItems.PRESS_CARRIAGE), state.lightCoords);
			poseStack.popPose();
		}

		// Matrix / paper on carriage
		ItemStack bedItem = !state.output.isEmpty()
			? state.output
			: (!state.paper.isEmpty() ? state.paper : state.matrix);
		if (!bedItem.isEmpty()) {
			poseStack.pushPose();
			poseStack.translate(0.0F, 0.28F, carriageZ);
			poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			poseStack.scale(0.45F, 0.45F, 0.45F);
			submitItem(poseStack, collector, state, bedItem, state.lightCoords);
			poseStack.popPose();
		}

		// Platen
		if (state.hasPlaten) {
			poseStack.pushPose();
			poseStack.translate(0.0F, 0.55F + platenY, 0.0F);
			poseStack.scale(0.65F, 0.1F, 0.5F);
			submitItem(poseStack, collector, state, new ItemStack(ModItems.PRESS_PLATEN), state.lightCoords);
			poseStack.popPose();
		}

		// Screw column
		if (state.hasScrew) {
			poseStack.pushPose();
			poseStack.translate(0.0F, 0.75F + platenY * 0.5F, 0.0F);
			poseStack.mulPose(Axis.XP.rotationDegrees(handleAngle * 0.25F));
			poseStack.scale(0.2F, 0.55F, 0.2F);
			submitItem(poseStack, collector, state, new ItemStack(ModItems.PRESS_SCREW), state.lightCoords);
			poseStack.popPose();
		}

		// Handle
		if (state.hasHandle) {
			poseStack.pushPose();
			poseStack.translate(0.35F, 0.85F + platenY * 0.3F, 0.0F);
			poseStack.mulPose(Axis.ZP.rotationDegrees(handleAngle));
			poseStack.scale(0.55F, 0.12F, 0.12F);
			submitItem(poseStack, collector, state, new ItemStack(ModItems.PRESS_HANDLE), state.lightCoords);
			poseStack.popPose();
		}

		poseStack.popPose();
	}

	private static float carriageOffset(PrintingPressRenderState state) {
		return switch (state.phase) {
			case CARRIAGE_IN, PRESSING, RESETTING, IMPRESSION_DONE -> 0.0F;
			case OUTPUT_READY -> 0.35F;
			default -> 0.4F; // out
		};
	}

	private static float platenOffset(PrintingPressRenderState state) {
		if (state.phase == PressPhase.PRESSING) {
			return -0.18F * Mth.clamp(state.animProgress, 0.0F, 1.0F);
		}
		if (state.phase == PressPhase.RESETTING) {
			return -0.18F * Mth.clamp(state.animProgress, 0.0F, 1.0F);
		}
		if (state.phase == PressPhase.IMPRESSION_DONE || state.phase == PressPhase.OUTPUT_READY) {
			return 0.0F;
		}
		return 0.0F;
	}

	private static float handleAngle(PrintingPressRenderState state) {
		if (state.phase == PressPhase.PRESSING) {
			return 70.0F * Mth.clamp(state.animProgress, 0.0F, 1.0F);
		}
		if (state.phase == PressPhase.RESETTING) {
			return 70.0F * Mth.clamp(state.animProgress, 0.0F, 1.0F);
		}
		return 0.0F;
	}

	private void submitItem(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		PrintingPressRenderState state,
		ItemStack stack,
		int light
	) {
		itemState.clear();
		Minecraft.getInstance().getItemModelResolver().updateForTopItem(
			itemState,
			stack,
			ItemDisplayContext.FIXED,
			null,
			null,
			0
		);
		itemState.submit(poseStack, collector, light, 0, 0);
	}
}

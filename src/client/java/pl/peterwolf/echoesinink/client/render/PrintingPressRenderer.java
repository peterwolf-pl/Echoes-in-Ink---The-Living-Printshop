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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import pl.peterwolf.echoesinink.block.PressPhase;
import pl.peterwolf.echoesinink.block.PrintingPressBlock;
import pl.peterwolf.echoesinink.block.entity.PrintingPressBlockEntity;
import pl.peterwolf.echoesinink.item.ModItems;

/**
 * Client animation of press parts. Server phase/progress is authoritative;
 * this only presents motion. Always draws something visible once parts exist.
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
		// Smooth during PRESSING/RESETTING using partial ticks when possible.
		float base = be.animProgress();
		if (be.phase() == PressPhase.PRESSING && be.maxProgress() > 0) {
			base = (be.progress() + partialTick) / (float) be.maxProgress();
		} else if (be.phase() == PressPhase.RESETTING) {
			base = 1.0F - (be.progress() + partialTick) / 20.0F;
		}
		state.animProgress = Mth.clamp(base, 0.0F, 1.0F);
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
		poseStack.translate(0.5F, 0.0F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));

		float carriageZ = carriageOffset(state);
		float platenY = platenOffset(state);
		float handleAngle = handleAngle(state);

		// Always draw a visible iron frame so the press never looks like bare wood alone.
		submitItem(poseStack, collector, state, new ItemStack(ModItems.PRESS_SCREW),
			0.0F, 0.95F, 0.0F, 0.35F, 0.9F, 0.35F, 0.0F);

		if (state.hasCarriage || state.phase != PressPhase.INCOMPLETE) {
			submitItem(poseStack, collector, state, new ItemStack(ModItems.PRESS_CARRIAGE),
				0.0F, 0.22F, carriageZ, 0.85F, 0.18F, 0.7F, 0.0F);
		}

		ItemStack bed = !state.output.isEmpty() ? state.output
			: (!state.paper.isEmpty() ? state.paper
			: (!state.matrix.isEmpty() ? state.matrix : ItemStack.EMPTY));
		if (!bed.isEmpty()) {
			poseStack.pushPose();
			poseStack.translate(0.0F, 0.32F, carriageZ);
			poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			poseStack.scale(0.55F, 0.55F, 0.55F);
			draw(poseStack, collector, state, bed);
			poseStack.popPose();
		}

		if (state.hasPlaten || state.phase != PressPhase.INCOMPLETE) {
			submitItem(poseStack, collector, state, new ItemStack(ModItems.PRESS_PLATEN),
				0.0F, 0.58F + platenY, 0.0F, 0.8F, 0.14F, 0.65F, 0.0F);
		}

		if (state.hasScrew || state.phase != PressPhase.INCOMPLETE) {
			submitItem(poseStack, collector, state, new ItemStack(ModItems.PRESS_SCREW),
				0.0F, 0.78F + platenY * 0.4F, 0.0F, 0.25F, 0.7F, 0.25F, handleAngle * 0.3F);
		}

		if (state.hasHandle || state.phase != PressPhase.INCOMPLETE) {
			poseStack.pushPose();
			poseStack.translate(0.42F, 0.88F + platenY * 0.25F, 0.0F);
			poseStack.mulPose(Axis.ZP.rotationDegrees(-handleAngle));
			poseStack.scale(0.75F, 0.18F, 0.18F);
			draw(poseStack, collector, state, new ItemStack(ModItems.PRESS_HANDLE));
			poseStack.popPose();
		}

		poseStack.popPose();
	}

	private void submitItem(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		PrintingPressRenderState state,
		ItemStack stack,
		float x, float y, float z,
		float sx, float sy, float sz,
		float rotX
	) {
		poseStack.pushPose();
		poseStack.translate(x, y, z);
		if (rotX != 0.0F) {
			poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
		}
		poseStack.scale(sx, sy, sz);
		draw(poseStack, collector, state, stack);
		poseStack.popPose();
	}

	private void draw(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		PrintingPressRenderState state,
		ItemStack stack
	) {
		if (stack.isEmpty()) {
			return;
		}
		itemState.clear();
		Level level = Minecraft.getInstance().level;
		Minecraft.getInstance().getItemModelResolver().updateForTopItem(
			itemState,
			stack,
			ItemDisplayContext.FIXED,
			level,
			null,
			42
		);
		if (!itemState.isEmpty()) {
			itemState.submit(poseStack, collector, state.lightCoords, 0, 0);
		}
	}

	private static float carriageOffset(PrintingPressRenderState state) {
		return switch (state.phase) {
			case CARRIAGE_IN, PRESSING, RESETTING, IMPRESSION_DONE -> 0.05F;
			case OUTPUT_READY -> 0.32F;
			default -> 0.42F;
		};
	}

	private static float platenOffset(PrintingPressRenderState state) {
		if (state.phase == PressPhase.PRESSING) {
			return -0.22F * state.animProgress;
		}
		if (state.phase == PressPhase.RESETTING) {
			return -0.22F * state.animProgress;
		}
		return 0.0F;
	}

	private static float handleAngle(PrintingPressRenderState state) {
		if (state.phase == PressPhase.PRESSING || state.phase == PressPhase.RESETTING) {
			return 75.0F * state.animProgress;
		}
		return 0.0F;
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 64;
	}
}

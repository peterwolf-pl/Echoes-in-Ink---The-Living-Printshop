package pl.peterwolf.echoesinink.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import pl.peterwolf.echoesinink.block.LaidPaperBlock;
import pl.peterwolf.echoesinink.block.entity.LaidPaperBlockEntity;
import pl.peterwolf.echoesinink.item.ModItems;
import pl.peterwolf.echoesinink.util.PrintedContent;

/**
 * Renders a laid workshop object: sheets flat (like the press), matrices flat,
 * machine parts as small 3D props on the surface.
 */
public class LaidPaperRenderer implements BlockEntityRenderer<LaidPaperBlockEntity, LaidPaperRenderState> {
	private static final float SHEET_Y = 0.04F;
	private static final float SHEET_SCALE = 0.72F;
	private static final float MATRIX_SCALE = 0.68F;
	private static final float PART_Y = 0.08F;
	private static final float PART_SCALE = 0.55F;
	private static final float TEXT_Y = 0.055F;
	private static final float MAX_TEXT_SCALE = 0.0042F;
	private static final float TEXT_WIDTH = 0.4F;

	private final ItemModelResolver itemModelResolver;
	private final Font font;

	public LaidPaperRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModelResolver = context.itemModelResolver();
		this.font = context.font();
	}

	@Override
	public LaidPaperRenderState createRenderState() {
		return new LaidPaperRenderState();
	}

	@Override
	public void extractRenderState(
		LaidPaperBlockEntity be,
		LaidPaperRenderState state,
		float partialTick,
		Vec3 cameraPos,
		@Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress
	) {
		BlockEntityRenderer.super.extractRenderState(be, state, partialTick, cameraPos, breakProgress);
		state.facing = be.getBlockState().getValue(LaidPaperBlock.FACING);
		state.page = be.page().copy();
		state.pageRenderState = resolveItem(state.page, be.getLevel(), be.getBlockPos().hashCode());
	}

	@Override
	public void submit(
		LaidPaperRenderState state,
		PoseStack poseStack,
		SubmitNodeCollector collector,
		CameraRenderState camera
	) {
		poseStack.pushPose();
		poseStack.translate(0.5F, 0.0F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));

		if (state.pageRenderState != null) {
			if (isFlatLaid(state.page)) {
				poseStack.pushPose();
				poseStack.translate(0.0F, SHEET_Y, 0.0F);
				poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
				float scale = isMatrixForm(state.page) ? MATRIX_SCALE : SHEET_SCALE;
				poseStack.scale(scale, scale, scale);
				state.pageRenderState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
				poseStack.popPose();
			} else {
				// Press parts / bulky tools sit upright on the surface.
				poseStack.pushPose();
				poseStack.translate(0.0F, PART_Y, 0.0F);
				poseStack.scale(PART_SCALE, PART_SCALE, PART_SCALE);
				state.pageRenderState.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
				poseStack.popPose();
			}
		}

		String suffix = PrintedContent.impressionSuffix(state.page);
		if (suffix != null) {
			submitImpressionText(poseStack, collector, state, suffix);
		}

		poseStack.popPose();
	}

	private static boolean isMatrixForm(ItemStack stack) {
		Item item = stack.getItem();
		return item == ModItems.WOODEN_PRINTING_MATRIX
			|| item == ModItems.VILLAGE_CHRONICLE_MATRIX
			|| item == ModItems.FORBIDDEN_NOTICE_FORME
			|| item == ModItems.METAL_TYPE_PIECE
			|| item == ModItems.LEAD_TYPE_SET
			|| item == ModItems.IRON_CHASE
			|| item == ModItems.MISSING_HEADLINE_TYPE
			|| item == ModItems.CHARCOAL_RUBBING;
	}

	private static boolean isFlatLaid(ItemStack stack) {
		Item item = stack.getItem();
		if (isMatrixForm(stack)) {
			return true;
		}
		// Sheets / notes / pages
		return item == net.minecraft.world.item.Items.PAPER
			|| item == ModItems.BLANK_ARCHIVE_PAGE
			|| item == ModItems.DAMAGED_ARCHIVE_PAGE
			|| item == ModItems.PRINTERS_NOTES
			|| item == ModItems.PRINTERS_INSTRUCTION_SHEET
			|| item == ModItems.WORKSHOP_MAP_FRAGMENT
			|| item == ModItems.DECORATIVE_WOODCUT
			|| item == ModItems.PRINTED_WARNING_POSTER
			|| item == ModItems.RESTORED_CHRONICLE_PAGE
			|| item == ModItems.VILLAGE_CHRONICLE_PRINT
			|| item == ModItems.FORBIDDEN_NOTICE_PRINT
			|| PrintedContent.impressionSuffix(stack) != null;
	}

	private void submitImpressionText(
		PoseStack poseStack,
		SubmitNodeCollector collector,
		LaidPaperRenderState state,
		String suffix
	) {
		FormattedCharSequence[] lines = new FormattedCharSequence[3];
		int widestLine = 1;
		for (int i = 0; i < lines.length; i++) {
			lines[i] = Component.translatable(
				"press.echoes_in_ink.impression." + suffix + "." + (i + 1)
			).getVisualOrderText();
			widestLine = Math.max(widestLine, font.width(lines[i]));
		}
		float textScale = Math.min(MAX_TEXT_SCALE, TEXT_WIDTH / widestLine);

		poseStack.pushPose();
		poseStack.translate(0.0F, TEXT_Y, 0.0F);
		poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		poseStack.scale(textScale, textScale, textScale);

		for (int i = 0; i < lines.length; i++) {
			FormattedCharSequence line = lines[i];
			float x = -font.width(line) / 2.0F;
			float y = -10.0F + i * 10.0F;
			collector.submitText(
				poseStack,
				x,
				y,
				line,
				false,
				Font.DisplayMode.POLYGON_OFFSET,
				state.lightCoords,
				0xFF18130F,
				0,
				0
			);
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

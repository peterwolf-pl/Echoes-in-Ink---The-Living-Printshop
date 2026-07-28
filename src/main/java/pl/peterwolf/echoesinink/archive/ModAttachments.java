package pl.peterwolf.echoesinink.archive;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import pl.peterwolf.echoesinink.EchoesInInk;

public final class ModAttachments {
	public static final AttachmentType<PlayerArchive> PLAYER_ARCHIVE = AttachmentRegistry.create(
		EchoesInInk.id("player_archive"),
		builder -> builder
			.persistent(PlayerArchive.CODEC)
			.initializer(PlayerArchive::new)
			.copyOnDeath()
			.syncWith(PlayerArchive.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
	);

	private ModAttachments() {}

	public static void init() {
		// static registration
	}
}

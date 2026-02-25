package btw.lowercase.template;

import dev.kikugie.fletching_table.annotation.fabric.Entrypoint;
import net.fabricmc.api.ClientModInitializer;

@Entrypoint
public final class TemplateFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		System.out.println("Hello from lowercasebtw stonecutter template!");
	}
}
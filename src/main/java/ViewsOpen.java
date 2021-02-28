import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.jetty.SlackAppServer;
import com.slack.api.methods.SlackApiException;

import java.io.IOException;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.element.BlockElements.asContextElements;
import static com.slack.api.model.view.Views.*;

public class ViewsOpen {

    public static void main(String[] args) throws Exception {
        var config = new AppConfig();
        config.setSingleTeamBotToken(System.getenv("SLACK_BOT_TOKEN"));
        config.setSigningSecret(System.getenv("SLACK_SIGNING_SECRET"));
        var app = new App(config); // `new App()` does the same

        app.globalShortcut("open_modal", (req, ctx) -> {
            var logger = ctx.logger;
            try {
                var payload = req.getPayload();
                // Call the conversations.create method using the built-in WebClient
                var modalView = view(v -> v
                        .type("modal")
                        .title(viewTitle(vt -> vt.type("plain_text").text("My App")))
                        .close(viewClose(vc -> vc.type("plain_text").text("Close")))
                        .blocks(asBlocks(
                                section(s -> s.text(markdownText(mt ->
                                        mt.text("About the simplest modal you could conceive of :smile:\\n\\nMaybe <https://api.slack.com/reference/block-kit/interactive-components|*make the modal interactive*> or <https://api.slack.com/surfaces/modals/using#modifying|*learn more advanced modal use cases*>.")))),
                                context(c -> c.elements(asContextElements(
                                        markdownText("Psssst this modal was designed using <https://api.slack.com/tools/block-kit-builder|*Block Kit Builder*>")
                                )))
                        ))
                );
                var result = ctx.client().viewsOpen(r -> r
                        // The token you used to initialize your app
                        .token(System.getenv("SLACK_BOT_TOKEN"))
                        .triggerId(payload.getTriggerId())
                        .view(modalView)
                );
                // Print result
                logger.info("result: {}", result);
            } catch (IOException | SlackApiException e) {
                logger.error("error: {}", e.getMessage(), e);
            }
            return ctx.ack();
        });

        var server = new SlackAppServer(app);
        server.start();
    }

}
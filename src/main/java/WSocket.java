import com.slack.api.bolt.App;
import com.slack.api.bolt.AppConfig;
import com.slack.api.bolt.response.Response;
import com.slack.api.bolt.socket_mode.SocketModeApp;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import com.slack.api.model.event.AppHomeOpenedEvent;
import com.slack.api.model.event.AppMentionEvent;
import com.slack.api.model.event.MessageEvent;
import com.slack.api.model.event.ReactionAddedEvent;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.*;
import static com.slack.api.model.block.element.BlockElements.*;
import static com.slack.api.model.view.Views.*;

/**
 * Update by andrey on 23:44 31.01.2021
 */
public class WSocket {
    public static void main(String[] args) throws Exception {
        AtomicBoolean actionCalled = new AtomicBoolean(false);
        App app = new App(AppConfig.builder().singleTeamBotToken(System.getenv("SLACK_BOT_TOKEN")).build());

        app.use((req, resp, chain) -> {
            System.out.println("Just App USE");
            req.getContext().logger.info(req.getRequestBodyAsString());
            return chain.next(req);
        });

        app.command("/start", (req, ctx) -> {
            return ctx.ack("Yes, I'm running in the Socket Mode!");
        });

        app.command("/hello", (req, ctx) -> {
            ctx.say(asBlocks(
                    section(section -> section.blockId("select-users").text(markdownText("*Select Users*")).accessory(multiUsersSelect(select -> select.actionId("select-user-action").placeholder(plainText("Select users that should recieve your survey"))))),
                    actions(actions -> actions.elements(asElements(multiUsersSelect(b -> b.actionId("users-select").placeholder(plainText("select a option"))))))
            ));
            return ctx.ack();
        });

        app.blockAction("a", (req, ctx) -> {
            System.out.println("Block Action event");
            actionCalled.set(true);
            return Response.builder().body("Thanks").build();
        });

        app.event(MessageEvent.class, (req, ctx) -> {
            System.out.println("New Async message event");
            ctx.asyncClient().reactionsAdd(r -> r
                    .channel(req.getEvent().getChannel())
                    .name("eyes")
                    .timestamp(req.getEvent().getTs())
            );
            return ctx.ack();
        });

        app.event(AppHomeOpenedEvent.class, (payload, ctx) -> {
            System.out.println("Home opened");
            return ctx.ack();
        });

        app.globalShortcut("socket-mode-global-shortcut", (req, ctx) -> {
            ctx.asyncClient().viewsOpen(r -> r
                    .triggerId(req.getContext().getTriggerId())
                    .view(view(v -> v
                            .type("modal")
                            .callbackId("test-view")
                            .title(viewTitle(vt -> vt.type("plain_text").text("Modal by Global Shortcut")))
                            .close(viewClose(vc -> vc.type("plain_text").text("Close")))
                            .submit(viewSubmit(vs -> vs.type("plain_text").text("Submit")))
                            .blocks(asBlocks(
//                                    input(input -> input
//                                    .blockId("agenda-block")
//                                    .element(plainTextInput(pti -> pti.actionId("agenda-action").multiline(true)))
//                                    .label(plainText(pt -> pt.text("Detailed Agenda").emoji(true)))
                                    section(section -> section.text(markdownText("Select description"))
                                            .blockId("Action selection")
                                            .accessory(staticSelect(a ->
                                                            a.placeholder(plainText(pt -> pt.text("Select an item")))
                                                                    .options(
                                                                            asOptions(
                                                                                    option(opt -> opt.text(plainText(pt -> pt.text("Var 1")))),
                                                                                    option(opt -> opt.text(plainText(pt -> pt.text("Var 2"))))
                                                                            )
                                                                    )
                                                    )
                                            )
                                    )
                            )))
                    ));
            return ctx.ack();
        });

        app.globalShortcut("open_modal", (req, ctx) -> {
            var logger = ctx.logger;
            try {
                var payload = req.getPayload();
                // Call the conversations.create method using the built-in WebClient
                var modalView = view(v -> v
                        .type("modal")
                        .title(viewTitle(vt -> vt.type("plain_text").text("My App")))
                        .submit(viewSubmit(vc -> vc.type("plain_text").text("Submit")))
                        .blocks(asBlocks(
                                section(s -> s.text(markdownText(mt ->
                                        mt.text("About the simplest modal you could conceive of :smile: Maybe " +
                                                "<https://api.slack.com/reference/block-kit/interactive-components|*make" +
                                                " the modal interactive*> or " +
                                                "<https://api.slack.com/surfaces/modals/using#modifying|*learn more " +
                                                "advanced modal use cases*>.")))),
                                context(c -> c.elements(asContextElements(
                                        markdownText("Psssst this modal was designed using " +
                                                "<https://api.slack.com/tools/block-kit-builder|*Block Kit Builder*>")
                                ))),
                                section(section -> section.text(markdownText("Select description"))
                                        .accessory(staticSelect(a ->
                                                        a.placeholder(plainText(pt -> pt.text("Select an item")))
                                                                .options(
                                                                        asOptions(
                                                                                option(opt -> opt.text(plainText(pt -> pt.text("Var 1")))),
                                                                                option(opt -> opt.text(plainText(pt -> pt.text("Var 2"))))
                                                                        )
                                                                )
                                                )
                                        )
                                )
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

        app.viewSubmission("test-view", (req, ctx) -> {
            System.out.println("!! test view section !!");
            return ctx.ack();
        });

        app.messageShortcut("socket-mode-message-shortcut", (req, ctx) -> {
            ctx.respond("It works!");
            return ctx.ack();
        });

        app.event(AppMentionEvent.class, (req, ctx) -> {
            ctx.say("<@" + req.getEvent().getUser() + "> Hi there!");
            System.out.println(ctx.getChannelId());
            ctx.say(asBlocks(
                    section(section -> section.text(markdownText(mt -> mt.text("*Welcome to QA Assistance*"))))
            ));

            return ctx.ack();
        });

        app.event(ReactionAddedEvent.class, (payload, ctx) -> {
            System.out.println("Reaction!!!");
            ReactionAddedEvent event = payload.getEvent();
            if (event.getReaction().equals("white_check_mark")) {
                ChatPostMessageResponse message = ctx.client().chatPostMessage(r -> r
                        .channel(event.getItem().getChannel())
                        .threadTs(event.getItem().getTs())
                        .text("<@" + event.getUser() + "> Thank you! We greatly appreciate your efforts :two_hearts:"));
                if (!message.isOk()) {
                    ctx.logger.error("chat.postMessage failed: {}", message.getError());
                }
            }
            return ctx.ack();
        });

        app.event(MessageEvent.class, (req, ctx) -> {
            System.out.println("New MessageEvent!!!");
            ctx.say("<@" + req.getEvent().getUser() + "> Hi!!!!");
            ctx.say(asBlocks(
                    divider(),
                    actions(actions -> actions.elements(asElements(
                            button(b -> b.text(plainText(pt -> pt.text("Button1"))).value("button1").actionId("button_1")),
                            button(c -> c.text(plainText(pt -> pt.text("Button2"))).value("button1").actionId("button_2"))
                            ))
                    )
            ));

            ctx.say(asBlocks(section(section -> section.text(markdownText("Select description"))
                    .accessory(staticSelect(a ->
                                    a.placeholder(plainText(pt -> pt.text("Select an item")))
                                            .options(
                                                    asOptions(
                                                            option(opt -> opt.text(plainText(pt -> pt.text("Var 1")))),
                                                            option(opt -> opt.text(plainText(pt -> pt.text("Var 2"))))
                                                    )
                                            )
                            )
                    )
            )));

            System.out.println(ctx.ack().getBody());
            return ctx.ack();
        });

        String appToken = System.getenv("SLACK_APP_TOKEN");

        SocketModeApp socketModeApp = new SocketModeApp(appToken, app);
        socketModeApp.start();


    }
}

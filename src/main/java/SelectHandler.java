import com.slack.api.bolt.context.builtin.ActionContext;
import com.slack.api.bolt.handler.builtin.BlockActionHandler;
import com.slack.api.bolt.request.builtin.BlockActionRequest;
import com.slack.api.bolt.response.Response;
import com.slack.api.methods.SlackApiException;

import java.io.IOException;

/**
 * Update by andrey on 21:40 14.02.2021
 */
public class SelectHandler implements BlockActionHandler {
    @Override
    public Response apply(BlockActionRequest blockActionRequest, ActionContext context) throws IOException, SlackApiException {
        System.out.println("Here is select handler");
        return null;
    }
}

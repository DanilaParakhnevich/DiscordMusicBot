package by.parakhnevich.handler;

import by.parakhnevich.service.MessageService;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class StandardCommandHandler extends ListenerAdapter {
    MessageService messageService;
    Map<String, Consumer<MessageReceivedEvent>> handlingCommands;

    public StandardCommandHandler() {
        super();
        this.messageService = new MessageService();
        handlingCommands = new HashMap<>();
        handlingCommands.put("help", Commands.helpCommand);
        handlingCommands.put("play", Commands.playCommand);
        handlingCommands.put("pause", Commands.pauseCommand);
        handlingCommands.put("resume", Commands.resumeCommand);
        handlingCommands.put("skip", Commands.skipCommand);
    }
    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event) {
        event.getTextChannel().sendMessage( "Some rat deleted message. Be careful").submit();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (messageService.isMessageCommand(event.getMessage())) {
            Consumer<MessageReceivedEvent> command =
                    handlingCommands.get(messageService.cutCommandFromMessage(event.getMessage()));
            if (command != null) {
                command.accept(event);
            } else {
                event.getChannel().sendMessage("Bad command").submit();
            }
        }
    }
}

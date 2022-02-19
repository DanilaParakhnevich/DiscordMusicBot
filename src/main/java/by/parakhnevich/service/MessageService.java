package by.parakhnevich.service;

import net.dv8tion.jda.api.entities.Message;

public class MessageService {
    public boolean isMessageCommand(Message message) {
        return message.getContentDisplay().toCharArray()[0] == '/';
    }

    public String getUrlFromMessage (Message message) {
        return message.getContentDisplay().split(" ")[1];
    }

    public String cutCommandFromMessage(Message message) {
        return message.getContentDisplay().replace("/", "").split(" ")[0];
    }
}

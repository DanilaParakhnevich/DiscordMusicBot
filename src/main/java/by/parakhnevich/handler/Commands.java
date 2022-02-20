package by.parakhnevich.handler;

import by.parakhnevich.service.MessageService;
import by.parakhnevich.service.MusicService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;
import java.io.FileReader;
import java.util.Scanner;
import java.util.function.Consumer;

public class Commands {
    private static final String PATH_TO_FILES = "D:\\Projects\\DiscordMusicBot\\src\\main\\resources\\responses\\";
    private static final MusicService musicService = new MusicService();
    private static final MessageService messageService = new MessageService();

    public static Consumer<MessageReceivedEvent> helpCommand = messageReceivedEvent -> {
        try {
            messageReceivedEvent.getChannel()
                    .sendMessage(readFile(new File(PATH_TO_FILES + "helpResponse.txt")))
                    .submit();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

    public static Consumer<MessageReceivedEvent> playCommand = messageReceivedEvent -> musicService.loadAndPlay(messageReceivedEvent.getTextChannel(),
            messageService.getUrlFromMessage(messageReceivedEvent.getMessage()),
            messageReceivedEvent.getMember());

    public static Consumer<MessageReceivedEvent> playAfterSearch = messageReceivedEvent -> musicService.playAfterSearch(messageReceivedEvent.getTextChannel(),
            messageService.getUrlFromMessage(messageReceivedEvent.getMessage()),
            messageReceivedEvent.getMember());


    public static Consumer<MessageReceivedEvent> skipCommand = messageReceivedEvent -> {
        musicService.skipTrack(messageReceivedEvent.getTextChannel());
    };

    public static Consumer<MessageReceivedEvent> pauseCommand = messageReceivedEvent -> {
        musicService.pause(messageReceivedEvent.getTextChannel());
    };

    public static Consumer<MessageReceivedEvent> resumeCommand = messageReceivedEvent -> {
        musicService.resume(messageReceivedEvent.getTextChannel());
    };

    public static Consumer<MessageReceivedEvent> searchCommand = messageReceivedEvent -> {
        musicService.search(messageReceivedEvent.getTextChannel(),
                messageService.getUrlFromMessage(messageReceivedEvent.getMessage()));
    };

    public static Consumer<MessageReceivedEvent> nowPlaying = messageReceivedEvent -> {
        musicService.playingNow(messageReceivedEvent.getTextChannel());
    };

    public static Consumer<MessageReceivedEvent> setVolume = messageReceivedEvent -> {
        musicService.setVolume(messageReceivedEvent.getTextChannel(),
                messageService.getUrlFromMessage(messageReceivedEvent.getMessage()));
    };

    private static String readFile (File file) throws InterruptedException {
        try {
            FileReader fileReader = new FileReader(file);
            Scanner scanner = new Scanner(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNext()) {
                stringBuilder.append(scanner.nextLine());
                stringBuilder.append('\n');
            }
            fileReader.close();
            return stringBuilder.toString();
        } catch (Exception e) {
            throw new InterruptedException("pizda");
        }
    }
}

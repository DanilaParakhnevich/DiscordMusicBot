package by.parakhnevich.app;

import by.parakhnevich.handler.StandardCommandHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

public class Application {
    public static JDA jda;

    public static void main(String[] args) throws Exception {
        jda = JDABuilder.createDefault("ODEyMDg0MTA4NjQxNTY2NzIw.YC7mQw.7ed7bO5KM3rGv9zQh4o1Zvqsab4")
                .setActivity(Activity.listening("shit"))
                .addEventListeners(new StandardCommandHandler())
                .build()
                .awaitReady();

        jda.getCategories().get(0)
                .getTextChannels().get(0)
                .sendMessage("Hello")
                .submit();
    }
}

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final File SAVE_LOCATION = new File(".\\ChefWumpus\\ChefWumpusBot\\src\\main\\java\\chefbot.txt");

    private static final String BOT_TOKEN = "ODE0MjY1Nzg5MTAzNzM0ODE0.YDbWHA.DuUpNkBCt3HENZNxd7bDLGIBTk4";

    private static ChefBot chefBot;

    public static void main(String[] args) throws LoginException {

        JDABuilder jdaBuilder = JDABuilder.createDefault(BOT_TOKEN);

        try(ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(SAVE_LOCATION))) {

            chefBot = (ChefBot)objectInputStream.readObject();

        }catch(IOException | ClassNotFoundException e) {

            //Ignored, create new ChefBot

        }

        if(chefBot == null) {

            chefBot = new ChefBot();

        }

        ScheduledExecutorService autoSave = Executors.newSingleThreadScheduledExecutor();

        autoSave.scheduleAtFixedRate(() -> save(SAVE_LOCATION), 0, 1, TimeUnit.MINUTES);

        jdaBuilder.addEventListeners(chefBot.getListenerAdapter());

        JDA j = jdaBuilder.build();

        Scanner scanner = new Scanner(System.in);

        scanner.nextLine();

        autoSave.shutdownNow();

        save(SAVE_LOCATION);

        j.shutdownNow();

    }

    public static boolean save(File saveLocation) {

        try{

            FileOutputStream fileOutputStream = new FileOutputStream(saveLocation);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);

            objectOutputStream.writeObject(chefBot);

        }catch(IOException e) {

            return false;

        }

        return true;
    }

}
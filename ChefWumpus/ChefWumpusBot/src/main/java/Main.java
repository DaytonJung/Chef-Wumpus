import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class Main {

    private static final String BOT_TOKEN = "ODE0MjY1Nzg5MTAzNzM0ODE0.YDbWHA.DuUpNkBCt3HENZNxd7bDLGIBTk4";

    public static void main(String [] args) throws LoginException {

        JDA jda = JDABuilder.createDefault(BOT_TOKEN).build();

        jda.addEventListener(new ChefBot());

    }

}
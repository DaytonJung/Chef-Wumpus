import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class Main {

    private static final String BOT_TOKEN = "ODE0MjY1Nzg5MTAzNzM0ODE0.YDbWHA.DuUpNkBCt3HENZNxd7bDLGIBTk4";

    public static void main(String [] args) throws LoginException {

        JDABuilder jdaBuilder = JDABuilder.createDefault(BOT_TOKEN);

        jdaBuilder.addEventListeners(new ChefBot());

        jdaBuilder.build();

    }

}
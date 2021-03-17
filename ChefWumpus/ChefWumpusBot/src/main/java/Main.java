import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;

public class Main {

    private static final String TOKEN = "ODE0MjY1Nzg5MTAzNzM0ODE0.YDbWHA.DuUpNkBCt3HENZNxd7bDLGIBTk4";

    public static void main(String [] args){
        JDABuilder builder = JDABuilder.createDefault(TOKEN);
        JDA jda = null;
        try {
            jda = builder.build();
        }
        catch(LoginException e){
            e.printStackTrace();
        }
        
        jda.addEventListener(new RecipeCommand());
    }
}

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import javax.security.auth.login.LoginException;

public class Main {

    public static void main(String [] args){
        JDABuilder builder = JDABuilder.createDefault("ODE0MjY1Nzg5MTAzNzM0ODE0.YDbWHA.DuUpNkBCt3HENZNxd7bDLGIBTk4");
        JDA jda = null;
        try {
            jda = builder.build();
        }
        catch(LoginException e){
            e.printStackTrace();
        }
    }
}

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class ChefBot extends ListenerAdapter {

    private static final int INGREDIENTS_START_INDEX = 12;
    private static final String BASE_URL = "https://api.spoonacular.com/recipes/";
    private static final String API_KEY = "&apiKey=51a7aa37f6ef405b99101b92bc70db68";

    /**
     * Receives Messages from discord bot and handles them according to content
     * @param event the MessageReceivedEvent to use
     */
    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {

        String command = event.getMessage().getContentRaw().toLowerCase().trim();

        String commandType = command.substring(0, command.indexOf(" "));

        String[] commandArgs = command
                .substring(command.indexOf(" "))
                .replaceAll(" ", "").replaceAll(",", " ")
                .split(" ");

        System.out.println(Arrays.toString(commandArgs));

        if(true) {

            return;

        }

        switch(commandType) {

            case "-ingredients":

                Recipe[] recipes = findRecipes(command.substring(INGREDIENTS_START_INDEX));

                if (recipes == null) {

                    event.getChannel().sendMessage("Sorry! Couldn't find any recipes related to your ingredients.").queue();

                }else{

                    Response nextResponse = CommandBuilder.getClientResponse(recipes[0].getUrl());
                    JSONObject jObject = CommandBuilder.getJSONObject(nextResponse);

                    EmbedBuilder e1 = CommandBuilder.getEmbedMessage(jObject);

                    event.getChannel().sendMessage(e1.build()).queue();

                }

                break;

        }

    }

    private Recipe[] findRecipes(String rawIngredients) {

        String[] ingredientList = rawIngredients.split(", ");

        String ingredientUrl = CommandBuilder.convertIngredients(ingredientList);

        Response response = CommandBuilder.getClientResponse(BASE_URL + "findByIngredients?ingredients=" + ingredientUrl + "&ranking=1&ignorePantry=true" + API_KEY);

        JSONArray jArr = CommandBuilder.getJSONArray(response);

        if(jArr == null || jArr.length() == 0){

            //prematurely return since we couldn't find ANY available recipes
            return null;

        }

        String recipeUrl = BASE_URL + jArr.getJSONObject(0).get("id") + "/information?includeNutrition=false" + API_KEY;

        return new Recipe[] {new Recipe(recipeUrl)};
    }

}
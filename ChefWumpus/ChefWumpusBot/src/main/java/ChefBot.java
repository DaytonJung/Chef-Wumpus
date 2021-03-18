import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class ChefBot extends ListenerAdapter {

    private static final String BASE_URL = "https://api.spoonacular.com/recipes/";
    private static final String API_KEY = "&apiKey=51a7aa37f6ef405b99101b92bc70db68";

    /**
     * Receives Messages from discord bot and handles them according to content
     * @param event the MessageReceivedEvent to use
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        String command = event.getMessage().getContentRaw().toLowerCase().trim();

        int index = command.indexOf(" ");

        //This command is not related to the bot
        if(index == -1) {

            return;

        }

        String commandType = command.substring(0, index);

        String[] commandArgs = command
                .substring(command.indexOf(" "))
                .replaceAll(" ", "").replaceAll(",", " ")
                .split(" ");

        switch(commandType) {

            case "-ingredients":

                Recipe[] recipes;
                Response nextResponse;

                try{

                    recipes = findRecipes(commandArgs);

                    nextResponse = CommandBuilder.getClientResponse(recipes[0].getUrl());

                }catch(IOException e) {

                    event.getChannel().sendMessage("Sorry! Couldn't find any recipes related to your ingredients.").queue();

                    return;

                }

                JSONObject jObject = CommandBuilder.getJSONObject(nextResponse);

                EmbedBuilder e1 = CommandBuilder.getEmbedMessage(jObject);

                event.getChannel().sendMessage(e1.build()).queue();

                break;

        }

    }

    private Recipe[] findRecipes(String[] ingredients) throws IOException {

        String ingredientUrl = CommandBuilder.convertIngredients(ingredients);

        Response response = CommandBuilder.getClientResponse(BASE_URL + "findByIngredients?ingredients=" + ingredientUrl + "&ranking=1&ignorePantry=true" + API_KEY);

        JSONArray jArr = CommandBuilder.getJSONArray(response);

        if(jArr == null || jArr.length() == 0) {

            //prematurely return since we couldn't find ANY available recipes
            throw new IOException();

        }

        String recipeUrl = BASE_URL + jArr.getJSONObject(0).get("id") + "/information?includeNutrition=false" + API_KEY;

        return new Recipe[] { new Recipe(recipeUrl) };
    }

}
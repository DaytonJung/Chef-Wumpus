import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class RecipeCommand implements EventListener {
    private static final int INGREDIENTS_START_INDEX = 12;
    private static final String BASE_URL = "https://api.spoonacular.com/recipes/";
    private static final String API_KEY = "&apiKey=51a7aa37f6ef405b99101b92bc70db68";


    /**
     * Sends  recipe links to discord servers based on user inputted ingredients
     * @param genericEvent Command that is needed to be parsed for ingredients
     */
    @Override
    public void onEvent(GenericEvent genericEvent) {
        if(genericEvent instanceof MessageReceivedEvent){

            MessageReceivedEvent event = (MessageReceivedEvent) genericEvent;
            String command = event.getMessage().getContentRaw().toLowerCase();

            if (command.indexOf("-ingredients ") == 0) {

                command = command.substring(INGREDIENTS_START_INDEX);
                String[] ingredientList = command.split(", ");
                String ingredientUrl = CommandBuilder.convertIngredients(ingredientList);

                Response response = CommandBuilder.getClientResponse(BASE_URL + "findByIngredients?ingredients=" + ingredientUrl + "&ranking=1&ignorePantry=true" + API_KEY);
                JSONArray jArr = CommandBuilder.getJSONArray(response);
                if(jArr == null || jArr.length() == 0){
                    event.getChannel().sendMessage("Sorry! Couldn't find any recipes related to your ingredients.").queue();
                    return; //prematurely return since we couldn't find ANY available recipes
                }
                String recipeUrl = BASE_URL + jArr.getJSONObject(0).get("id") + "/information?includeNutrition=false" + API_KEY;
                Recipe recipe = new Recipe(recipeUrl);

                Response nextResponse = CommandBuilder.getClientResponse(recipeUrl);
                JSONObject jObject = CommandBuilder.getJSONObject(nextResponse);

                EmbedBuilder e1 = CommandBuilder.getEmbedMessage(jObject);
                event.getChannel().sendMessage(e1.build()).queue();
            }
        }
    }

}
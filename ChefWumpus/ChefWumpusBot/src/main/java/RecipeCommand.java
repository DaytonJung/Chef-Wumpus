import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class RecipeCommand extends ListenerAdapter {
    private static final int INGREDIENTS_START_INDEX = 12;
    private static final String BASE_URL = "https://api.spoonacular.com/recipes/";
    private static final String API_KEY = "&apiKey=ca4f364fc4e34482b69fc70527800a7c";

    /**
     * Sends  recipe links to discord servers based on user inputted ingredients
     * @param event Command that is needed to be parsed for ingredients
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        super.onMessageReceived(event);
        String command = event.getMessage().getContentRaw().toLowerCase();
        if (command.indexOf("-ingredients ") == 0) {
            command = command.substring(INGREDIENTS_START_INDEX);
            String[] ingredientList = command.split(", ");
            String ingredientUrl = convertIngredients(ingredientList);
            Response response = getClientResponse(BASE_URL + "findByIngredients?ingredients=" + ingredientUrl + "&ranking=1&ignorePantry=true" + API_KEY);
            JSONArray jArr = getJSONArray(response);
            if(jArr == null){
                event.getChannel().sendMessage("Sorry! Couldn't find any recipes related to your ingredients.").queue();
                return; //prematurely return since we couldn't find ANY available recipes
            }

            Response nextResponse = getClientResponse(BASE_URL + jArr.getJSONObject(0).get("id") + "/information?includeNutrition=false" + API_KEY);
            JSONObject jObject = getJSONObject(nextResponse);
            EmbedBuilder e1 = getEmbedMessage(jObject);
            event.getChannel().sendMessage(e1.build()).queue();
        }
    }

    /**
     * Converts a ingredient list into an appropriate url-based ingredient string (e.g. green eggs,+ham,+cheese)
     * @param str An array of ingredients
     * @return returns a single url-based string
     */
    public String convertIngredients(String[] str){
        String s = str[0];
        for(int i = 1; i < str.length; i++){
            s += ",+" + str[i];
        }
        return s;
    }

    /**
     * Gets an embed message based on the JSONObject (parameter) properties
     * @param jsonObject JSONObject passed to customize the embedded message
     * @return returns an embedded message
     */
    public EmbedBuilder getEmbedMessage(JSONObject jsonObject){
        EmbedBuilder e1 = new EmbedBuilder();
        String summary = jsonObject.get("summary").toString().replaceAll("\\<.*?\\>", ""); //removes any unwanted HTML tags from recipe summary
        e1.setTitle(jsonObject.get("title").toString(), jsonObject.get("sourceUrl").toString());
        e1.setFooter(jsonObject.get("sourceName").toString());
        e1.setImage(jsonObject.get("image").toString());
        if(summary.length() > 300)
            summary = summary.substring(0, 300);
        e1.setDescription(summary + "...");
        return e1;
    }

    public JSONArray getJSONArray(Response response){
        JSONArray jArr = null;
        try {
            if(response == null){
                throw new IOException("No Recipes!");
            }
            jArr = new JSONArray(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            return jArr;
        }
    }
    public JSONObject getJSONObject(Response response){
        JSONObject jObject = null;
        try {
            jObject = new JSONObject(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jObject;
    }

    public Response getClientResponse(String url){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder() //establishes connection with the database
                .url(url)
                .get()
                .build();
        Response response = null;
        try { //gets JSON array of recipes from request
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            return response;
        }

    }
}

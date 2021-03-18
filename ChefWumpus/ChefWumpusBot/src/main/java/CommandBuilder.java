import net.dv8tion.jda.api.EmbedBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public final class CommandBuilder {

    //Not instantiable
    private CommandBuilder() {}

    /**
     * Gets an embed message based on the JSONObject (parameter) properties
     * @param jsonObject JSONObject passed to customize the embedded message
     * @return returns an embedded message
     */
    public static EmbedBuilder getEmbedMessage(JSONObject jsonObject){
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

    public static JSONArray getJSONArray(Response response){
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

    public static JSONObject getJSONObject(Response response){
        JSONObject jObject = null;
        try {
            jObject = new JSONObject(response.body().string());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jObject;
    }

    public static Response getClientResponse(String url){
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
    /**
     * Converts a ingredient list into an appropriate url-based ingredient string (e.g. green eggs,+ham,+cheese)
     * @param str An array of ingredients
     * @return returns a single url-based string
     */
    public static String convertIngredients(String[] str){
        String s = str[0];
        for(int i = 1; i < str.length; i++){
            s += ",+" + str[i];
        }
        return s;
    }

}
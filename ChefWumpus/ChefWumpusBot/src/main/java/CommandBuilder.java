import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Class command Builder that builds commands
 * @author Group 2
 */
public final class CommandBuilder {
    /**
     * Cannot create instance
     */
    private CommandBuilder() {}

    /**
     * Gets an embed message based on the JSONObject (parameter) properties
     * @param jsonObject JSONObject passed to customize the embedded message
     * @return returns an embedded message
     */
    public static MessageEmbed getEmbedMessage(JSONObject jsonObject){

        EmbedBuilder e1 = new EmbedBuilder();

        String summary = jsonObject.get("summary").toString().replaceAll("\\<.*?\\>", ""); //Removes any unwanted HTML tags from recipe summary

        e1.setTitle(jsonObject.get("title").toString(), jsonObject.get("sourceUrl").toString());
        e1.setFooter(jsonObject.get("sourceName").toString());
        e1.setImage(jsonObject.get("image").toString());

        if(summary.length() > 300) {

            summary = summary.substring(0, 300);

        }

        e1.setDescription(summary + "...");

        return e1.build();
    }

    /**
     * Gets the built link to the recipe.
     * @param recipe for the user
     * @return link to recipe
     */
    public static MessageEmbed getEmbedMessage(Recipe recipe){

        EmbedBuilder e1 = new EmbedBuilder();

        e1.setTitle(recipe.getName(), recipe.getUrl());
        e1.setDescription(recipe.getSummary());
        e1.setImage(recipe.getImageUrl());
        e1.setFooter(recipe.getSourceName());

        return e1.build();
    }

    /**
     * Gets a new JSONArray
     * @param response to user
     * @return JSONArray
     * @throws IOException
     */
    public static JSONArray getJSONArray(Response response) throws IOException {

        return new JSONArray(response.body().string());
    }

    /**
     * Getter for the JSON object
     * @param response to user
     * @return JSONObject
     * @throws IOException
     */
    public static JSONObject getJSONObject(Response response) throws IOException {

        return new JSONObject(response.body().string());
    }

    /**
     * Getter for the client response
     * @param url of recipe website
     * @return the url
     * @throws IOException
     */
    public static Response getClientResponse(String url) throws IOException {

        Request request = new Request.Builder() //Establishes connection with the database
                .url(url)
                .get()
                .build();

        return new OkHttpClient().newCall(request).execute();
    }

}
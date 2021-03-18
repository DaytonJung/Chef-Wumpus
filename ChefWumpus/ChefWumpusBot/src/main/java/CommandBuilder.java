import net.dv8tion.jda.api.EmbedBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public final class CommandBuilder {

    private CommandBuilder() {}

    /**
     * Gets an embed message based on the JSONObject (parameter) properties
     * @param jsonObject JSONObject passed to customize the embedded message
     * @return returns an embedded message
     */
    public static EmbedBuilder getEmbedMessage(JSONObject jsonObject){

        EmbedBuilder e1 = new EmbedBuilder();

        String summary = jsonObject.get("summary").toString().replaceAll("\\<.*?\\>", ""); //Removes any unwanted HTML tags from recipe summary

        e1.setTitle(jsonObject.get("title").toString(), jsonObject.get("sourceUrl").toString());
        e1.setFooter(jsonObject.get("sourceName").toString());
        e1.setImage(jsonObject.get("image").toString());

        if(summary.length() > 300) {

            summary = summary.substring(0, 300);

        }

        e1.setDescription(summary + "...");

        return e1;
    }

    public static JSONArray getJSONArray(Response response) throws IOException {

        return new JSONArray(response.body().string());
    }

    public static JSONObject getJSONObject(Response response) throws IOException {

        return new JSONObject(response.body().string());
    }

    public static Response getClientResponse(String url) throws IOException {

        Request request = new Request.Builder() //Establishes connection with the database
                .url(url)
                .get()
                .build();

        return new OkHttpClient().newCall(request).execute();
    }

}
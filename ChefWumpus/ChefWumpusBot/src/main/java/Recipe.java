import net.dv8tion.jda.api.entities.MessageEmbed;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class Recipe implements Serializable {

    private String url;
    private String name;
    private List<String> ingredientList;
    private String imageUrl;
    private String summary;

    public Recipe(String url) {

        this.url = url;

    }

    public final String getUrl() {

        return url;
    }

    public final MessageEmbed getMessageEmbed() throws IOException {

        Response nextResponse = CommandBuilder.getClientResponse(url);

        JSONObject jObject = CommandBuilder.getJSONObject(nextResponse);

        nextResponse.close();

        return CommandBuilder.getEmbedMessage(jObject).build();
    }

}
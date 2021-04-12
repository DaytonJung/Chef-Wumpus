import net.dv8tion.jda.api.entities.MessageEmbed;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Recipe implements Serializable {

    private String url;
    private String name;
    private List<String> ingredientList = new ArrayList<>();
    private String imageUrl;
    private String summary;
    private transient JSONObject jObject;
    private String sourceName;

    public Recipe(String summary, String name, String url, String imageUrl, String sourceName) {

        this.summary = summary;
        this.name = name;
        this.url = url;
        this.imageUrl = imageUrl;
        this.sourceName = sourceName;

    }

    public Recipe(String url) throws IOException {

        this.url = url;

        buildJObject(url);

        name = jObject.get("title").toString();

        imageUrl = jObject.get("image").toString();

        summary = jObject.get("summary").toString().replaceAll("\\<.*?\\>", "");

        String iUrl = url.replace("information","ingredientWidget.json");

        buildIngredientList(iUrl);

    }

    public final MessageEmbed getMessageEmbed() {

        return CommandBuilder.getEmbedMessage(jObject);
    }

    /**
     * Instantiates JSON object for a given url -- to be accessed as jObject
     * @param url the id url for a given recipe
     * @throws IOException I/O
     */
    private void buildJObject(String url) throws IOException {

        Response nextResponse = CommandBuilder.getClientResponse(url);

        jObject = CommandBuilder.getJSONObject(nextResponse);

        nextResponse.close();

    }

    /**
     * Uses "Get Recipe Ingredients by ID" API function to parse ingredient list
     * @param iUrl the ingredient url is used to call the particular function
     * @throws IOException I/O
     */
    private void buildIngredientList(String iUrl) throws IOException {

        Response nextResponse = CommandBuilder.getClientResponse(iUrl);

        JSONObject jObjectIngredients = CommandBuilder.getJSONObject(nextResponse);

        nextResponse.close();

        JSONArray jObjectArrayIngredients = (JSONArray) jObjectIngredients.get("ingredients");

        if (jObjectArrayIngredients.length() != 0) {

            for(int i = 0; i < jObjectArrayIngredients.length(); i++){

                JSONObject tempJ = jObjectArrayIngredients.getJSONObject(i);

                ingredientList.add(tempJ.get("name").toString());

                //System.out.println(ingredientList.get(i));

            }

        }

    }

    public final String getUrl() {

        return url;
    }

    public final String getName() {

        return name;
    }

    public final String getImageUrl() {

        return imageUrl;
    }

    public final String getSummary() {

        return summary;
    }

    public final List<String> getIngredientList(){

        return ingredientList;
    }

    public String getSourceName() {

        return sourceName;
    }

    @Override
    public boolean equals(Object obj) {

        if(obj instanceof Recipe) {

            Recipe recipe = (Recipe)obj;

            return recipe.url.equals(url);

        }

        return false;
    }

    @Override
    public String toString() {

        return name;
    }

    @Override
    public int hashCode() {

        return url.hashCode();
    }

}
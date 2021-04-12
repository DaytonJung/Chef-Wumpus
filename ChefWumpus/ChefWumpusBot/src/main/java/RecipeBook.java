import net.dv8tion.jda.api.entities.MessageEmbed;
import okhttp3.Response;
import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RecipeBook extends HashSet<Recipe> {

    private static final String BASE_URL = "https://api.spoonacular.com/recipes/";
    private static final String API_KEY = "&apiKey=51a7aa37f6ef405b99101b92bc70db68";

    private static final int MAX_RECIPES = 10;

    public static RecipeBook getRecipes(List<String> ingredients, int numRecipes) throws IOException {

        RecipeBook recipeBook = new RecipeBook();

        if((ingredients != null) && !ingredients.isEmpty() && (numRecipes > 0)) {

            String ingredientUrl = convertIngredients(ingredients);

            Response response = CommandBuilder.getClientResponse(BASE_URL + "findByIngredients?ingredients=" + ingredientUrl + "&ranking=1&ignorePantry=true" + API_KEY);

            JSONArray jArr = CommandBuilder.getJSONArray(response);

            numRecipes = Math.min(MAX_RECIPES, numRecipes);

            if(jArr.length() != 0) {

                for(int i = 0; i < numRecipes; i++) {

                    recipeBook.add(new Recipe(BASE_URL + jArr.getJSONObject(i).get("id") + "/information?includeNutrition=false" + API_KEY));

                }

            }

            response.close();

        }

        return recipeBook;
    }

    private static String convertIngredients(List<String> ingredients){

        if(!ingredients.isEmpty()) {

            StringBuilder s = new StringBuilder(ingredients.get(0));

            for(int i = 1; i < ingredients.size(); i++){

                s.append(",+").append(ingredients.get(i));

            }

            return s.toString();

        }

        return "";
    }



    public List<MessageEmbed> getRecipeMessages() {

        List<MessageEmbed> messageEmbeds = new ArrayList<>();

        for(Recipe recipe : this) {

            messageEmbeds.add(CommandBuilder.getEmbedMessage(recipe));

        }

        return messageEmbeds;
    }

}
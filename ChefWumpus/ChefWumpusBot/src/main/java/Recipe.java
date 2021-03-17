public class Recipe {

    private String url;
    private String name;
    private String[] ingredientList;
    private String imageUrl;
    private String summary;

    public Recipe(String url) {

        this.url = url;

    }

    public String getUrl() {

        return url;
    }

}
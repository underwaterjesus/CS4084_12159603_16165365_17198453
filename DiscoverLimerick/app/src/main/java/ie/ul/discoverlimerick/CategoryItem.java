package ie.ul.discoverlimerick;

public class CategoryItem {
    private int mImageresource;
    private String categoryName;

    public CategoryItem(int imageResource, String text){
        mImageresource = imageResource;
        categoryName = text;
    }

    public int getImageresource(){
        return mImageresource;
    }

    public String getCategoryName() {
        return categoryName;
    }
}

package pl.com.andrzejgrzyb.shoppinglist.adapter;

public class ShoppingListHolderItem {

    private long id;
    private String name;
    private String description;
    private String modificationDate;
    private String itemsCount;
    private String percentCompleted;

    public ShoppingListHolderItem(long id, String name, String description, String modificationDate, String itemsCount, String percentCompleted) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.modificationDate = modificationDate;
        this.itemsCount = itemsCount;
        this.percentCompleted = percentCompleted;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(String modificationDate) {
        this.modificationDate = modificationDate;
    }

    public String getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(String itemsCount) {
        this.itemsCount = itemsCount;
    }

    public String getPercentCompleted() {
        return percentCompleted;
    }

    public void setPercentCompleted(String percentCompleted) {
        this.percentCompleted = percentCompleted;
    }
}

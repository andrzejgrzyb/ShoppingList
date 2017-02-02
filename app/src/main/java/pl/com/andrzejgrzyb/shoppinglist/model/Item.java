package pl.com.andrzejgrzyb.shoppinglist.model;

import org.json.JSONException;
import org.json.JSONObject;

public class Item {

	public Item() { }
	public Item(JSONObject obj) throws JSONException {
		idCloud = obj.getInt("idCloud");
		name = obj.getString("name");
		quantity = obj.getDouble("quantity");
		quantityUnit = obj.getString("quantityUnit");
		listIdCloud = obj.getInt("listIdCloud");
		position = obj.getInt("position");
		checked = obj.getInt("checked");
		modificationDate = obj.getLong("modificationDate");
		modifiedByIdCloud = obj.getString("modifiedByIdCloud");
	}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setIdCloud(long idCloud) {
        this.idCloud = idCloud;
    }

    private long id;
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	@Column(name = "id_cloud")
	private long idCloud;

//	@Size(min = 3, max = 50)
//	@Column(name = "NAME")
	private String name;

//	@Digits(integer = 10, fraction = 5)
//	@Column(name = "QUANTITY")
	private double quantity;

//	@Size(min = 1, max = 50)
//	@Column(name = "QUANTITY_UNIT")
	private String quantityUnit;

    private long listId;

//	@Digits(integer = 10, fraction = 0)
//	@Column(name = "LIST_ID_CLOUD")
	private long listIdCloud;

//	@Digits(integer = 10, fraction = 0)
//	@Column(name = "POSITION")
	private int position;

//	@Digits(integer = 10, fraction = 0)
//	@Column(name = "CHECKED")
	private int checked;

//	@Digits(integer = 15, fraction = 0)
//	@Column(name = "MODIFICATION_DATE")
	private long modificationDate;

//	@Digits(integer = 10, fraction = 0)
//	@Column(name = "MODIFIED_BY_ID_CLOUD")
	private String modifiedByIdCloud;

	public long getIdCloud() {
		return idCloud;
	}

	public void setIdCloud(int idCloud) {
		this.idCloud = idCloud;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getQuantity() {
		return quantity;
	}

	public void setQuantity(double quantity) {
		this.quantity = quantity;
	}

	public String getQuantityUnit() {
		return quantityUnit;
	}

	public void setQuantityUnit(String quantityUnit) {
		this.quantityUnit = quantityUnit;
	}

	public long getListIdCloud() {
		return listIdCloud;
	}

	public void setListIdCloud(long listIdCloud) {
		this.listIdCloud = listIdCloud;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getChecked() {
		return checked;
	}

	public void setChecked(int checked) {
		this.checked = checked;
	}

	public long getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(long modificationDate) {
		this.modificationDate = modificationDate;
	}

    public String getModifiedByIdCloud() {
        return modifiedByIdCloud;
    }

    public void setModifiedByIdCloud(String modifiedByIdCloud) {
        this.modifiedByIdCloud = modifiedByIdCloud;
    }

    public JSONObject toJSONObject() throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("idCloud", idCloud);
		obj.put("name",name);
		obj.put("quantity", quantity);
		obj.put("quantityUnit", quantityUnit);
		obj.put("listIdCloud", listIdCloud);
		obj.put("position", position);
		obj.put("checked", checked);
		obj.put("modificationDate", modificationDate);
		obj.put("modifiedByIdCloud", modifiedByIdCloud);

		return obj;
	}

	@Override
	public String toString() {
		return "Item{" +
				"idCloud=" + idCloud +
				", name='" + name + '\'' +
				", quantity=" + quantity +
				", quantityUnit='" + quantityUnit + '\'' +
				", listIdCloud=" + listIdCloud +
				", position=" + position +
				", checked=" + checked +
				", modificationDate=" + modificationDate +
				", modifiedByIdCloud=" + modifiedByIdCloud +
				'}';
	}


    @Override
    public int hashCode() {
        int result;
        long temp;
        result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (idCloud ^ (idCloud >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        temp = Double.doubleToLongBits(quantity);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (quantityUnit != null ? quantityUnit.hashCode() : 0);
        result = 31 * result + (int) (listId ^ (listId >>> 32));
        result = 31 * result + (int) (listIdCloud ^ (listIdCloud >>> 32));
        result = 31 * result + position;
        result = 31 * result + checked;
        result = 31 * result + (int) (modificationDate ^ (modificationDate >>> 32));
        result = 31 * result + (modifiedByIdCloud != null ? modifiedByIdCloud.hashCode() : 0);
        return result;
    }

    @Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Item item = (Item) o;

		if (idCloud != item.idCloud) return false;

		if (Double.compare(item.quantity, quantity) != 0) return false;
		if (listIdCloud != item.listIdCloud) return false;
		if (position != item.position) return false;
		if (checked != item.checked) return false;
		if (modificationDate != item.modificationDate) return false;
		if (modifiedByIdCloud != item.modifiedByIdCloud) return false;
		if (!name.equals(item.name)) return false;
		return quantityUnit.equals(item.quantityUnit);

	}

}

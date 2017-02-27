package pl.com.andrzejgrzyb.shoppinglist.model;

import org.json.JSONException;
import org.json.JSONObject;

public class ShoppingList {

	public ShoppingList() { }
	public ShoppingList(JSONObject obj) throws JSONException {
		idCloud = obj.getLong("idCloud");
		name = obj.getString("name");
		description = obj.getString("description");
		ownerIdCloud = obj.getString("ownerIdCloud");
		modificationDate = obj.getLong("modificationDate");
		modifiedByIdCloud = obj.getString("modifiedByIdCloud");
		hashtag = obj.getString("hashtag");
	}

	private long id;
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	@Column(name = "ID_CLOUD")
	private long idCloud;

//	@Size(min = 3, max = 50)
//	@Column(name = "NAME")
	private String name;

//	@Size(min = 3, max = 255)
//	@Column(name = "DESCRIPTION")
	private String description;

//	@Digits(integer = 10, fraction = 0)
//	@Column(name = "owner_id_cloud")
	private String ownerIdCloud;

//	@Digits(integer = 15, fraction = 0)
//	@Column(name = "MODIFICATION_DATE")
	private long modificationDate;

//	@Digits(integer = 10, fraction = 0)

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getIdCloud() {
		return idCloud;
	}

	public void setIdCloud(long idCloud) {
		this.idCloud = idCloud;
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

	public String getOwnerIdCloud() {
		return ownerIdCloud;
	}

	public void setOwnerIdCloud(String ownerIdCloud) {
		this.ownerIdCloud = ownerIdCloud;
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

	public String getHashtag() {
		return hashtag;
	}

	public void setHashtag(String hashtag) {
		this.hashtag = hashtag;
	}

	//	@Column(name = "MODIFIED_BY_ID_CLOUD")
	private String modifiedByIdCloud;

//	@Column(name = "HASHTAG")
	private String hashtag;


	public JSONObject toJSONObject() throws JSONException {
		JSONObject obj = new JSONObject();
		obj.put("idCloud", idCloud);
		obj.put("name", name);
		obj.put("description", description);
		obj.put("ownerIdCloud", ownerIdCloud);
		obj.put("modificationDate", modificationDate);
		obj.put("modifiedByIdCloud", modifiedByIdCloud);
		obj.put("hashtag", hashtag);

		return obj;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ShoppingList that = (ShoppingList) o;

		if (id != that.id) return false;
		if (idCloud != that.idCloud) return false;
		if (modificationDate != that.modificationDate) return false;
		if (name != null ? !name.equals(that.name) : that.name != null) return false;
		if (description != null ? !description.equals(that.description) : that.description != null)
			return false;
		if (ownerIdCloud != null ? !ownerIdCloud.equals(that.ownerIdCloud) : that.ownerIdCloud != null)
			return false;
		if (modifiedByIdCloud != null ? !modifiedByIdCloud.equals(that.modifiedByIdCloud) : that.modifiedByIdCloud != null)
			return false;
		return hashtag != null ? hashtag.equals(that.hashtag) : that.hashtag == null;

	}

	@Override
	public int hashCode() {
		int result = (int) (id ^ (id >>> 32));
		result = 31 * result + (int) (idCloud ^ (idCloud >>> 32));
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (description != null ? description.hashCode() : 0);
		result = 31 * result + (ownerIdCloud != null ? ownerIdCloud.hashCode() : 0);
		result = 31 * result + (int) (modificationDate ^ (modificationDate >>> 32));
		result = 31 * result + (modifiedByIdCloud != null ? modifiedByIdCloud.hashCode() : 0);
		result = 31 * result + (hashtag != null ? hashtag.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "ShoppingList{" +
				"idCloud=" + idCloud +
				", name='" + name + '\'' +
				", description='" + description + '\'' +
				", ownerIdCloud=" + ownerIdCloud +
				", modificationDate=" + modificationDate +
				", modifiedByIdCloud=" + modifiedByIdCloud +
				", hashtag='" + hashtag + '\'' +
				'}';
	}

}

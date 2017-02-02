package pl.com.andrzejgrzyb.shoppinglist.model;

import org.json.JSONException;
import org.json.JSONObject;

public class User {

	public User() { }
	public User(JSONObject obj) throws JSONException {
		idCloud = obj.getString("idCloud");
        email = obj.getString("email");
		name = obj.getString("name");
        image = obj.getString("image");
	}
	private long id;
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	@Column(name = "ID_CLOUD")
	private String idCloud;

//	@Size(min = 3, max = 50)
//	@Column(name = "LOGIN", unique = true, nullable = false)
	private String email;

//	@Size(min = 3, max = 50)
//	@Column(name = "NAME")
	private String name;

    private String image;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIdCloud() {
        return idCloud;
    }

    public void setIdCloud(String idCloud) {
        this.idCloud = idCloud;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("idCloud", idCloud);
        obj.put("email", email);
        obj.put("name", name);
        obj.put("image", image);

        return obj;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (id != user.id) return false;
        if (idCloud != null ? !idCloud.equals(user.idCloud) : user.idCloud != null) return false;
        if (email != null ? !email.equals(user.email) : user.email != null) return false;
        if (name != null ? !name.equals(user.name) : user.name != null) return false;
        return image != null ? image.equals(user.image) : user.image == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (idCloud != null ? idCloud.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (image != null ? image.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", idCloud='" + idCloud + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}

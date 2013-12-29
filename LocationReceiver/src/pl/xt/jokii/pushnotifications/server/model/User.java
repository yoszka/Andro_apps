package pl.xt.jokii.pushnotifications.server.model;

/**
 * Container class with registered user data
 * @author Tomek
 *
 */
public class User {
	private Integer  id;
	private String  gcm_regid;
	private String  name;
	private String  email;
	private String  created_at;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getGcm_regid() {
		return gcm_regid;
	}
	public void setGcm_regid(String gcm_regid) {
		this.gcm_regid = gcm_regid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getCreated_at() {
		return created_at;
	}
	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	} 
}

package pl.xt.jokii.pushnotifications.server.model;

/**
 * Container class with locations data
 * @author Tomek
 */
public class LocationDb {
    private Integer id;
    private String  user;
    private Double  location_lat;
    private Double  location_lon;
    private Float   location_acc;
    private String  location_provider;
    private Long    location_timestamp;

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public String getUser() {
        return user;
    }
    public void setUser(String user) {
        this.user = user;
    }
    public Double getLocation_lat() {
        return location_lat;
    }
    public void setLocation_lat(Double location_lat) {
        this.location_lat = location_lat;
    }
    public Double getLocation_lon() {
        return location_lon;
    }
    public void setLocation_lon(Double location_lon) {
        this.location_lon = location_lon;
    }
    public Float getLocation_acc() {
        return location_acc;
    }
    public void setLocation_acc(Float location_acc) {
        this.location_acc = location_acc;
    }
    public String getLocation_provider() {
        return location_provider;
    }
    public void setLocation_provider(String location_provider) {
        this.location_provider = location_provider;
    }
    public Long getLocation_timestamp() {
        return location_timestamp;
    }
    public void setLocation_timestamp(Long location_timestamp) {
        this.location_timestamp = location_timestamp;
    }

}

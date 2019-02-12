package tony.dev.mohamed.myuberridder.models;

public class Rider {
    private String name, email, phone, photo, rates,vechleType;

    public Rider() {
    }

    public Rider(String name, String email, String phone, String photo, String rates, String vechleType) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.photo = photo;
        this.rates = rates;
        this.vechleType = vechleType;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getRates() {
        return rates;
    }

    public void setRates(String rates) {
        this.rates = rates;
    }

    public String getVechleType() {
        return vechleType;
    }

    public void setVechleType(String vechleType) {
        this.vechleType = vechleType;
    }
}

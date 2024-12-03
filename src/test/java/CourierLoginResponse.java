public class CourierLoginResponse {

    private String id; // Поле для хранения ID курьера

    // Геттеры и сеттеры для поля id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Опционально: переопределите toString(), если нужно
    @Override
    public String toString() {
        return "CourierLoginResponse{id='" + id + "'}";
    }
}

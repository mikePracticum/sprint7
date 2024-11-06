import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Test;
import io.restassured.path.json.JsonPath;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class OrderListTests {

    private static final String BASE_URI = "https://qa-scooter.praktikum-services.ru/api/v1";

    // Запрос с параметрами limit=10&page=0
    @Test
    public void shouldReturnListOfOrdersWithPagination() {
        Response response = given()
                .baseUri(BASE_URI)
                .param("limit", 10)
                .param("page", 0)
                .when()
                .get("/orders");

        // Печать тела ответа для отладки
        System.out.println("Response body: " + response.asString());
        System.out.println("Response status code: " + response.getStatusCode());

        // Проверка, что статус ответа 200 и что есть список заказов
        response.then()
                .statusCode(200)
                .body("orders", notNullValue())  // Проверяем, что поле 'orders' присутствует
                .body("orders", is(not(empty())))  // Проверяем, что список заказов не пустой
                .body("pageInfo", notNullValue());  // Проверяем, что есть информация о странице
    }

    // Запрос с courierId=1 (он должен вернуть ошибку)
    @Test
    public void shouldReturnErrorForInvalidCourierId() {
        Response response = given()
                .baseUri(BASE_URI)
                .param("courierId", 1)
                .when()
                .get("/orders");

        // Печать тела ответа для отладки
        System.out.println("Response body: " + response.asString());
        System.out.println("Response status code: " + response.getStatusCode());

        // Проверка, что статус ответа 404 (не найдено)
        response.then()
                .statusCode(404)
                .body("message", equalTo("Курьер с идентификатором 1 не найден"));  // Ожидаем сообщение об ошибке
    }

    // Запрос с фильтрацией по nearestStation=["110"]
    @Test
    public void shouldReturnOrdersForSpecificMetroStation() {
        Response response = given()
                .baseUri(BASE_URI)
                .param("limit", 10)
                .param("page", 0)
                .param("nearestStation", "[\"110\"]")  // Фильтруем по станции метро "110"
                .when()
                .get("/orders");

        // Печать тела ответа для отладки
        System.out.println("Response body: " + response.asString());
        System.out.println("Response status code: " + response.getStatusCode());

        // Проверка, что статус ответа 200 и что есть список заказов
        response.then()
                .statusCode(200)
                .body("orders", notNullValue())  // Проверяем, что поле 'orders' присутствует
                .body("orders", is(not(empty())))  // Проверяем, что список заказов не пустой
                .body("pageInfo", notNullValue());  // Проверяем, что есть информация о странице
    }
}

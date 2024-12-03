import io.qameta.allure.Step;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class OrderTests {

    private static final String BASE_URI = "https://qa-scooter.praktikum-services.ru/api/v1";
    private String color;

    public OrderTests(String color) {
        this.color = color;
    }

    @Before
    @Step("Setting up base URI")
    public void setUp() {
        baseURI = BASE_URI;
    }

    // Параметризированный тест для проверки различных вариантов цветовых предпочтений
    @Test
    @Step("Create an order with color preferences")
    public void shouldCreateOrderWithColors() {
        // Составляем тело запроса
        String body = "{\n" +
                "    \"firstName\": \"Simona\",\n" +
                "    \"lastName\": \"Krikun\",\n" +
                "    \"address\": \"Moscow, 142 apt.\",\n" +
                "    \"metroStation\": 4,\n" +
                "    \"phone\": \"+7 800 355 35 35\",\n" +
                "    \"rentTime\": 5,\n" +
                "    \"deliveryDate\": \"2020-06-06\",\n" +
                "    \"comment\": \"Thanks\",\n" +
                "    \"color\": " + color + "\n" +
                "}";

        // Выводим тело запроса в консоль
        System.out.println("Request Body: " + body);

        // Отправляем запрос и проверяем, что ответ содержит track
        String response = given()
                .contentType("application/json")
                .body(body)
                .when()
                .post("/orders")
                .then()
                .statusCode(201)
                .body("track", notNullValue())  // Проверка наличия поля track в ответе
                .extract().asString();  // Извлекаем ответ как строку

        // Выводим ответ в консоль
        System.out.println("Response Body: " + response);

        // Дополнительная проверка, что тело запроса соответствует ожиданиям
        Assert.assertTrue("Response should contain track", response.contains("track"));
    }

    // Источник данных для параметризированного теста
    @Parameterized.Parameters
    public static Collection<Object[]> provideColorsForOrderCreation() {
        return Arrays.asList(new Object[][]{
                {"[\"BLACK\"]"},   // Указан один цвет BLACK
                {"[\"GREY\"]"},    // Указан один цвет GREY
                {"[\"BLACK\", \"GREY\"]"}, // Указаны оба цвета
                {"[]"}             // Цвет не указан
        });
    }
}

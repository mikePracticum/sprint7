import io.qameta.allure.Step;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class CourierCreationTests {

    private static final String BASE_URI = "https://qa-scooter.praktikum-services.ru/api/v1";
    private String login;
    private String password;
    private String firstName;

    private static String courierId;  // Для хранения ID курьера

    @BeforeClass
    public static void setUpClass() {
        baseURI = BASE_URI;
    }

    // Параметризированный конструктор, который инициализирует данные для каждого теста
    public CourierCreationTests(String login, String password, String firstName) {
        this.login = login;
        this.password = password;
        this.firstName = firstName;
    }

    // Параметры для тестов
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"Simona400", "123456", "Sima"}
        });
    }

    // Создание курьера
    private String createCourier() {
        return given()
                .contentType("application/json")
                .body("{ \"login\": \"" + login + "\", \"password\": \"" + password + "\", \"firstName\": \"" + firstName + "\" }")
                .when()
                .post("/courier")
                .then()
                .statusCode(201)  // Ожидаем статус 201 при успешном создании курьера
                .body("ok", equalTo(true))
                .extract()
                .asString();
    }

    // Логинимся и получаем ID курьера
    private void loginAndGetCourierId() {
        String loginResponse = given()
                .contentType("application/json")
                .body("{ \"login\": \"" + login + "\", \"password\": \"" + password + "\" }")
                .when()
                .post("/courier/login")
                .then()
                .statusCode(200)  // Ожидаем успешный логин
                .extract()
                .asString();

        // Десериализуем ответ и извлекаем id
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            CourierLoginResponse loginResponseObj = objectMapper.readValue(loginResponse, CourierLoginResponse.class);
            courierId = loginResponseObj.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Created Courier ID: " + courierId);  // Печатаем ID курьера для отладки
    }

    // Удаление курьера по ID
    private void deleteCourier() {
        if (courierId != null) {
            // Удаление курьера после теста
            String response = given()
                    .contentType("application/json")
                    .when()
                    .delete("https://qa-scooter.praktikum-services.ru/api/v1/courier/" + courierId)  // Путь с ID курьера
                    .then()
                    .statusCode(200)  // Ожидаем успешный ответ при удалении
                    .body("ok", equalTo(true))  // Проверяем, что в ответе ok = true
                    .extract()
                    .asString();

            System.out.println("Delete Courier Response: " + response);  // Печатаем ответ на удаление для отладки
        }
    }


    // Тест на создание курьера
    @Test
    @Step("Create a courier successfully")
    public void shouldCreateCourierSuccessfully() {
        String response = given()
                .contentType("application/json")
                .body("{ \"login\": \"" + login + "\", \"password\": \"" + password + "\", \"firstName\": \"" + firstName + "\" }")
                .when()
                .post("/courier")
                .then()
                .statusCode(201)  // Ожидаем статус 201 при успешном создании курьера
                .body("ok", equalTo(true))
                .extract()
                .asString();

        System.out.println("Create Courier Response: " + response);  // Печатаем ответ сервера

        loginAndGetCourierId();
        deleteCourier();
    }

    // Тест на полный дубликат курьера
    @Test
    @Step("Prevent creating duplicate courier")
    public void shouldNotAllowDuplicateCourier() {
        // Создаем курьера
        String createResponse = createCourier();
        System.out.println("Create Courier Response: " + createResponse);

        // Логинимся, чтобы получить ID курьера
        loginAndGetCourierId();

        // Попробуем создать его снова с тем же логином
        String duplicateResponse = given()
                .contentType("application/json")
                .body("{ \"login\": \"" + login + "\", \"password\": \"" + password + "\", \"firstName\": \"" + firstName + "\" }")
                .when()
                .post("/courier")
                .then()
                .statusCode(409)  // Ожидаем ошибку 409, так как курьер с таким логином уже существует
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."))
                .extract().asString();

        System.out.println("Prevent Duplicate Courier Response: " + duplicateResponse);  // Печатаем ответ с ошибкой

        // Удаляем курьера
        deleteCourier();
    }

    // Тест на нехватку пароля при создании курьера
    @Test
    @Step("Create courier with missing password")
    public void shouldReturnErrorWhenPasswordIsEmpty() {
        String response = given()
                .contentType("application/json")
                .body("{ \"login\": \"" + login + "\", \"password\": \"\", \"firstName\": \"" + firstName + "\" }")  // Пустой пароль
                .when()
                .post("/courier")
                .then()
                .statusCode(400)  // Ожидаем ошибку из-за пустого пароля
                .body("message", equalTo("Недостаточно данных для создания учетной записи"))
                .extract().asString();

        System.out.println("Create Courier Empty Password Response: " + response);  // Печатаем ответ с ошибкой
    }

    // Тест на нехватку логина при создании курьера
    @Test
    @Step("Create courier with missing login")
    public void shouldReturnErrorWhenLoginIsEmpty() {
        String response = given()
                .contentType("application/json")
                .body("{ \"login\": \"\", \"password\": \"" + password + "\", \"firstName\": \"" + firstName + "\" }")  // Пустой логин
                .when()
                .post("/courier")
                .then()
                .statusCode(400)  // Ожидаем ошибку из-за пустого логина
                .body("message", equalTo("Недостаточно данных для создания учетной записи"))
                .extract().asString();

        System.out.println("Create Courier Empty Login Response: " + response);  // Печатаем ответ с ошибкой
    }

    // Тест на создание курьера с повторяющимся логином
    @Test
    @Step("Create courier with existing login")
    public void shouldReturnErrorWhenLoginAlreadyExists() {
        // Создаем курьера
        String createResponse = createCourier();
        System.out.println("Create Courier Response: " + createResponse);

        // Логинимся, чтобы получить ID курьера
        loginAndGetCourierId();

        // Попробуем создать его снова с тем же логином
        String response = given()
                .contentType("application/json")
                .body("{ \"login\": \"" + login + "\", \"password\": \"" + password + "\", \"firstName\": \"" + firstName + "\" }")
                .when()
                .post("/courier")
                .then()
                .statusCode(409)  // Ожидаем ошибку 409, так как логин уже существует
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."))
                .extract().asString();

        System.out.println("Create Courier Existing Login Response: " + response);  // Печатаем ответ с ошибкой

        // Удаляем курьера
        deleteCourier();
    }
}

import io.qameta.allure.Step;
import org.junit.*;
import org.junit.runners.MethodSorters;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)  // Указывает порядок выполнения тестов по имени
public class CourierLoginTests {

    private static final String BASE_URI = "https://qa-scooter.praktikum-services.ru/api/v1";

    @Before
    @Step("Setting up base URI")
    public void setUp() {
        baseURI = BASE_URI;
    }

    @Test
    @Step("Login as a courier successfully")
    public void test1_shouldLoginCourierSuccessfully() {
        String response = given()
                .contentType("application/json")
                .body("{ \"login\": \"Simona1\", \"password\": \"123456\" }")
                .when()
                .post("/courier/login")
                .then()
                .statusCode(200)
                .body("id", notNullValue())  // Проверка, что в ответе есть поле id
                .extract().asString();

        System.out.println("Login Courier Response: " + response);  // Печатаем ответ в консоль
    }

    @Test
    @Step("Login with missing login field")
    public void test2_shouldReturnErrorWhenLoginIsMissing() {
        String response = given()
                .contentType("application/json")
                .body("{ \"password\": \"123456\" }")  // Отсутствует login
                .when()
                .post("/courier/login")
                .then()
                .statusCode(400)  // Ожидаем ошибку из-за отсутствующего поля login
                .body("message", equalTo("Недостаточно данных для входа"))
                .extract().asString();

        System.out.println("Login Missing Login Response: " + response);
    }

    @Test
    @Step("Login with missing password field")
    public void test3_shouldReturnErrorWhenPasswordIsMissing() {
        String response = given()
                .contentType("application/json")
                .body("{ \"login\": \"Simona1\",  \"password\": \"\" }")  // Отсутствует password
                .when()
                .post("/courier/login")
                .then()
                .statusCode(400)  // Ожидаем ошибку из-за отсутствующего поля password
                .body("message", equalTo("Недостаточно данных для входа"))
                .extract().asString();

        System.out.println("Login Missing Password Response: " + response);
    }

    @Test
    @Step("Login with incorrect credentials")
    public void test4_shouldReturnErrorWhenCredentialsAreIncorrect() {
        String response = given()
                .contentType("application/json")
                .body("{ \"login\": \"Simona1\", \"password\": \"wrongpassword\" }")  // Неправильный пароль
                .when()
                .post("/courier/login")
                .then()
                .statusCode(404)  // Ожидаем ошибку из-за неправильного пароля
                .body("message", equalTo("Учетная запись не найдена"))
                .extract().asString();

        System.out.println("Login Incorrect Credentials Response: " + response);
    }

    @Test
    @Step("Login with non-existing courier")
    public void test5_shouldReturnErrorWhenCourierDoesNotExist() {
        String response = given()
                .contentType("application/json")
                .body("{ \"login\": \"NonExistent\", \"password\": \"123456\" }")  // Несуществующий логин
                .when()
                .post("/courier/login")
                .then()
                .statusCode(404)  // Ожидаем ошибку 404, так как курьер не существует
                .body("message", equalTo("Учетная запись не найдена"))
                .extract().asString();

        System.out.println("Login Non-Existent Courier Response: " + response);
    }
}

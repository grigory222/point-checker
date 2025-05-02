import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {
    private WebDriver driver;
    private MainPage mainPage;
    private WebDriverWait wait;

    @BeforeEach
    public void setup() {
        FirefoxOptions options = new FirefoxOptions();
        options.setBinary("/snap/firefox/current/usr/lib/firefox/firefox");
        options.addArguments("--width=1920");
        options.addArguments("--height=1080");
        options.addArguments("--incognito");
        options.addArguments("--headless");

        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        mainPage = new MainPage(driver);

        authorize(driver);

        System.out.println(driver.getCurrentUrl());
    }

    private void authorize(WebDriver driver) {
        AuthPage authPage = new AuthPage(driver);
        driver.get("http://localhost:5173/register");
        String username = String.valueOf(System.currentTimeMillis());
        authPage.enterUsername(username);
        authPage.enterPassword("qwerty123");
        authPage.enterPasswordConfirmation("qwerty123");
        authPage.submit(false);
        wait.until(ExpectedConditions.urlContains("/main"));
        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("/main"), "Должен быть редирект на /main");
    }

    @AfterEach
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testValidPointSubmission() {
        mainPage.selectX("1");
        mainPage.enterY("2.5");
        mainPage.selectR("3");
        mainPage.submitForm();

        String result = mainPage.getLastResult();
        assertAll(
                () -> assertTrue(result.contains("X: 1"), "Некорректный X"),
                () -> assertTrue(result.contains("Y: 2.5"), "Некорректный Y"),
                () -> assertTrue(result.contains("R: 3"), "Некорректный R"),
                () -> assertTrue(result.contains("Result: false"), "Неверный результат")
        );
    }

//    @Test
//    public void testInvalidYValue() {
//        mainPage.enterY("abc");
//        mainPage.submitForm();
//
//        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
//                By.xpath("//p[contains(@class, 'MuiFormHelperText-root') and contains(@class, 'Mui-error')]")
//        ));
//
//        assertTrue(error.getText().contains("должно быть числом"),
//                "Отсутствует сообщение об ошибке валидации");
//    }
//
//    @Test
//    public void testBoundaryValues() {
//        String[][] testCases = {
//                {"-3", "0", "2", "true"},
//                {"5", "5", "5", "false"},
//                {"0", "0", "1", "true"}
//        };
//
//        for (String[] testCase : testCases) {
//            mainPage.selectX(testCase[0]);
//            mainPage.enterY(testCase[1]);
//            mainPage.selectR(testCase[2]);
//            mainPage.submitForm();
//
//            String result = mainPage.getLastResult();
//            assertTrue(result.contains("Result: " + testCase[3]),
//                    "Неверный результат для: " + String.join(",", testCase));
//        }
//    }
}
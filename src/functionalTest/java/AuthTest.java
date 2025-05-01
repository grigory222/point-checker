import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthTest {

    private WebDriver driver;
    private AuthPage authPage;
    private WebDriverWait wait;

    @BeforeEach
    public void setUp() {
        FirefoxOptions options = new FirefoxOptions();
        options.setBinary("/snap/firefox/current/usr/lib/firefox/firefox");
        options.addArguments("--headless");
        options.addArguments("--incognito");
        options.addArguments("--disable-extensions");


        driver = new FirefoxDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10)); // Устанавливаем WebDriverWait
        String baseUrl = "http://localhost:5173";
        driver.get(baseUrl + "/register");
        authPage = new AuthPage(driver);
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    public void testSuccessfulRegistration() {
        String username = String.valueOf(System.currentTimeMillis());
        authPage.enterUsername(username);
        authPage.enterPassword("qwerty123");
        authPage.enterPasswordConfirmation("qwerty123");
        authPage.submit(false);

        wait.until(ExpectedConditions.urlContains("/main"));
        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("/main"), "Должен быть редирект на /main");
    }

    @Test
    public void testRegistrationUsernameExists() {
        String username = String.valueOf(System.currentTimeMillis());
        Runnable registerUser = () -> {
            authPage.enterUsername(username);
            authPage.enterPassword("qwerty123");
            authPage.enterPasswordConfirmation("qwerty123");
            authPage.submit(false);
        };

        // Первая регистрация
        registerUser.run();
        wait.until(ExpectedConditions.urlContains("/main"));
        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("/main"), "Должен быть редирект на /main");

        // Выйти
        authPage.logout();

        // Вторая регистрация
        authPage.switchForm();
        registerUser.run();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".MuiAlert-message")));
        assertTrue(authPage.isErrorUserAlreadyExistsAlertVisible(), "Должно показаться сообщение об ошибке");
    }

    @Test
    public void testWrongUsername() {
        String username = "юзернейм";
        authPage.enterUsername(username);
        authPage.enterPassword("qwerty123");
        authPage.enterPasswordConfirmation("qwerty123");
        authPage.submit(false);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".MuiAlert-message")));
        assertTrue(authPage.isErrorWrongSymbolsUsernameVisible(), "Должно показаться сообщение об ошибке");
    }

    @Test
    public void testTooLongUsername() {
        String username = "user" + System.currentTimeMillis();
        authPage.enterUsername(username);
        authPage.enterPassword("qwerty123");
        authPage.enterPasswordConfirmation("qwerty123");
        authPage.submit(false);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".MuiAlert-message")));
        assertTrue(authPage.isErrorUsernameTooLongAlertVisible(), "Должно показаться сообщение об ошибке");
    }

    @Test
    public void testTooLongPassword() {
        String username = String.valueOf(System.currentTimeMillis());
        authPage.enterUsername(username);
        authPage.enterPassword("qwerty123qwerty123qwerty123qwerty123qwerty123qwerty123qwerty123");
        authPage.enterPasswordConfirmation("qwerty123qwerty123qwerty123qwerty123qwerty123qwerty123qwerty123");
        authPage.submit(false);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".MuiAlert-message")));
        assertTrue(authPage.isErrorPasswordTooLongAlertVisible(), "Должно показаться сообщение об ошибке");
    }

    @Test
    public void testRegistrationPasswordMismatch() {
        authPage.enterUsername("mismatchUser");
        authPage.enterPassword("abc123");
        authPage.enterPasswordConfirmation("xyz456");
        authPage.submit(false);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".MuiAlert-message")));
        assertTrue(authPage.isErrorPasswordMismatchAlertVisible(), "Должно показаться сообщение об ошибке");
    }

    @Test
    public void testSwitchToLogin() {
        authPage.switchForm();

        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(Objects.requireNonNull(driver.getCurrentUrl()).contains("/login"), "Должен быть переход на страницу входа");
    }
}

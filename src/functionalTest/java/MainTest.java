import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
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
    public void testSubmissionHistory() {
        String[][] testPoints = {
                {"1", "2", "3"},
                {"-1", "0.5", "2"},
                {"0", "0", "1"}
        };

        for (String[] point : testPoints) {
            mainPage.selectX(point[0]);
            mainPage.enterY(point[1]);
            mainPage.selectR(point[2]);
            mainPage.submitForm();
        }

        List<WebElement> rows = mainPage.getResultRows();
        assertEquals(3, rows.size(), "Должно быть 3 записи в истории");

        for (int i = 0; i < testPoints.length; i++) {
            String rowText = rows.get(i).getText();
            assertTrue(rowText.contains(String.join("\n",testPoints[i])));
        }
    }

    @Test
    public void testValidPointSubmission() {
        String[][] testData = {
                {"-2", "1", "1", "false"},
                {"0", "0", "1", "true"},
                {"5", "0", "2", "false"},
                {"0", "0", "2", "true"},
                {"1", "2.5", "3", "false"},
                {"-1", "2.5", "3", "true"},
                {"-2", "1", "4", "true"},
                {"-2", "-2", "4", "false"},
                {"-3", "-3", "5", "false"},
                {"2", "-3", "5", "true"}
        };

        for (int curTest = 0; curTest < testData.length; curTest++) {

            String currentText = getPaginationText();

            var data = testData[curTest];
            String x = data[0];
            String y = data[1];
            String r = data[2];
            String expectedResult = data[3];
            mainPage.selectX(x);
            mainPage.enterY(y);
            mainPage.selectR(r);
            mainPage.submitForm();

            // ждём пока придет ответ
            waitAnswerFromServer(currentText);


            if (curTest % 5 == 0) {
                mainPage.goToPage(curTest / 5);
            }
            String actualResult = mainPage.getLastResult();



            assertAll(
                    String.format("Проверка для X=%s, Y=%s, R=%s", x, y, r),
                    () -> assertTrue(actualResult.contains("X: " + x), "Некорректный X"),
                    () -> assertTrue(actualResult.contains("Y: " + y), "Некорректный Y"),
                    () -> assertTrue(actualResult.contains("R: " + r), "Некорректный R"),
                    () -> assertTrue(actualResult.contains("Result: " + expectedResult), "Неверный результат")
            );
        }
    }

    @Test
    public void testYValidation() {
        String correctX = "1";
        String correctR = "1";
        String[] invalidValues = {"8", "-6", "3.01", "-5.01"};

        for (String value : invalidValues) {

            mainPage.selectX(correctX);
            mainPage.enterY(value);
            mainPage.selectR(correctR);
            mainPage.submitForm();

            assertTrue(mainPage.isYErrorDisplayed(),
                    "Должна быть ошибка для значения Y = " + value);

            assertTrue(mainPage.isYInputHighlightedAsError(),
                    "Поле Y должно быть подсвечено как ошибочное для значения " + value);
        }

        String[] validValues = {"-5", "3", "0asd", "1a.5", "-2.9s9"};

        for (String value : validValues) {

            mainPage.selectX(correctX);
            mainPage.enterY(value);
            mainPage.selectR(correctR);
            mainPage.submitForm();

            assertFalse(mainPage.isYErrorDisplayed(),
                    "Не должно быть ошибки для валидного значения Y = " + value);

            assertFalse(mainPage.isYInputHighlightedAsError(),
                    "Поле Y не должно быть подсвечено для валидного значения " + value);
        }
    }

    @Test
    public void testCanvasClick() {
        int scaleX = 20;
        int scaleY = 20;
        Object[][] testData = {
                {60, 50, "5", "false"},
                {-40, 30, "5", "true"},
                {40, -30, "5", "true"},
        };

        for (Object[] data : testData) {
            int mouseX = (int) data[0];
            int mouseY = (int) data[1];
            String x =  String.valueOf((float) mouseX / scaleX);
            String xInt =  String.valueOf(mouseX / scaleX);
            String y =  String.valueOf((float) mouseY / scaleY);
            String yInt =  String.valueOf( mouseY / scaleY);
            String r = (String) data[2];
            String expectedResult = (String) data[3];

            String currentText = getPaginationText();
            mainPage.selectR(r);
            mainPage.clickOnCanvas(mouseX, mouseY);
            waitAnswerFromServer(currentText);

            String actualResult = mainPage.getLastResult();


            assertAll(
                    String.format("Проверка для X=%s, Y=%s, R=%s", x, y, r),
                    () -> assertTrue(actualResult.contains("X: " + x) || actualResult.contains("X: " + xInt), "Некорректный X"),
                    () -> assertTrue(actualResult.contains("Y: " + y) || actualResult.contains("Y: " + yInt), "Некорректный Y"),
                    () -> assertTrue(actualResult.contains("R: " + r), "Некорректный R"),
                    () -> assertTrue(actualResult.contains("Result: " + expectedResult), "Неверный результат")
            );
        }
    }
    @Test
    public void testEmptyFieldsAlert() {
        mainPage.enterY("");
        mainPage.submitForm();

        WebElement alert = mainPage.getAlert();

        assertAll(
                () -> assertTrue(alert.isDisplayed(), "Алерт должен быть видимым"),
                () -> assertEquals("Должны быть заполнены все поля",
                        alert.findElement(By.cssSelector(".MuiAlert-message")).getText(),
                        "Неверный текст алерта"),
                () -> assertTrue(alert.findElement(
                                By.cssSelector("[data-testid='ErrorOutlineIcon']")).isDisplayed(),
                        "Должна отображаться иконка ошибки")
        );
    }

    @Test
    public void testAlertDisappearsAfterCorrectInput() {
        mainPage.submitForm();
        WebElement alert = mainPage.getAlert();

        mainPage.selectX("1");
        mainPage.enterY("2.5");
        mainPage.selectR("3");
        mainPage.submitForm();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector(".MuiAlert-standardError")
        ));
    }

    private String getPaginationText() {
        WebElement pagination = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".MuiTablePagination-displayedRows")
        ));
        return pagination.getText();
    }

    // ждем пока изменится количество строк
    private void waitAnswerFromServer(String oldText) {
        wait.until(ExpectedConditions.not(
                ExpectedConditions.textToBe(
                        By.cssSelector(".MuiTablePagination-displayedRows"),
                        oldText
                )
        ));
    }

}
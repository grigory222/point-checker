import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import java.util.List;

public class MainPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public MainPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public WebElement xInput() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//label[contains(., 'Координата X')]/following::input[1]")
        ));
    }

    public WebElement yInput() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//label[contains(., 'Координата Y')]/following::input[1]")
        ));
    }

    public WebElement rInput() {
        return wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//label[contains(., 'Радиус')]/following::input[1]")
        ));
    }

    public WebElement submitButton() {
        return wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(., 'Отправить')]")
        ));
    }

    public void selectX(String value) {
        selectWithArrowKeys(xInput(), value);
    }

    public void enterY(String value) {
        WebElement input = yInput();
        input.click();
        input.sendKeys(Keys.CONTROL + "a");
        input.sendKeys(Keys.DELETE);
        input.sendKeys(value);
    }

    public void selectR(String value) {
        selectWithArrowKeys(rInput(), value);
    }

    private void selectWithArrowKeys(WebElement input, String targetValue) {
        input.click();
        input.sendKeys(Keys.CONTROL + "a");
        input.sendKeys(Keys.DELETE);

        input.sendKeys(targetValue);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ul.MuiAutocomplete-listbox")));
        List<WebElement> options = driver.findElements(By.cssSelector("li.MuiAutocomplete-option"));

        for (WebElement option : options) {
            if (option.getText().equals(targetValue)) {
                option.click();
                return;
            }
        }

    }


    public List<WebElement> getResultRows() {
        return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector(".MuiDataGrid-row")
        ));
    }

    public String getLastResult() {
        List<WebElement> rows = getResultRows();
        if (rows.isEmpty()) return "";

        WebElement lastRow = rows.getLast();
        return String.format("X: %s, Y: %s, R: %s, Result: %s",
                getCellValue(lastRow, "x"),
                getCellValue(lastRow, "y"),
                getCellValue(lastRow, "r"),
                getCellValue(lastRow, "result")
        );
    }

    private String getCellValue(WebElement row, String field) {
        return row.findElement(By.cssSelector(String.format("[data-field='%s']", field))).getText();
    }

    public void submitForm() {
        submitButton().click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector(".MuiCircularProgress-root")
        ));
    }
}
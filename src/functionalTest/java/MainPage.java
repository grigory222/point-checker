import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
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

    // MuiGrid
    public List<WebElement> getResultRows() {
        return wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector(".MuiDataGrid-row")
        ));
    }

    public String getLastResult() {
        List<WebElement> rows = getResultRows();
        if (rows.isEmpty()) return "";

        WebElement lastRow = rows.get(rows.size() - 1);
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

    public void goToPage(int targetPage) {
        String oldText;

        WebElement paginationInfo = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector(".MuiTablePagination-displayedRows")
        ));

        String currentText = paginationInfo.getText();
        int currentPage = Integer.parseInt(currentText.split("–")[0]) / 5;
        if (currentPage == targetPage) {
            return;
        }
        while (currentPage != targetPage) {
            if (currentPage < targetPage) {
                WebElement nextButton = driver.findElement(
                        By.cssSelector("button[aria-label='Go to next page']:not([disabled])")
                );
                oldText = paginationInfo.getText();
                nextButton.click();
                currentPage++;
            } else {
                WebElement prevButton = driver.findElement(
                        By.cssSelector("button[aria-label='Go to previous page']:not([disabled])")
                );
                oldText = paginationInfo.getText();
                prevButton.click();
                currentPage--;
            }


            String finalOldText = oldText;
            wait.until(driver -> !driver.findElement(
                    By.cssSelector(".MuiTablePagination-displayedRows")
            ).getText().equals(finalOldText));
        }
    }



    public void submitForm() {
        submitButton().click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.cssSelector(".MuiCircularProgress-root")
        ));
    }

    public boolean isYErrorDisplayed() {
        try {
            WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("#\\:rb\\:-helper-text")
            ));
            return error.isDisplayed() &&
                    error.getText().equals("Значение Y должно быть в диапазоне [-5;3]");
        } catch (TimeoutException e) {
            return false;
        }
    }

    public boolean isYInputHighlightedAsError() {
        try {
            WebElement yInputWrapper = driver.findElement(By.cssSelector(".MuiInputBase-root.Mui-error"));
            return yInputWrapper.isDisplayed();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public WebElement getAlert() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".MuiAlert-standardError")
        ));
    }

    public void clickOnCanvas(int x, int y) {
        WebElement canvas = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("myCanvas")
        ));

        new Actions(driver)
                .moveToElement(canvas, x, -y)
                .click()
                .perform();
    }

    private WebElement getAvatarIcon() {
        return driver.findElement(By.xpath("//div[contains(@class, 'MuiAvatar-root')]"));
    }

    private WebElement getLogoutButton() {
        getAvatarIcon().click();
        return driver.findElement(By.xpath("//a[contains(text(), 'Выход')]"));

    }

    public void logout(){
        getLogoutButton().click();
    }

}
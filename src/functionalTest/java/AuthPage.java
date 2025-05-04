import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class AuthPage {
    private final WebDriver driver;

    public AuthPage(WebDriver driver) {
        this.driver = driver;
    }

    private WebElement getUsernameInput() {
        return driver.findElement(By.xpath("//label[contains(text(), 'Имя пользователя')]/following::input[1]"));
    }

    private WebElement getPasswordInput() {
        return driver.findElement(By.xpath("//input[@type='password'][1]"));
    }

    private WebElement getPasswordConfirmInput() {
        var passwords = driver.findElements(By.xpath("//input[@type='password']"));
        return passwords.size() > 1
                ? passwords.get(1)
                : null;
    }

    private WebElement getSubmitButton(String label) {
        return driver.findElement(By.xpath("//button[contains(text(), '" + label + "')]"));
    }

    private WebElement getMainButton() {
        return driver.findElement(By.xpath("//a[contains(text(), 'Главная')]"));
    }

    private WebElement getAvatarIcon() {
        return driver.findElement(By.xpath("//div[contains(@class, 'MuiAvatar-root')]"));
    }

    private WebElement getLogoutButton() {
        getAvatarIcon().click();
        return driver.findElement(By.xpath("//a[contains(text(), 'Выход')]"));

    }

    private WebElement getSwitchLink() {
        return driver.findElement(By.xpath("//p[contains(text(),'Ещё нет аккаунта?') or contains(text(),'Уже есть аккаунт?')]"));
    }

    public void logout(){
        getLogoutButton().click();
    }

    public void goToMainPage(){
        getMainButton().click();
    }

    public void enterUsername(String username) {
        getUsernameInput().clear();
        getUsernameInput().sendKeys(username);
    }

    public void enterPassword(String password) {
        getPasswordInput().clear();
        getPasswordInput().sendKeys(password);
    }

    public void enterPasswordConfirmation(String password) {
        WebElement confirm = getPasswordConfirmInput();
        if (confirm != null) {
            confirm.clear();
            confirm.sendKeys(password);
        }
    }

    public void submit(boolean isLogin) {
        getSubmitButton(isLogin ? "Вход" : "Регистрация").click();
    }

    public void switchForm() {
        WebElement link = getSwitchLink();
        link.click();
    }

    public boolean isErrorPasswordMismatchAlertVisible() {
        return !driver.findElements(By.xpath("//div[contains(@class, 'MuiAlert') and contains(text(),'Пароли не совпадают')]")).isEmpty();
    }

    public boolean isErrorUserAlreadyExistsAlertVisible() {
        return !driver.findElements(By.xpath("//div[contains(@class, 'MuiAlert') and contains(text(),'User with this name already exists')]")).isEmpty();
    }

    public boolean isErrorUsernameTooLongAlertVisible() {
        return !driver.findElements(By.xpath("//div[contains(@class, 'MuiAlert') and contains(text(),'signup.arg0.username: Username must be between 4 and 16 characters')]")).isEmpty();
    }

    public boolean isErrorPasswordTooLongAlertVisible() {
        return !driver.findElements(By.xpath("//div[contains(@class, 'MuiAlert') and contains(text(),'signup.arg0.password: Password must be between 8 and 50 characters')]")).isEmpty();
    }

    public boolean isErrorWrongSymbolsUsernameVisible() {
        return !driver.findElements(By.xpath("//div[contains(@class, 'MuiAlert') and contains(text(),'signup.arg0.username: Username must be alphanumeric with underscores')]")).isEmpty();
    }



}

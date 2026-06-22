package br.fieltorcedor;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class FielTorcedorAutomation {
    public static void main(String[] args) {
        // Configurar o caminho para o ChromeDriver
        System.setProperty("webdriver.chrome.driver", "C:/Users/brunello/Documents/chromedriver-win64/chromedriver.exe"); // substitua pelo caminho real

        // Inicializar o driver do Chrome
        WebDriver driver = new ChromeDriver();

        // Configurar o WebDriverWait com timeout de 10 segundos
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            // Acessa o site
            driver.get("https://www.fieltorcedor.com.br/");

            // Espera e clica no elemento desejado (por exemplo, botão de login)
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='header']/div/div/div[2]/div[2]/div/a[1]"))).click();

            // Espera que o campo de username esteja visível e insere o valor "41973078880"
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='id_username']"))).sendKeys("41973078880");

            // Espera que o campo de password esteja visível e insere o valor "senha"
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id='id_password']"))).sendKeys("Nellosbru1@");

            driver.switchTo().frame(driver.findElement(By.xpath("//iframe[contains(@title, 'reCAPTCHA')]")));
            WebElement captchaCheckbox = driver.findElement(By.cssSelector("#recaptcha-anchor"));
            captchaCheckbox.click();

            // Voltar para o contexto principal
            driver.switchTo().defaultContent();

            // Aguarde alguns segundos para a verificação manual, se necessário
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            // Espera e clica no botão para enviar o formulário
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id='main']/div/div/div/div/form/button"))).click();

            // Aguardar alguns segundos para visualizar o resultado (opcional)
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Fecha o navegador
            driver.quit();
        }
    }
}
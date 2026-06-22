package steps;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class KimonoSteps {

	private Playwright playwright;
	private Browser browser;
	private Page page;

	@Before
	public void setup() {
		boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
		playwright = Playwright.create();
		browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
		page = browser.newPage();
	}

	@After
	public void tearDown() {
		if (browser != null) browser.close();
		if (playwright != null) playwright.close();
	}

	@Given("I access the kimonos page")
	public void accessKimonosPage() {
		page.navigate("https://g13bjjstore.com.br/kimonos/c");
	}

	@Given("I open the Kimono G13BJJ Branco product page")
	public void openProductPage() {
		// try to find an anchor that contains both 'kimono g13' and 'branco'
		Object phObj = page.evaluate("() => { const a = Array.from(document.querySelectorAll('a')).find(x=> (x.innerText||x.textContent||'').toLowerCase().includes('kimono g13') && (x.innerText||x.textContent||'').toLowerCase().includes('branco')); return a ? a.href : null; }");
		String productHref = phObj == null ? null : phObj.toString();
		if (productHref == null) {
			productHref = "https://g13bjjstore.com.br/kimono-g13bjj---branco/p"; // fallback
		}
		page.navigate(productHref);
	}

	@When("I select size A1")
	public void selectSize() {
		try {
			// look for any element that contains the exact token A1 and click it
			Locator size = page.locator("text=/\\bA1\\b/");
			if (size.count() > 0) {
				size.first().click();
			}
		} catch (Exception e) {
			// ignore if not present
		}
	}

	@Then("the COMPRAR button should be visible")
	public void validateButton() {
		int intervalMs = Integer.parseInt(System.getProperty("kimono.interval.ms", "2000"));
		long timeoutMs = Long.parseLong(System.getProperty("kimono.timeout.ms", "60000"));
		long start = System.currentTimeMillis();
		boolean visible = false;
		while (System.currentTimeMillis() - start < timeoutMs) {
			try {
				Locator comprar = page.locator("text=COMPRAR").first();
				if (comprar.isVisible()) {
					visible = true;
					break;
				}
			} catch (Exception ignored) {
			}
			try {
				page.reload();
			} catch (Exception ignored) {
			}
			try {
				Thread.sleep(intervalMs);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				break;
			}
		}
		Assert.assertTrue(visible, "COMPRAR button is not visible after waiting " + timeoutMs + " ms");
	}
}
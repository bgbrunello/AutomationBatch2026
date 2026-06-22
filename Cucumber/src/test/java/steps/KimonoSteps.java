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
			//Thread.sleep(10000);
			// look for any element that contains the exact token A1 and click it
			Locator size = page.locator("text=/\\bA1\\b/");
			if (size.count() > 0) {
				size.first().click();
			//	Thread.sleep(10000);
			}
		} catch (Exception e) {
			// ignore if not present
		}
	}

	// helper to attempt selecting A1; used on retries after reload
	private void clickA1IfPresent() {
		try {
			// hide overlays that may reappear
			try { page.evaluate("() => { document.querySelectorAll('#promoFlyer, .flyer-overlay.open, dialog, [role=\\\"dialog\\\"], .modal, .overlay').forEach(e=>{e.style.display='none'; e.setAttribute('aria-hidden','true');}); }"); } catch (Exception ignored) {}
			// click any candidate element that matches A1 (covers buttons, labels, options)
			try { page.evaluate("(t) => { const candidates = Array.from(document.querySelectorAll('button, a, label, span, li, option')); for(const n of candidates){ const txt=(n.innerText||n.textContent||'').trim(); if(new RegExp('\\\\b'+t+'\\\\b','i').test(txt)){ try{ n.click(); }catch(e){} break; } } }", "A1"); } catch (Exception ignored) {}
			try { Thread.sleep(1200); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
		} catch (Exception ignored) {
		}
	}

	@Then("the COMPRAR button should be visible")
	public void validateButton() {
		int intervalMs = Integer.parseInt(System.getProperty("kimono.interval.ms", "2000"));
		long timeoutMs = Long.parseLong(System.getProperty("kimono.timeout.ms", "60000"));
		long start = System.currentTimeMillis();
		boolean visible = false;
		while (System.currentTimeMillis() - start < timeoutMs) {
			// attempt to select A1 again in case reload removed previous selection
			clickA1IfPresent();

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
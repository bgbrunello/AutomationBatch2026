package tests;

import org.testng.annotations.Test;
import org.testng.Assert;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;

import base.BaseTest;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FirstTest extends BaseTest {

	@Test
	public void verifyTitle() {
		page.navigate("https://google.com");
		takeScreenshot("verifyTitle", "afterNavigate");
		if (page.isVisible("button:has-text('Accept all')")) {
			page.click("button:has-text('Accept all')");
			takeScreenshot("verifyTitle", "afterAcceptAll");
		}

		System.out.println("The page tittle is : " + page.title());
		takeScreenshot("verifyTitle", "end");

	}

	@Test
	public void getWeatherWithPlaywrightRequest() {
		// delegate to API helper
		API.getWeatherWithPlaywrightRequest(playwright);
	}

	@Test
	public void openWay2AutomationAndMemberLogin() {
		// Navigate to the site
		page.navigate("https://way2automation.com/");
		takeScreenshot("openWay2AutomationAndMemberLogin", "afterNavigate");

		// Remove/hide the Lifetime Membership popup and any promo overlay that blocks clicks
		page.evaluate("() => { document.querySelectorAll('#promoFlyer, .flyer-overlay.open, dialog, [role=\"dialog\"]').forEach(e=>{e.style.display='none'; e.setAttribute('aria-hidden','true');}); }");
		takeScreenshot("openWay2AutomationAndMemberLogin", "afterRemoveOverlays");

		// Click the Member Login link — it opens a new popup/tab. Wait for the popup and switch to it.
		com.microsoft.playwright.Page loginPage = page.waitForPopup(() -> {
			page.click("a:has-text(\"Member Login\")");
		});

		// Wait for the login page to load and print info
		loginPage.waitForLoadState();
		// also capture the popup/login page
		try {
			String ts = java.time.LocalDateTime.now().toString().replace(':','-');
			Path out = Paths.get(screenshotDir, "openWay2AutomationAndMemberLogin_loginpage_" + ts + ".png");
			loginPage.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions().setPath(out));
			System.out.println("Saved popup screenshot: " + out.toAbsolutePath());
		} catch (Exception e) {
			System.err.println("Failed to capture popup screenshot: " + e.getMessage());
		}
		System.out.println("Member login URL: " + loginPage.url());
		System.out.println("Member login title: " + loginPage.title());
	}

	@Test
	public void ensureComprarAvailableForKimonoBranco() throws InterruptedException {
		// Go to the kimonos listing and find the Branco product
		page.navigate("https://g13bjjstore.com.br/kimonos/c");
		takeScreenshot("ensureComprarAvailableForKimonoBranco", "afterNavigateListing");

		Object phObj = page.evaluate("() => { const a = Array.from(document.querySelectorAll('a')).find(x=> (x.innerText||x.textContent||'').toLowerCase().includes('kimono g13') && (x.innerText||x.textContent||'').toLowerCase().includes('branco')); return a ? a.href : null; }");
		String productHref = phObj == null ? null : phObj.toString();
		if (productHref == null) {
			// fallback to known path
			productHref = "https://g13bjjstore.com.br/kimono-g13bjj---branco/p";
		}

		page.navigate(productHref);
		takeScreenshot("ensureComprarAvailableForKimonoBranco", "afterNavigateProduct");

		int intervalMs = 2_000; // retry interval
		boolean available = false;

		// Loop indefinitely until the COMPRAR button becomes available for A1.
		// This will reload the product page each iteration to pick up stock/availability changes.
		while (true) {
			// hide overlays that may block interaction
			page.evaluate("() => { document.querySelectorAll('#promoFlyer, .flyer-overlay.open, dialog, [role=\\\"dialog\\\"], .modal, .overlay').forEach(e=>{e.style.display='none'; e.setAttribute('aria-hidden','true');}); }");
			takeScreenshot("ensureComprarAvailableForKimonoBranco", "afterHideOverlays");

			// attempt to select size A1 if present
			page.evaluate("() => { const candidates = Array.from(document.querySelectorAll('button, a, label, span, li, option')); for(const n of candidates){ const t=(n.innerText||n.textContent||'').trim(); if(/\\bA1\\b/.test(t)){ try{ n.click(); }catch(e){} break; } } }");
			takeScreenshot("ensureComprarAvailableForKimonoBranco", "afterSelectA1Attempt");
			Thread.sleep(10000);
			// check for COMPRAR presence and visibility
			try {
				if (page.locator("text=COMPRAR").first().isVisible()) {
					available = true;
					break;
				}
			} catch (Exception e) {
				// locator not found or other transient error - continue retrying
			}
			// Not available yet — reload the page and retry after a short pause.
			System.out.println("COMPRAR not available yet, reloading and retrying...");
			try {
				page.reload();
			} catch (Exception rex) {
				// ignore reload errors and continue
			}
			takeScreenshot("ensureComprarAvailableForKimonoBranco", "afterReload");
			Thread.sleep(intervalMs);
		}

		System.out.println("COMPRAR available: " + available);
		Assert.assertTrue(available, "COMPRAR button was not available within timeout for Kimono G13BJJ - Branco");
	}

//	public static void main(String[] args) {
//
//		try (Playwright playwright = Playwright.create()) {
//
//			Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
//			Page page = browser.newPage();
//			page.navigate("https://google.com");
//			System.out.println("The page tittle is : " + page.title());
//			browser.close();
//		}
//	}
}

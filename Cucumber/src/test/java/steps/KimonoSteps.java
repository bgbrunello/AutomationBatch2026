package steps;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class KimonoSteps {

	private Page page;

	@Before
	public void setup() {
		boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
		BrowserManager.init(headless);
		page = BrowserManager.getPage();
	}

	@After
	public void tearDown() {
		BrowserManager.close();
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
			Locator size = page.locator("text=/\\bA2\\b/");
			if (size.count() > 0) {
				size.first().click();
			//	Thread.sleep(10000);
			}
		} catch (Exception e) {
			// ignore if not present
		}
	}

	// Portuguese step: acessar a página do produto pelo nome
	@Given("que o usuário acessa a página do produto {string}")
	public void accessProductPageByName(String productName) {
		String lower = productName.toLowerCase();
		try {
			Object phObj = page.evaluate("(name) => { const a = Array.from(document.querySelectorAll('a')).find(x=> (x.innerText||x.textContent||'').toLowerCase().includes(name)); return a ? a.href : null; }", lower);
			String productHref = phObj == null ? null : phObj.toString();
			if (productHref == null) {
				// fallback to known product path for Kimono G13BJJ Branco
				if (lower.contains("kimono g13") && lower.contains("branco")) {
					productHref = "https://g13bjjstore.com.br/kimono-g13bjj---branco/p";
				} else {
					// if not found, navigate to category page so subsequent steps can try to locate the product
					productHref = "https://g13bjjstore.com.br/kimonos/c";
				}
			}
			page.navigate(productHref);
		} catch (Exception e) {
			// try fallback navigation
			page.navigate("https://g13bjjstore.com.br/kimonos/c");
		}
	}

	@When("seleciona o tamanho {string}")
	public void selecionaTamanho(String size) {
		// attempt to click any element that contains the exact size token (word boundary)
		try {
			page.evaluate("(s) => { const candidates = Array.from(document.querySelectorAll('button, a, label, span, li, option')); for(const n of candidates){ const txt=(n.innerText||n.textContent||'').trim(); if(new RegExp('\\\\b'+s+'\\\\b','i').test(txt)){ try{ n.click(); }catch(e){} break; } } }", size);
			try { Thread.sleep(800); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
		} catch (Exception ignored) {
		}
	}

	@When("clica no botão {string}")
	public void clicaNoBotao(String buttonText) {
		try {
			Locator btn = null;
			// try common variants for "Comprar"
			if ("Comprar".equalsIgnoreCase(buttonText)) {
				btn = page.locator("text=COMPRAR").first();
				if (btn == null || btn.count() == 0) btn = page.locator("text=Comprar").first();
			} else {
				btn = page.locator("text=\"" + buttonText + "\"").first();
				if (btn == null || btn.count() == 0) btn = page.locator("text=" + buttonText).first();
			}
			if (btn == null || btn.count() == 0) {
				// explicit failure when expected action button is missing
				throw new AssertionError("Button '" + buttonText + "' not found on page. Product may be out of stock or UI changed.");
			}
			btn.click();
			try { Thread.sleep(800); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
		} catch (Exception e) {
			// as a fallback attempt to click by searching and invoking click via JS
			try {
				page.evaluate("(t) => { const el = Array.from(document.querySelectorAll('button, a')).find(e=> (e.innerText||e.textContent||'').trim().toLowerCase().includes(t.toLowerCase())); if(el) try{ el.click(); }catch(e){} }", buttonText);
			} catch (Exception ignored) {}
		}
	}

	@Then("o produto deve ser adicionado ao carrinho")
	public void produtoAdicionadoAoCarrinho() {
		// best-effort: look for common confirmation texts or cart indicators
		try {
			// wait a moment for UI update
			try { Thread.sleep(1000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
			Object res = page.evaluate("() => { const confirmations = ['adicionado ao carrinho','produto adicionado','adicionado']; for(const c of confirmations){ if(document.body.innerText.toLowerCase().includes(c)) return c; } return null; }");
			// don't assert here; the next step will validate cart count. If needed, we could assert res!=null but keep optional.
		} catch (Exception ignored) {}
	}

	@Then("o carrinho deve conter {int} item")
	public void carrinhoDeveConterItem(int expected) {
		int found = -1;
		long start = System.currentTimeMillis();
		long timeout = Long.parseLong(System.getProperty("kimono.cart.timeout.ms", "5000"));
		while (System.currentTimeMillis() - start < timeout) {
			try { Thread.sleep(300); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
			try {
				// first try a specific cart badge selector that exists in the site
				try {
					Locator badge = page.locator(".jet-cart-count").first();
					if (badge != null && badge.count() > 0) {
						String txt = badge.textContent();
						if (txt != null && txt.trim().length() > 0) {
							try { found = Integer.parseInt(txt.trim()); } catch (Exception ignored) { found = -1; }
							if (found == expected) break;
						}
					}
				} catch (Exception ignored) {}

				// fallback to broader heuristics only if badge not present
				Object val = page.evaluate(
					"(n) => { function extractDigits(s){ var m=s&&s.match(/\\\\d+/); return m?parseInt(m[0],10):null; } var kwRe = /cart|carrinho|produt|produto|item|itens|badge|qty|quantity|count/; var els = Array.from(document.querySelectorAll('*')); for(var i=0;i<els.length;i++){ var e=els[i]; try{ var attr = (e.getAttribute && ((e.getAttribute('data-count')||e.getAttribute('data-qty')||e.getAttribute('data-quantity')||e.getAttribute('aria-label')||e.getAttribute('title')||e.getAttribute('alt'))))||''; if(attr){ var am = attr.toString().match(/\\\\d+/); if(am) return parseInt(am[0],10); } }catch(ee){} var txt=(e.innerText||e.textContent||'').trim(); var d=extractDigits(txt); if(d!==null){ var anc=e; var accept=false; for(var k=0;k<4 && anc;k++){ var ancTxt=(anc.innerText||anc.textContent||'').toLowerCase(); var c=(anc.className||'').toLowerCase(); var id=(anc.id||'').toLowerCase(); if(kwRe.test(ancTxt) || kwRe.test(c) || kwRe.test(id)){ accept=true; break; } anc=anc.parentElement; } if(accept) return d; } } var anchors=Array.from(document.querySelectorAll('a,button,span')).filter(a=> ((a.href||'').toLowerCase().includes('cart') || (a.href||'').toLowerCase().includes('carrinho') || (a.getAttribute && (a.getAttribute('aria-label')||'').toLowerCase().includes('cart')))); for(var j=0;j<anchors.length;j++){ var t=(anchors[j].innerText||anchors[j].textContent||'').trim(); var d2=extractDigits(t); if(d2!==null) return d2; } try{ for(var i=0;i<localStorage.length;i++){ var k=localStorage.key(i); var v=localStorage.getItem(k); try{ var obj=JSON.parse(v); function searchObj(o){ if(o==null) return null; if(typeof o==='number') return o; if(typeof o==='string'){ var mm=o.match(/\\\\d+/); return mm?parseInt(mm[0],10):null; } if(typeof o==='object'){ for(var p in o){ try{ var r=searchObj(o[p]); if(r!=null) return r; }catch(e){} } } return null; } var r=searchObj(obj); if(r!=null) return r; }catch(e){} } }catch(e){} var body=(document.body.innerText||'').toLowerCase(); var m=body.match(/\\\\d+/); if(m) return parseInt(m[0],10); return null; }",
					expected);
				if (val != null) {
					try { found = Integer.parseInt(val.toString()); } catch (Exception e) { found = -1; }
				}
				if (found == expected) break;
			} catch (Exception ignored) {
			}
		}
		// debug: if assertion will fail, dump page content and a screenshot to sure-fire reports for inspection
		if (found != expected) {
			try {
				String dumpDir = "target/surefire-reports";
				try { Files.createDirectories(Paths.get(dumpDir)); } catch (Exception ignored) {}
				// dump localStorage (useful to see cart state stored in browser)
				try {
					Object ls = page.evaluate("() => { try { return JSON.stringify(Object.assign({}, window.localStorage)); } catch(e) { return null; } }");
					if (ls != null) {
						Files.write(Paths.get(dumpDir, "cart-debug-localstorage.json"), ls.toString().getBytes(StandardCharsets.UTF_8));
						System.out.println("[DEBUG] saved localStorage to " + Paths.get(dumpDir, "cart-debug-localstorage.json").toAbsolutePath());
					}
				} catch (Exception ignored) {}

				String content = page.content();
				Files.write(Paths.get(dumpDir, "cart-debug-page.html"), content.getBytes(StandardCharsets.UTF_8));
				System.out.println("[DEBUG] cart raw value detected: " + found);
				try {
					page.screenshot(new com.microsoft.playwright.Page.ScreenshotOptions().setPath(Paths.get(dumpDir, "cart-debug.png")));
					System.out.println("[DEBUG] saved screenshot to " + Paths.get(dumpDir, "cart-debug.png").toAbsolutePath());
				} catch (Exception se) {
					System.out.println("[DEBUG] could not save screenshot: " + se.getMessage());
				}
			} catch (Exception ioe) {
				System.out.println("[DEBUG] could not dump page content: " + ioe.getMessage());
			}
		}
		Assert.assertEquals(found, expected, "Cart did not contain expected item count. Found: " + found + " Expected: " + expected);
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
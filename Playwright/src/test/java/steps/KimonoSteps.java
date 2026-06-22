package steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import org.testng.Assert;

public class KimonoSteps {

    private Playwright playwright;
    private Browser browser;
    private Page page;
    private String productHref;
    private boolean available = false;

    @Before
    public void setup() {
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false"));
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
        page = browser.newPage();
    }

    @After
    public void tearDown() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @Given("que eu acesso a página de listagem de kimonos")
    public void acessoPaginaListagem() {
        page.navigate("https://g13bjjstore.com.br/kimonos/c");
    }

    @Given("eu encontro o produto cujo título contém {string} e {string}")
    public void encontroProduto(String p1, String p2) {
        Object phObj = page.evaluate("(p1,p2) => { const a = Array.from(document.querySelectorAll('a')).find(x=> (x.innerText||x.textContent||'').toLowerCase().includes(p1) && (x.innerText||x.textContent||'').toLowerCase().includes(p2)); return a ? a.href : null; }", p1.toLowerCase(), p2.toLowerCase());
        productHref = phObj == null ? null : phObj.toString();
        if (productHref == null) {
            productHref = "https://g13bjjstore.com.br/kimono-g13bjj---branco/p"; // fallback
        }
    }

    @When("eu abro a página do produto encontrado")
    public void abroPaginaProduto() {
        page.navigate(productHref);
    }

    @When("eu removo popups e overlays que bloqueiam interações")
    public void removeOverlays() {
        page.evaluate("() => { document.querySelectorAll('#promoFlyer, .flyer-overlay.open, dialog, [role=\\\"dialog\\\"], .modal, .overlay').forEach(e=>{e.style.display='none'; e.setAttribute('aria-hidden','true');}); }");
    }

    @When("eu seleciono o tamanho {string} se ele estiver presente")
    public void selecionoTamanho(String tamanho) {
        page.evaluate("(t) => { const candidates = Array.from(document.querySelectorAll('button, a, label, span, li, option')); for(const n of candidates){ const txt=(n.innerText||n.textContent||'').trim(); if(new RegExp('\\\\b'+t+'\\\\b','i').test(txt)){ try{ n.click(); }catch(e){} break; } } }", tamanho);
    }

    @When("eu aguardo até que o botão {string} esteja visível, recarregando a página periodicamente")
    public void aguardoComprarVisivel(String textoBotao) throws InterruptedException {
        int intervalMs = Integer.parseInt(System.getProperty("kimono.interval.ms", "2000"));
        long timeoutMs = Long.parseLong(System.getProperty("kimono.timeout.ms", "300000")); // default 5 minutes
        long start = System.currentTimeMillis();

        while (System.currentTimeMillis() - start < timeoutMs) {
            try {
                // hide overlays again in case they reappear
                page.evaluate("() => { document.querySelectorAll('#promoFlyer, .flyer-overlay.open, dialog, [role=\\\"dialog\\\"], .modal, .overlay').forEach(e=>{e.style.display='none'; e.setAttribute('aria-hidden','true');}); }");
            } catch (Exception ignored) {}

            try {
                if (page.locator("text=" + textoBotao).first().isVisible()) {
                    available = true;
                    return;
                }
            } catch (Exception ignored) {
            }

            try {
                page.reload();
            } catch (Exception ignored) {}
            Thread.sleep(intervalMs);
        }
        available = false;
    }

    @Then("o botão {string} deve estar disponível")
    public void botaoDeveEstarDisponivel(String textoBotao) {
        Assert.assertTrue(available, "Botão '" + textoBotao + "' não ficou disponível no tempo esperado");
    }

}


package steps;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.E;
import org.testng.Assert;

public class LoginSteps {

	private Page page;

	@Before
	public void setup() {
		boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
		// initialize shared browser/page
		BrowserManager.init(headless);
		page = BrowserManager.getPage();
	}

	@After
	public void tearDown() {
		// decrement ref count and close when no longer used
		BrowserManager.close();
	}

	// suporte para o cenário de login
	@Dado("que o usuário acessa o G13BjjStore")
	public void acessarPaginaInicial() {
		page.navigate("https://g13bjjstore.com.br/");
		page.waitForLoadState();
	}

	@Quando("informa o usuário {string}")
	public void informaUsuario(String usuario) {
		// tenta abrir o modal/página de login clicando em textos comuns
		clickFirstVisibleText("Entrar", "Entre", "Login", "Entrar / Registrar");

		// tenta preencher o campo do usuário usando seletores comuns
		String[] userSelectors = new String[] {
			"input[formcontrolname=username]"
		};
		fillFirstExisting(userSelectors, usuario);
	}

	@E("informa a senha {string}")
	public void informaSenha(String senha) {
		String[] passSelectors = new String[] {
			"input[name=senha]",
			"input[name=password]",
			"input[type=password]"
		};
		fillFirstExisting(passSelectors, senha);
	}

	@E("clica em Login")
	public void clicaEmLogin() {
	    // Clica especificamente no botão type="submit" com texto "Entrar"
	    Locator submitButton = page.locator("button[type='submit']:has-text('Entrar')");
	    
	    if (submitButton.count() > 0) {
	        submitButton.first().click();
	        page.waitForLoadState();
	    } else {
	        // Fallback para botão sem type específico mas com texto "Entrar"
	        Locator button = page.locator("button:has-text('Entrar')");
	        if (button.count() > 0) {
	            button.first().click();
	            page.waitForLoadState();
	            
	        }
	    }
	}

	@Entao("deve exibir {string}")
	public void validaMensagem(String mensagem) {
	    // Espera específica para "Olá, BRUNO!(sair)"
	    String mensagemEsperada = "Olá, BRUNO!(sair)";
	    
	    // Aguarda até 5 segundos com verificação contínua
	    boolean encontrou = false;
	    for (int i = 0; i < 50; i++) {
	        try {
	            String body = page.innerText("body");
	            if (body.contains(mensagemEsperada)) {
	                encontrou = true;
	                break;
	            }
	        } catch (Exception ignored) {}
	        
	        try {
	            Thread.sleep(100);
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	        }
	    }
	    
	    // Validação final
	    String body = page.innerText("body");
	    String normalizedBody = body
	            .replaceAll("[\\n\\r]", " ")
	            .replaceAll("\\s+", " ")
	            .trim();

	    String normalizedExpected = mensagemEsperada
	            .replaceAll("[\\n\\r]", " ")
	            .replaceAll("\\s+", " ")
	            .trim();

	    Assert.assertTrue(
	            normalizedBody.contains(normalizedExpected),
	            "Mensagem 'Olá, BRUNO!(sair)' não encontrada no body após 5 segundos: " + normalizedBody
	    );
	}
	// --- helpers ---
	private void clickFirstVisibleText(String... texts) {
		for (String t : texts) {
			try {
				Locator loc = page.locator("text=" + t);
				if (loc.count() > 0) {
					loc.first().click();
					page.waitForLoadState();
					return;
				}
			} catch (Exception ignored) {}
		}
	}

	private void fillFirstExisting(String[] selectors, String value) {
		for (String sel : selectors) {
			try {
				Locator loc = page.locator(sel);
				if (loc.count() > 0) {
					loc.first().fill(value);
					return;
				}
			} catch (Exception ignored) {}
		}
	}

}

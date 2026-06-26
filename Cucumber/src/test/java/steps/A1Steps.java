package steps;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Entao;
import io.cucumber.java.pt.Dado;
import org.testng.Assert;

public class A1Steps {

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

	@Dado("que o usuário acessa a página de kimonos")
	public void acessarPaginaKimonos() {
		page.navigate("https://g13bjjstore.com.br/kimonos/c");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	@Quando("o usuário busca o kimono tamanho A1 com refreshes")
	public void buscarKimonoA1ComRefresh() {
		long timeoutMs = Long.parseLong(System.getProperty("a1.timeout.ms", "600000")); // 10 minutos por padrão
		long start = System.currentTimeMillis();
		boolean a1Found = false;
		int refreshCount = 0;

		// Loop infinito até encontrar A1 ou timeout
		while (System.currentTimeMillis() - start < timeoutMs) {
			try {
				// Abrir a página do produto Kimono G13BJJ Branco
				abrirPaginaProdutoKimono();
				Thread.sleep(10000);
				// Tentar encontrar e clicar no tamanho A1
				if (selecionarTamanhoA1()) {
					a1Found = true;
					System.out.println("[A1STEPS] ✓ Tamanho A1 encontrado e selecionado após " + refreshCount + " refreshes!");
					break;
				}

				// Se não encontrou, fazer refresh
				refreshCount++;
				long elapsedMs = System.currentTimeMillis() - start;
				System.out.println("[A1STEPS] Tamanho A1 não encontrado. Refresh #" + refreshCount + " (" + (elapsedMs / 1000) + "s decorridos)...");
				page.reload();
				
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}

			} catch (Exception e) {
				System.out.println("[A1STEPS] Erro durante busca: " + e.getMessage());
				refreshCount++;
				try {
					page.reload();
				} catch (Exception ignored) {
				}
				try {
					Thread.sleep(3000);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
			}
		}

		long totalElapsedMs = System.currentTimeMillis() - start;
		Assert.assertTrue(a1Found, 
			"Tamanho A1 não foi encontrado após " + refreshCount + " refreshes em " + 
			(totalElapsedMs / 1000) + "s. Timeout excedido!");
	}

	@Entao("o botão COMPRAR deve estar visível")
	public void botaoComprarDeveEstarVisivel() {
		long timeoutMs = Long.parseLong(System.getProperty("botao.timeout.ms", "120000")); // 2 minutos
		long start = System.currentTimeMillis();
		boolean visible = false;
		int attemptCount = 0;

		System.out.println("[A1STEPS] Começando busca pelo botão COMPRAR (timeout: " + (timeoutMs / 1000) + "s)...");

		while (System.currentTimeMillis() - start < timeoutMs) {
			attemptCount++;
			try {
				// Tentar múltiplas variações de seletor
				Locator comprar1 = page.locator("text=COMPRAR");
				Locator comprar2 = page.locator("text=Comprar");
				Locator comprar3 = page.locator("button:has-text('COMPRAR')");
				Locator comprar4 = page.locator("[class*='comprar'], [class*='buy'], [class*='checkout']");

				if (comprar1.count() > 0 && comprar1.first().isVisible()) {
					visible = true;
					System.out.println("[A1STEPS] ✓ Botão COMPRAR encontrado (variação 1)!");
					break;
				}
				if (comprar2.count() > 0 && comprar2.first().isVisible()) {
					visible = true;
					System.out.println("[A1STEPS] ✓ Botão COMPRAR encontrado (variação 2)!");
					break;
				}
				if (comprar3.count() > 0 && comprar3.first().isVisible()) {
					visible = true;
					System.out.println("[A1STEPS] ✓ Botão COMPRAR encontrado (variação 3)!");
					break;
				}

				// Busca por JavaScript para encontrar qualquer botão com "comprar"
				Object result = page.evaluate("() => { const btns = Array.from(document.querySelectorAll('button, a, [role=\"button\"]')); const comprar = btns.find(b => (b.innerText||b.textContent||'').toLowerCase().includes('comprar')); return comprar ? { text: (comprar.innerText||comprar.textContent||'').trim(), visible: comprar.offsetParent !== null } : null; }");
				
				if (result != null) {
					System.out.println("[A1STEPS] Botão encontrado via JS: " + result);
					@SuppressWarnings("unchecked")
					java.util.Map<String, Object> resultMap = (java.util.Map<String, Object>) result;
					if ((Boolean) resultMap.get("visible")) {
						visible = true;
						System.out.println("[A1STEPS] ✓ Botão COMPRAR está visível!");
						break;
					}
				}

				long elapsedMs = System.currentTimeMillis() - start;
				if (attemptCount % 10 == 0) {
					System.out.println("[A1STEPS] Tentativa #" + attemptCount + " (" + (elapsedMs / 1000) + "s). Botão COMPRAR ainda não está visível...");
				}

			} catch (Exception e) {
				System.out.println("[A1STEPS] Erro ao tentar validar botão: " + e.getMessage());
			}

			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}

		long totalElapsedMs = System.currentTimeMillis() - start;
		if (!visible) {
			// Debug: tentar extrair conteúdo da página para diagnóstico
			try {
				String pageText = page.content().substring(0, Math.min(2000, page.content().length()));
				System.out.println("[A1STEPS] DEBUG - Conteúdo da página (primeiros 2000 chars):\n" + pageText);
			} catch (Exception ignored) {
			}
		}

		Assert.assertTrue(visible, 
			"Botão COMPRAR não está visível após " + (totalElapsedMs / 1000) + "s e " + attemptCount + " tentativas. " +
			"Verifique se o tamanho A1 foi selecionado corretamente.");
	}

	// ============ Métodos auxiliares ============

	private void abrirPaginaProdutoKimono() {
		try {
			Object phObj = page.evaluate("() => { const a = Array.from(document.querySelectorAll('a')).find(x=> (x.innerText||x.textContent||'').toLowerCase().includes('kimono g13') && (x.innerText||x.textContent||'').toLowerCase().includes('branco')); return a ? a.href : null; }");
			String productHref = phObj == null ? null : phObj.toString();
			if (productHref == null) {
				productHref = "https://g13bjjstore.com.br/kimono-g13bjj---branco/p";
			}
			page.navigate(productHref);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		} catch (Exception e) {
			System.out.println("[A1STEPS] Erro ao abrir página do produto: " + e.getMessage());
		}
	}

	private boolean selecionarTamanhoA1() {
		try {
			// Fechar overlays que possam atrapalhar
			try {
				page.evaluate("() => { document.querySelectorAll('#promoFlyer, .flyer-overlay.open, dialog, [role=\"dialog\"], .modal, .overlay').forEach(e=>{e.style.display='none'; e.setAttribute('aria-hidden','true');}); }");
			} catch (Exception ignored) {
			}

			// 1) Usar XPath para encontrar exatamente //span[text()='A1']
			try {
				Locator a1Span = page.locator("//span[text()='A1']");
				if (a1Span.count() > 0) {
					System.out.println("[A1STEPS] Encontrado span com texto 'A1'. Clicando...");
					a1Span.first().click();
					try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
					System.out.println("[A1STEPS] A1 span foi clicado!");
					return true;
				}
			} catch (Exception e) {
				System.out.println("[A1STEPS] XPath //span[text()='A1'] não encontrou nada: " + e.getMessage());
			}

			// 2) Tentar encontrar span contendo A1 (menos restritivo)
			try {
				Locator a1Any = page.locator("span:has-text('A1')");
				if (a1Any.count() > 0) {
					System.out.println("[A1STEPS] Encontrado span com :has-text('A1'). Clicando...");
					a1Any.first().click();
					try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
					System.out.println("[A1STEPS] A1 foi clicado!");
					return true;
				}
			} catch (Exception e) {
				System.out.println("[A1STEPS] Seletor span:has-text('A1') falhou: " + e.getMessage());
			}

			// 3) Tentar selecionar opção dentro de <select>
			try {
				Locator selects = page.locator("select");
				if (selects.count() > 0) {
					for (int i = 0; i < selects.count(); i++) {
						try {
							Locator selectElement = selects.nth(i);
							selectElement.selectOption("A1");
							System.out.println("[A1STEPS] A1 selecionado na opção selectOption()");
							try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
							return true;
						} catch (Exception ignored) {
						}
					}
				}
			} catch (Exception e) {
				System.out.println("[A1STEPS] Erro ao tentar select dropdown: " + e.getMessage());
			}

			// 4) Tentar clicar via JS usando XPath
			try {
				Object result = page.evaluate("() => { const xpath = \"//span[text()='A1']\"; const el = document.evaluate(xpath, document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue; if (el) { el.click(); return true; } return false; }");
				if (result != null && Boolean.parseBoolean(result.toString())) {
					System.out.println("[A1STEPS] A1 clicado via XPath + JS");
					try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
					return true;
				}
			} catch (Exception e) {
				System.out.println("[A1STEPS] XPath via JS falhou: " + e.getMessage());
			}

			System.out.println("[A1STEPS] Nenhum método conseguiu selecionar A1");
			return false;

		} catch (Exception e) {
			System.out.println("[A1STEPS] Erro ao selecionar tamanho A1: " + e.getMessage());
			return false;
		}
	}

}
package steps;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

public class BrowserManager {
    private static Playwright playwright;
    private static Browser browser;
    private static Page page;
    private static int refCount = 0;

    public static synchronized void init(boolean headless) {
        init(headless, 0); // 0 = sem slow motion
    }

    public static synchronized void init(boolean headless, int slowMo) {
        if (playwright == null) {
            playwright = Playwright.create();
            browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                    .setHeadless(headless)
                    .setSlowMo(slowMo) // Adiciona slow motion em milissegundos
            );
            page = browser.newPage();
            refCount = 1;
        } else {
            refCount++;
        }
    }

    public static synchronized Page getPage() {
        return page;
    }

    public static synchronized void close() {
        refCount--;
        if (refCount <= 0) {
            try {
                if (browser != null) {
                    browser.close();
                }
            } catch (Exception ignored) {}
            try {
                if (playwright != null) {
                    playwright.close();
                }
            } catch (Exception ignored) {}
            browser = null;
            playwright = null;
            page = null;
            refCount = 0;
        }
    }

    public static synchronized boolean isInitialized() {
        return playwright != null && browser != null && page != null;
    }
}
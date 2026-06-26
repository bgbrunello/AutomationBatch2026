package steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;

import org.testng.Assert;

public class ExampleSteps {

    private Playwright playwright;
    private String lastBody;
    private String url;

    @Before
    public void before() {
        playwright = Playwright.create();
    }

    @After
    public void after() {
        if (playwright != null) playwright.close();
    }

    @Given("I have the example.com URL")
    public void haveUrl() {
        url = "https://example.com";
    }

    @When("I request the page using Playwright API")
    public void requestPage() {
        APIRequestContext request = null;
        try {
            request = playwright.request().newContext();
            com.microsoft.playwright.APIResponse response = request.get(url);
            // read body while response is valid
            lastBody = response.text();
        } finally {
            if (request != null) {
                try { request.dispose(); } catch (Exception ignored) {}
            }
        }
    }

    @Then("the response body should contain {string}")
    public void bodyShouldContain(String expected) {
        Assert.assertNotNull(lastBody, "No response body was recorded");
        Assert.assertTrue(lastBody.contains(expected), "Response did not contain expected text. Body length: " + lastBody.length());
    }
}

package tests;

import org.testng.annotations.Test;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;

import base.BaseTest;

public class API extends BaseTest {

	public static void getWeatherWithPlaywrightRequest(Playwright playwright) {
		String url = "https://api.openweathermap.org/data/2.5/weather?q=itapevi,brazil&lang=pt_br&appid=98051a5beb882361b5133f22f363ec56";
		APIRequestContext request = null;
		try {
			request = playwright.request().newContext();
			APIResponse response = request.get(url);
			int status = response.status();
			String body = response.text();
			System.out.println("Request URL: " + url);
			System.out.println("Status: " + status);
			System.out.println("Body: \n" + body);
		} catch (Exception e) {
			System.err.println("Failed to perform API request: " + e.getMessage());
		} finally {
			if (request != null) {
				try { request.dispose(); } catch (Exception ignored) {}
			}
		}
	}

	@Test
	public void runGetWeather() {
		// uses Playwright instance from BaseTest
		getWeatherWithPlaywrightRequest(playwright);
	}

}

package tests;

import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.http.Method;
import io.restassured.response.Response;

@Test
public class TestOne {

	public void test_1() {
		Response response = RestAssured.request(Method.GET,
				"https://api.openweathermap.org/data/2.5/weather?q=itapevi,brazil&lang=pt_br&appid=98051a5beb882361b5133f22f363ec56");
		System.out.println("A resposta da requisição é " +response.getBody().asString());
	}
}

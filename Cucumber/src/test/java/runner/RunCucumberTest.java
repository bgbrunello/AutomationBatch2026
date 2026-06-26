package runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

@CucumberOptions(
    features = "src/test/resources/features/Login.feature",
    glue = { "steps" },
    plugin = { "pretty", "html:target/cucumber-report.html" }
)
public class RunCucumberTest extends AbstractTestNGCucumberTests {

}

This project is prepared to run Cucumber (with TestNG) together with Playwright for Java.

Quick setup
1. Install Playwright browsers (required once):

```cmd
cd C:\Users\brunello\Desktop\rotina\Cucumber
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
```

2. Run Cucumber tests (uses TestNG runner class `RunCucumberTest`):

```cmd
mvn -Dtest=runner.RunCucumberTest test
```

Optional flags
- Run headless by setting `-Dheadless=true` (steps can read System.getProperty("headless")).
- Customize kimono wait/interval:
  -Dkimono.timeout.ms=60000 -Dkimono.interval.ms=2000

If you want, I can add a sample feature + step definitions and a TestNG Cucumber runner into this project.

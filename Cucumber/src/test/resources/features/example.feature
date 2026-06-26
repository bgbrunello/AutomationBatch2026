Feature: Example site check

  Scenario: Verify Example Domain contains expected text
    Given I have the example.com URL
    When I request the page using Playwright API
    Then the response body should contain "Example Domain"

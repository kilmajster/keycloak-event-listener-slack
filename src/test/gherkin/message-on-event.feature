Feature: Keycloak sends message to Slack on event

  Scenario: Message is sent to Slack on user login
    Given Keycloak is up and running
    And Slack event listener is enabled
    And fresh test user is generated
    When user navigates to account console page
    And user clicks a sign in button
    Then login form should be displayed
    When user log into account console with valid credentials
    Then user should be logged into account console
    And message with LOGIN event was delivered to Slack
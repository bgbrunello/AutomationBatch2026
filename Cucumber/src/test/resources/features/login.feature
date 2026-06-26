Feature: Login G13Store

  Scenario: Login com sucesso
    Given que o usuário acessa o G13BjjStore
    When informa o usuário "41973078880"
    And informa a senha "Nellosbru1@"
    And clica em Login
    Then deve exibir "Olá Bruno"
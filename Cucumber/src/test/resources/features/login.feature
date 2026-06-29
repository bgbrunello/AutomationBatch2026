Feature: Login G13Store

  Scenario: Login com sucesso
    Given que o usuário acessa o G13BjjStore
    When informa o usuário "usuário"
    And informa a senha "senha"
    And clica em Login
    Then deve exibir "Olá Bruno"

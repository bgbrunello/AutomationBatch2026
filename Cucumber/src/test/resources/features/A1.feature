Feature: Verificar Kimono G13BJJ Branco com Retry

Scenario: Verify availability of COMPRAR button for Kimono G13BJJ Branco with refresh retry

  Given que o usuário acessa a página de kimonos
  When o usuário busca o kimono tamanho A1 com refreshes
  Then o botão COMPRAR deve estar visível
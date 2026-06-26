Feature: Verificar Carrinho de compras

Scenario: Adicionar produto ao carrinho
  Given que o usuário acessa a página do produto "Kimono G13BJJ Branco"
  When seleciona o tamanho "A2"
  And clica no botão "Comprar"
  Then o produto deve ser adicionado ao carrinho
  And o carrinho deve conter 1 item
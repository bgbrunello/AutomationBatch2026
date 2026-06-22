@kimono @disponibilidade
Funcionalidade: Verificar disponibilidade do botão COMPRAR para Kimono G13 Branco

  Cenário: COMPRAR fica disponível para o tamanho A1 do Kimono G13 (Branco)
    Dado que eu acesso a página de listagem de kimonos
    E eu encontro o produto cujo título contém "kimono g13" e "branco"
    Quando eu abro a página do produto encontrado
    E eu removo popups e overlays que bloqueiam interações
    E eu seleciono o tamanho "A1" se ele estiver presente
    E eu aguardo até que o botão "COMPRAR" esteja visível, recarregando a página periodicamente
    Então o botão "COMPRAR" deve estar disponível

# Playwright Sauce Demo Automation

Projeto de automação de testes end-to-end em TypeScript com Playwright, focado no site público Sauce Demo.

Este projeto foi estruturado com base no aprendizado e na referência do projeto antigo da pasta D:\workspace\outsera-qa-test\automation-frontend, mas profissionalizado para seguir uma arquitetura mais limpa, reutilizável e adequada para execução em ambientes reais.

## Objetivo

Validar cenários essenciais de e-commerce em uma aplicação pública e estável, cobrindo:

- Login com sucesso e falha
- Fluxo de compra completo
- Remoção de itens do carrinho
- Ordenação de produtos
- Estrutura de Page Object Model para manutenção e escalabilidade

## Stack

- Node.js
- TypeScript
- Playwright
- HTML report
- JSON report

## Requisitos

- Node.js 18+
- npm
- Navegador Chromium instalado via Playwright

## Instalação

Na raiz do projeto, execute:

```bash
npm install
npx playwright install chromium
```

## Execução dos testes

Rodar todos os testes:

```bash
npm test
```

Rodar em modo headed (com visualização do navegador):

```bash
npm run test:headed
```

Abrir interface do Playwright Test:

```bash
npm run test:ui
```

Modo debug:

```bash
npm run test:debug
```

Abrir relatório HTML gerado:

```bash
npm run test:report
```

Validação de TypeScript:

```bash
npm run lint
```

Executar lint + testes:

```bash
npm run check
```

## Estrutura do projeto

```text
.
├── playwright.config.ts
├── package.json
├── tsconfig.json
├── README.md
├── .gitignore
├── src/
│   ├── data/
│   │   └── users.ts
│   └── pages/
│       ├── BasePage.ts
│       ├── LoginPage.ts
│       ├── ProductsPage.ts
│       ├── CartPage.ts
│       └── CheckoutPage.ts
├── tests/
│   └── sauce-demo.spec.ts
├── playwright-report/
├── test-results/
└── node_modules/
```

## Arquitetura adotada

O projeto segue a abordagem de Page Object Model (POM):

- Cada página da aplicação possui sua classe específica
- Os elementos e ações ficam encapsulados na camada de página
- Os testes ficam focados na navegação e comportamento do usuário
- A manutenção do projeto fica mais simples conforme cresce a suíte

## Testes implementados

Atualmente a suíte cobre os seguintes cenários:

1. Login com usuário válido
2. Login com credenciais inválidas
3. Fluxo completo de compra
4. Remoção de item do carrinho
5. Ordenação por preço mais alto

## Configuração do Playwright

A configuração global está em [playwright.config.ts](playwright.config.ts) e define:

- diretório de testes
- timeout padrão
- report HTML e JSON
- browser Chromium
- screenshots em falha
- vídeos em falha
- trace em falha

## Observações

- O projeto usa a aplicação pública e estável Sauce Demo como alvo principal.
- O objetivo é manter uma base de automação robusta e profissional, sem dependência de sites bloqueados ou instáveis.
- O código foi organizado para facilitar expansões futuras, como novos fluxos, dados externos e integração com CI/CD.

## Autor

Projeto de automação em Playwright para fins de estudo e evolução profissional em QA Automation.

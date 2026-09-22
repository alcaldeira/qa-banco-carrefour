---
name: record-e2e-scenario
description: Use when asked to record or create a new e2e scenario for this project by having the user narrate the steps in chat, one at a time (e.g. "grava um cenário novo", "cria um teste assistido", "preencher usuário com X", "validar que Y apareceu"). Drives a real browser live via the project's Playwright MCP server when connected (see .mcp.json), inspecting the real DOM instead of guessing selectors; falls back to narration-only mapping when the MCP tools aren't connected. Maps each step live to this project's Page Object Model (src/pages, tests/*.spec.ts), reusing existing page objects/methods instead of duplicating them. Playwright's own visible-browser codegen is available as a further fallback when the person has a display and prefers clicking through the flow themselves.
---

Não invente seletores sem avisar que é suposição. Reaproveite o que já existe antes de criar algo novo.

Este projeto tem um servidor MCP `playwright` configurado em `.mcp.json` (raiz do repo, `npx @playwright/mcp@latest`). Antes de começar, verifique se as ferramentas `browser_navigate`/`browser_click`/`browser_type`/`browser_snapshot` (ou equivalentes do Playwright MCP) estão disponíveis nesta sessão:
- **Disponíveis** → use o Modo A (navegador ao vivo).
- **Não disponíveis** (MCP ainda não conectado/sessão não recarregada) → avise o usuário que precisa recarregar a janela/extensão para conectar, e ofereça o Modo B (narração sem navegador) como alternativa imediata.

## Modo A — navegador ao vivo via Playwright MCP (preferido quando disponível)

O usuário narra as ações em linguagem natural, uma por vez, e você **executa cada uma de verdade** no navegador através das ferramentas do Playwright MCP, em vez de só imaginar o resultado:

```
preencher usuário com standard_user
preencher senha com secret_sauce
clicar em login
validar que a página de produtos carregou
```

Fluxo por passo:
1. Abra/navegue para a URL correta com `browser_navigate` (só no primeiro passo, ou quando o cenário mudar de página).
2. Tire um `browser_snapshot` (ou equivalente) para ver a árvore de acessibilidade/DOM real e localizar o elemento — não suponha o `data-test`, leia o atributo real do elemento na página.
3. Execute a ação (`browser_click`, `browser_type`, etc.) no elemento identificado.
4. Confirme visualmente/pelo snapshot que o efeito esperado aconteceu (ex.: mensagem de erro apareceu, navegou para outra página) antes de seguir pro próximo passo narrado.
5. Anote o seletor real e a ação numa lista ordenada (esse é o material que vira código no Passo 3 mais abaixo) até o usuário sinalizar que terminou o cenário.

Como o seletor vem do DOM real, não é preciso avisar "é suposição" neste modo — mas ainda assim prefira reaproveitar locators/métodos já existentes no Page Object da página (ver Passo 1 abaixo) em vez de recriar um novo para o mesmo elemento.

## Modo B — narração no chat, sem navegador (fallback)

Use quando o Playwright MCP não estiver conectado. O usuário descreve as ações da mesma forma, mas sem execução real:

### Passo 1 — ler o projeto antes de mapear qualquer coisa

Leia `src/pages/BasePage.ts` e todos os `src/pages/*.ts`, o(s) `tests/*.spec.ts` existente(s) e `src/data/*.ts` (massa de dados fixa, ex. `validUser`, `errorMessages`). Isso acontece uma vez por sessão de gravação, antes do primeiro passo narrado — vale para os dois modos.

Note:
- Helpers de `BasePage`: `waitForVisible`, `click`, `fillField`, `getText`, `clearAndFill` — todo método novo em Page Object deve usar esses, não `page.click()`/`page.fill()` cru.
- Convenção de locator: `page.locator('[data-test="..."]')` como padrão; classes semânticas (`.title`, `.inventory_item`) só quando não existe `data-test`.
- Métodos já existentes (`LoginPage.login`, `ProductsPage.addBackpack`, `CartPage.removeItemByProductName`, etc.) — se o passo narrado corresponde ao que um método existente já faz, **reaproveite o método**, não reemita os passos crus que ele encapsula.

### Passo 2 — mapear cada passo narrado, um de cada vez

Para cada linha que o usuário narrar:
1. Identifique a ação (preencher, clicar, selecionar, validar, navegar...) e o alvo.
2. Verifique se já existe locator/método correspondente no Page Object da página atual. Se existir, reaproveite — não crie duplicata.
3. Se não existir, proponha o novo `Locator` (inferido de `[data-test="..."]` a partir do nome do campo/ação) e o novo método, **avisando explicitamente que é uma suposição de seletor** já que não há inspeção ao vivo do DOM nesse modo. Se o nome do campo for ambíguo, pergunte antes de supor.
4. Vá acumulando os passos mapeados numa lista ordenada até o usuário sinalizar que terminou o cenário (ex. "gerar código", "é isso").

### Passo 3 — gerar o código, no formato do projeto

Quando o usuário fechar o cenário:
- Elemento novo numa página que já tem Page Object → adicionar `Locator` readonly + método nessa classe existente.
- Página sem Page Object ainda → nova classe `extends BasePage`, mesmo formato de `LoginPage.ts` (locators readonly no construtor, um método por ação do usuário, métodos `expect*` para validações).
- O teste em si entra em `tests/*.spec.ts` como um novo `test(...)` dentro do `test.describe(...)` existente (ver `sauce-demo.spec.ts`) — nome do teste em inglês, frase descritiva, no mesmo estilo dos testes vizinhos (`'Login with valid credentials'`, `'Complete purchase flow'`, etc.), composto só por chamadas a métodos de Page Object.
- Massa de dados fixa (usuário, senha, mensagens de erro) → reaproveitar/estender `src/data/*.ts`, não hardcodear de novo no spec. Dados variáveis específicos do cenário (nomes, CEP, valores) podem ficar direto no spec.

### Passo 4 — verificar

```bash
npm run check   # tsc --noEmit + npm test
```

Não considere o cenário pronto até isso passar. Se um seletor suposto no Passo 2 estiver errado, o teste falhando aqui é o sinal — ajuste e rode de novo.

## Modo C — gravação assistida com Playwright Codegen (fallback quando o MCP não é opção)

Se a pessoa tiver display disponível e preferir clicar o fluxo de verdade em vez de narrar em texto:

```bash
npx playwright codegen --test-id-attribute=data-test <url>
```

`--test-id-attribute=data-test` importa: os Page Objects deste projeto localizam elementos por `[data-test="..."]` (ver `LoginPage.ts`), não pelo padrão do Playwright `data-testid` — sem a flag, o codegen emite seletores fora da convenção.

Isso abre um navegador visível de verdade mais o Playwright Inspector, mostrando o código gerado ao vivo enquanto a pessoa clica/digita/seleciona. Só roda na máquina da pessoa, com display — não roda num shell headless/sandboxed.

Quando terminar, a pessoa copia o código gerado do Inspector (ou salva) e entrega para tradução — segue então os Passos 1, 3 e 4 do modo principal acima (o Passo 2 já vem pronto no código copiado, só precisa reescrever/traduzir para o formato do projeto em vez de mapear do zero).

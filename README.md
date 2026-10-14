# Assistente Financeiro (Telegram e Google Sheets)

Bot pessoal em Java e Spring Boot para registrar gastos e consultar o resumo do orçamento numa planilha do Google Sheets, por comandos e menus no Telegram.

O projeto começou com a API de WhatsApp (Cloud API) e hoje o fluxo principal é pelo Telegram.

## O que faz

- Registra gastos escolhendo categoria, item e valor numa conversa guiada (menu do bot).
- Soma o valor informado ao valor já existente do mês atual, na linha do item correspondente da planilha.
- Atualiza e consulta a aba de controle com comandos (`/saldo`, `/fatura`, `/posso`).
- Responde apenas aos chats autorizados.

## Tecnologias

- Java e Spring Boot
- API REST
- Google Sheets API (conta de serviço)
- Telegram Bot
- Maven

## Como funciona

1. A pessoa envia uma mensagem ao bot.
2. A conversa segue uma máquina de estados: categoria, item, valor.
3. O serviço grava o valor na célula correta da planilha (mês atual e linha do item).
4. O bot responde com a confirmação ou com o resumo.

O catálogo de categorias e itens, com a linha de cada item na planilha, fica na classe `CatalogoGastos`.

## Endpoints de teste

| Método | Rota | O que faz |
|---|---|---|
| GET | `/categorias` | Lista o catálogo de categorias e itens |
| POST | `/gastos` | Registra um gasto (soma ao valor do mês atual) |

Exemplo:

```bash
curl http://localhost:8080/categorias

curl -X POST http://localhost:8080/gastos \
     -H "Content-Type: application/json" \
     -d '{"categoria":"Despesas Essenciais","item":"Gasolina","valor":50}'
```

## Comandos do Telegram

| Comando | O que faz |
|---|---|
| `/saldo <valor>` | Grava o saldo na aba de controle e responde o resumo |
| `/fatura <valor>` | Grava a fatura na aba de controle e responde o resumo |
| `/posso` | Responde o resumo: dinheiro livre, disponível no lazer e quanto posso gastar agora |

- Valores aceitos: `1156,26`, `1.156,26` e `1156.26`. Valores negativos, texto e vazio são rejeitados.
- Os endereços das células ficam em `application.properties` (propriedades `controle.*`).
- Os comandos só respondem aos chat IDs listados em `TELEGRAM_ALLOWED_CHAT_IDS` (separados por vírgula). Sem essa variável, os comandos são ignorados para todos.

## Configuração

Edite `src/main/resources/application.properties` e defina:

```properties
google.sheets.credentials-path=/caminho/para/google-service-account.json
google.sheets.spreadsheet-id=ID_DA_PLANILHA
```

- `credentials-path`: caminho do arquivo JSON da conta de serviço do Google.
- `spreadsheet-id`: trecho da URL da planilha entre `/d/` e `/edit`.

Variável de ambiente para o Telegram:

```bash
export TELEGRAM_ALLOWED_CHAT_IDS=ID_DO_CHAT
```

Antes do primeiro uso, confirme se os números de linha em `CatalogoGastos.java` correspondem às linhas da aba de acompanhamento da sua planilha.

## Como rodar

```bash
./mvnw clean package -DskipTests
java -jar target/assistente-financeiro.jar
```

Se o projeto não tiver o Maven Wrapper (`mvnw`), instale o Maven e use `mvn clean package -DskipTests`.

## Status

Implementado:

- Integração com o Google Sheets (leitura e escrita de gastos)
- Catálogo de categorias e itens (menu do bot)
- Endpoints de teste manual
- Lógica de conversa por máquina de estados
- Comandos de Telegram com lista de chats autorizados
- Webhook da API de WhatsApp (recebe e responde mensagens)

## Próximos passos

- Executar a aplicação em segundo plano com `systemd`, para sobreviver a reinícios do servidor
- Adicionar testes automatizados

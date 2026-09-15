# Assistente Financeiro (WhatsApp + Google Sheets)

Projeto Spring Boot que vai virar um bot no WhatsApp pra lançar gastos
direto na planilha de orçamento pessoal.

## Status atual

✅ Integração com Google Sheets (ler e escrever gastos)
✅ Catálogo de categorias/itens (menu do bot)
✅ Endpoints de teste manual (sem WhatsApp ainda)
✅ Webhook do WhatsApp (recebe mensagem, responde de volta)
✅ Lógica de conversa (máquina de estado: categoria → item → valor)
⏳ Token permanente da API (via System User) — hoje usa o token temporário de 24h
⏳ Nginx + registro do webhook no painel da Meta (últimos passos manuais)

## ⚠️ Antes de rodar: 2 coisas pra ajustar

### 1. Confirmar os números de linha da planilha

O arquivo `CatalogoGastos.java` tem os números de linha de cada item
**estimados** a partir da ordem vista nas capturas de tela. Abra a aba
"Acompanhamento" na sua planilha e confirme se os números batem
(o número da linha aparece à esquerda, no Google Sheets). Se algum item
estiver em outra linha, corrija no arquivo.

### 2. Preencher `application.properties`

Edite `src/main/resources/application.properties`:

```properties
google.sheets.credentials-path=/home/ubuntu/credentials/google-service-account.json
google.sheets.spreadsheet-id=SEU_ID_AQUI
```

- `credentials-path`: onde você vai colocar o arquivo JSON da Service
  Account dentro da VM (veja o passo abaixo)
- `spreadsheet-id`: o trecho da URL da sua planilha entre `/d/` e `/edit`.
  Exemplo: se a URL é
  `https://docs.google.com/spreadsheets/d/1AbCdEfGhIjKlmNoPqRs/edit`,
  o ID é `1AbCdEfGhIjKlmNoPqRs`.

## Como levar o projeto pra VM

Na sua VM Oracle, dentro da pasta do usuário `ubuntu`:

```bash
mkdir -p ~/credentials
# copie o JSON da Service Account pra ~/credentials/google-service-account.json
# (pode usar scp do seu computador, ou colar o conteúdo com nano)
```

Pra copiar o projeto inteiro (rode isso no seu computador, não na VM):

```bash
scp -i "caminho\para\sua-chave.key" -r assistente-financeiro ubuntu@129.148.63.137:~/
```

## Como rodar (dentro da VM)

```bash
cd ~/assistente-financeiro
./mvnw clean package -DskipTests
java -jar target/assistente-financeiro.jar
```

Se não tiver o `mvnw` (Maven Wrapper) no projeto, instale o Maven antes:
```bash
sudo apt install maven -y
mvn clean package -DskipTests
java -jar target/assistente-financeiro.jar
```

## Como testar (antes do WhatsApp existir)

Com a aplicação rodando, abra outro terminal SSH (ou use o mesmo depois
de rodar em background com `nohup ... &`) e teste:

```bash
# Ver o catálogo de categorias/itens
curl http://localhost:8080/categorias

# Registrar um gasto de teste (SOMA no valor já existente do mês atual)
curl -X POST http://localhost:8080/gastos \
     -H "Content-Type: application/json" \
     -d '{"categoria":"Despesas Essenciais","item":"Gasolina","valor":50}'
```

Depois, confira na própria planilha se o valor mudou na célula certa
(mês atual, linha do item testado).

## Preencher o restante do application.properties

```properties
whatsapp.access-token=SEU_TOKEN_GERADO_NO_PAINEL
whatsapp.phone-number-id=SEU_PHONE_NUMBER_ID
whatsapp.verify-token=escolha-uma-senha-aqui
```

- `access-token`: o token gerado na tela "API Setup" (Etapa 1). É o
  temporário de 24h por enquanto — funciona pra testar, mas expira.
- `phone-number-id`: também está na tela de API Setup.
- `verify-token`: **você inventa** qualquer palavra/senha. Só precisa
  colar essa mesma palavra no painel da Meta na hora de registrar o
  webhook (próximo passo).

## Configurar o Nginx (proxy HTTPS -> Spring Boot)

O Spring Boot roda na porta 8080 (HTTP puro). A Meta exige HTTPS na
porta 443. O Nginx faz essa ponte usando o certificado que já geramos.

Na VM:
```bash
sudo apt install nginx -y
sudo cp nginx-assistente-financeiro.conf /etc/nginx/sites-available/assistente-financeiro
sudo ln -s /etc/nginx/sites-available/assistente-financeiro /etc/nginx/sites-enabled/
sudo nginx -t          # testa se a configuração está válida
sudo systemctl restart nginx
```

## Registrar o webhook no painel da Meta

1. No app da Meta, vá em **WhatsApp > Configuration** (ou "Configuração")
2. No campo **Callback URL**, coloque: `https://brenobot.duckdns.org/webhook`
3. No campo **Verify Token**, coloque a mesma senha que você escolheu em
   `whatsapp.verify-token` no application.properties
4. Clique em **Verify and Save** — a Meta vai chamar o GET do webhook
   pra confirmar. Se dor certo, o Spring Boot precisa estar rodando
   nesse momento pra responder.
5. Depois de salvar, ainda na mesma tela, procure a seção de
   **"Webhook fields"** e clique em **"Manage"** — marque o campo
   **"messages"** pra assinar (subscribe). Sem isso, a Meta nem manda
   as mensagens recebidas pro seu webhook.

## Testar o fluxo completo

Com o Spring Boot rodando (e o Nginx configurado), manda qualquer
mensagem pro número de teste do WhatsApp, pelo seu celular cadastrado
como destinatário. O bot deve responder com o menu de categorias, e o
fluxo completo (categoria → item → valor) deve terminar gravando na
planilha.

## Próximos passos

1. Confirmar linhas da planilha (topo deste arquivo)
2. Rodar e testar os endpoints manuais (`/categorias`, `/gastos`)
3. Preencher as credenciais do WhatsApp acima
4. Configurar Nginx
5. Registrar o webhook no painel da Meta
6. Testar o fluxo completo pelo celular
7. Gerar token permanente via System User (pra não expirar a cada 24h)
8. Deixar a aplicação rodando em segundo plano (systemd), pra sobreviver
   a reinícios da VM

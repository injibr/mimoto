# Plano de Migração INJIBR — Mimoto v0.17.0 → v0.19.2

## Contexto

A branch atual `update/v0.19.2` foi criada a partir da upstream v0.19.2. Este documento
descreve todas as customizações INJIBR que existiam na branch `entrega` (baseada na
upstream v0.17.0) e como cada uma deve ser aplicada na v0.19.2.

Fonte do git diff: `entrega-vs-upstream-0.17.0.patch`
Gerado com: `git diff v0.17.0..entrega`

---

## Convenção de Customização

Toda linha de código alterada ou desativada por motivo de compatibilidade com govbr
ou por customização INJIBR **não deve ser deletada** — deve ser **comentada**,
precedida de um comentário explicativo com a tag `INJIBR-CUSTOM`.

**Padrão obrigatório:**
```java
// INJIBR-CUSTOM: <motivo da mudança>
// linha original comentada
nova linha ou ausência de linha
```

**Objetivo:** facilitar busca por `INJIBR-CUSTOM` para identificar todos os pontos
customizados ao fazer um novo upgrade de versão upstream.

---

## Mudanças de Arquitetura entre v0.17.0 e v0.19.2 (upstream)

Verificar antes de aplicar cada customização se a v0.19.2 já incorporou alguma
das mudanças abaixo nativamente:

| Aspecto | v0.17.0 | v0.19.2 |
|---|---|---|
| `getIssuerWellknown()` | recebe só `credentialIssuerHost` | verificar assinatura atual |
| `getTokenResponse()` | método em `CredentialServiceImpl` | verificar se ainda existe |
| `verifyCredential()` | lança `VCVerificationException` | verificar comportamento atual |
| `DataShareServiceImpl` | chama `pushCredentialIntoDataShare` | verificar se ainda existe |
| Cache `issuerWellknown` | chave por URL | verificar configuração atual |

---

## Customizações INJIBR a Aplicar

---

### 1. Pacote `govbr` — novos arquivos (portar direto)

**Módulo:** `src/main/java/io/mosip/mimoto/govbr/`

Todos os arquivos abaixo são novos e não existem na upstream v0.19.2.
Portar diretamente sem conflito:

| Arquivo | Descrição |
|---|---|
| `ApiResponse.java` | Wrapper genérico de resposta da API govbr |
| `GovBRController.java` | Endpoint `GET /user/profile` — busca perfil do usuário govbr |
| `GovBRExceptionHandler.java` | `@ControllerAdvice` para exceções govbr |
| `GovBRService.java` | Interface com `getUserProfile()` e `getToken()` |
| `GovBRServiceImpl.java` | Implementação — chama SSO govbr para token, userinfo e foto |
| `GovBRUserProfileResponse.java` | DTO de resposta do perfil govbr |
| `exceptions/GovBRException.java` | Exceção base com `HttpStatus` |
| `exceptions/InvalidCodeException.java` | Código de autorização inválido/expirado (400) |
| `exceptions/TokenRequestException.java` | Falha na requisição de token |
| `exceptions/UnauthorizedClientException.java` | Credenciais inválidas (401) |
| `exceptions/UserInfoRequestException.java` | Falha ao buscar userinfo |

**Properties necessárias** (adicionar em `application-local.properties` e `certify-default.properties`):
```properties
# INJIBR-CUSTOM: SSO govbr endpoints para autenticação
sso.token-url=to_set
sso.userinfo-url=to_set
sso.redirect-uri=to_set
sso.auth-header=to_set
```

**Ponto de atenção:** `GovBRServiceImpl` usa `RestTemplate` diretamente (não `WebClient`).
Verificar se a v0.19.2 tem alguma configuração de `RestTemplate` que precise ser respeitada.

---

### 2. `CredentialsController.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/controller/CredentialsController.java`

**O que muda:** Substituir a chamada `credentialService.getTokenResponse()` por
`govBRService.getToken()` no endpoint de download de credencial.

**Evidência no patch:**
```diff
-TokenResponseDTO response = credentialService.getTokenResponse(params, issuerId);
+//Changed the token API call with govbr, to integrate with govbr
+TokenResponseDTO response = govBRService.getToken(params.get("code"), params.get("code_verifier"));
```

**Como aplicar na v0.19.2:**
```java
// INJIBR-CUSTOM: govbr uses its own token endpoint via GovBRService instead of esignet
// TokenResponseDTO response = credentialService.getTokenResponse(params, issuerId);
TokenResponseDTO response = govBRService.getToken(params.get("code"), params.get("code_verifier"));
```

Também adicionar o `@Autowired GovBRService govBRService`.

**Verificar na v0.19.2:** se `credentialService.getTokenResponse()` ainda existe com
a mesma assinatura — pode ter mudado entre v0.17.0 e v0.19.2.

---

### 3. `IdpController.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/controller/IdpController.java`

**O que muda:** Substituir `credentialService.getTokenResponse()` por
`govBRService.getToken()` no endpoint `get-token`.

**Evidência no patch:**
```diff
-TokenResponseDTO response = credentialService.getTokenResponse(params, issuer);
+TokenResponseDTO response = govBRService.getToken(params.get("code"), params.get("code_verifier"));
```

**Como aplicar na v0.19.2:**
```java
// INJIBR-CUSTOM: govbr uses its own token endpoint via GovBRService instead of esignet
// TokenResponseDTO response = credentialService.getTokenResponse(params, issuer);
TokenResponseDTO response = govBRService.getToken(params.get("code"), params.get("code_verifier"));
```

Também adicionar o `@Autowired GovBRService govBRService`.

---

### 4. `CredentialServiceImpl.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/service/impl/CredentialServiceImpl.java`

**O que muda (2 pontos):**

#### 4a. Injetar `doctype` no `VCCredentialRequest`

**Evidência no patch:**
```diff
+//Added CredentialType to the request, to integrate with govbr
+vcCredentialRequest.setDoctype(credentialType);
```

**Como aplicar na v0.19.2:**
```java
// INJIBR-CUSTOM: certify uses doctype to dispatch to the correct DataProvider
vcCredentialRequest.setDoctype(credentialType);
```

#### 4b. Injetar `issuerId` no `VCCredentialRequest`

**Evidência no patch:**
```diff
+.issuerId(issuerDTO.getIssuer_id())
```

**Como aplicar na v0.19.2:** Adicionar `.issuerId(issuerDTO.getIssuer_id())` no builder
do `VCCredentialRequest`:
```java
// INJIBR-CUSTOM: certify uses issuerId for multi-issuer well-known lookup
.issuerId(issuerDTO.getIssuer_id())
```

#### 4c. Desabilitar lançamento de exceção na verificação de VC

**Evidência no patch:**
```diff
-throw new VCVerificationException(verificationResult.getVerificationErrorCode().toLowerCase(), verificationResult.getVerificationMessage());
+//            throw new VCVerificationException(verificationResult.getVerificationErrorCode().toLowerCase(), verificationResult.getVerificationMessage());
```

**Como aplicar na v0.19.2:**
```java
// INJIBR-CUSTOM: VC verification failure does not block issuance (govbr VC format compatibility)
// throw new VCVerificationException(verificationResult.getVerificationErrorCode().toLowerCase(), verificationResult.getVerificationMessage());
```

**Ponto de atenção:** Esta é uma mudança de comportamento significativa — VCs com
falha de verificação passam a ser aceitas silenciosamente. Verificar se a v0.19.2
mudou a lógica de verificação antes de aplicar.

---

### 5. `DataShareServiceImpl.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/service/impl/DataShareServiceImpl.java`

**O que muda:** Desabilitar o push real para o DataShare, retornando string fixa `"sas"`.

**Evidência no patch:**
```diff
-DataShareResponseWrapperDTO dataShareResponseWrapperDTO = pushCredentialIntoDataShare(requestEntity, credentialValidity);
-log.info("Data pushed into DataShare -> " + dataShareResponseWrapperDTO);
-return  dataShareResponseWrapperDTO.getDataShare().getUrl();
+return "sas";
+//        DataShareResponseWrapperDTO dataShareResponseWrapperDTO = pushCredentialIntoDataShare(requestEntity, credentialValidity);
+//        log.info("Data pushed into DataShare -> " + dataShareResponseWrapperDTO);
+//        return  dataShareResponseWrapperDTO.getDataShare().getUrl();
```

**Como aplicar na v0.19.2:**
```java
// INJIBR-CUSTOM: DataShare not used in govbr flow, returning placeholder
return "sas";
// DataShareResponseWrapperDTO dataShareResponseWrapperDTO = pushCredentialIntoDataShare(requestEntity, credentialValidity);
// log.info("Data pushed into DataShare -> " + dataShareResponseWrapperDTO);
// return dataShareResponseWrapperDTO.getDataShare().getUrl();
```

**Ponto de atenção:** Verificar se a v0.19.2 mudou a assinatura ou o comportamento
do método `pushToDataShare` — pode ter sido refatorado.

---

### 6. `IssuersServiceImpl.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/service/impl/IssuersServiceImpl.java`

**O que muda (2 pontos):**

#### 6a. Passar `issuerId` para `getIssuerWellknown()`

**Evidência no patch:**
```diff
-CredentialIssuerWellKnownResponse credentialIssuerWellKnownResponse = issuersConfigUtil.getIssuerWellknown(getIssuerDetails(issuerId).getCredential_issuer_host());
+CredentialIssuerWellKnownResponse credentialIssuerWellKnownResponse = issuersConfigUtil.getIssuerWellknown(getIssuerDetails(issuerId).getCredential_issuer_host(), issuerId);
```

**Como aplicar na v0.19.2:** Adicionar `issuerId` como segundo parâmetro.

#### 6b. Não buscar `AuthorizationServerWellKnownResponse`

**Evidência no patch:**
```diff
-AuthorizationServerWellKnownResponse authorizationServerWellKnownResponse = issuersConfigUtil.getAuthServerWellknown(credentialIssuerWellKnownResponse.getAuthorizationServers().get(0));
+//Remove the code to fetch AuthorizationServerWellKnownResponse as it is not used in the current implementation
+//to integrate with govbr
+//        AuthorizationServerWellKnownResponse authorizationServerWellKnownResponse = issuersConfigUtil.getAuthServerWellknown(...);
```
E retornar `new AuthorizationServerWellKnownResponse()` vazio em vez do objeto real.

**Como aplicar na v0.19.2:**
```java
// INJIBR-CUSTOM: govbr does not expose AuthorizationServer well-known; skip the call
// AuthorizationServerWellKnownResponse authorizationServerWellKnownResponse =
//     issuersConfigUtil.getAuthServerWellknown(credentialIssuerWellKnownResponse.getAuthorizationServers().get(0));
```

**Verificar na v0.19.2:** se `CredentialIssuerConfiguration` ainda aceita
`AuthorizationServerWellKnownResponse` como parâmetro — pode ter mudado.

---

### 7. `IssuerConfigUtil.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/util/IssuerConfigUtil.java`

**O que muda:** Adicionar `issuerId` como parâmetro de `getIssuerWellknown()` e
usar como chave de cache + adicionar `?issuer_id=` na URL do well-known.

**Evidência no patch:**
```diff
-@Cacheable(value = "issuerWellknown", key = "#p0")
-public CredentialIssuerWellKnownResponse getIssuerWellknown(String credentialIssuerHost) {
-    String wellknownEndpoint = credentialIssuerHost + "/.well-known/openid-credential-issuer";
+//Changed the cache name to issuerId to avoid conflict with other cache names, to integrate govbr
+@Cacheable(value = "issuerId", key = "#p1")
+public CredentialIssuerWellKnownResponse getIssuerWellknown(String credentialIssuerHost, String issuerId) {
+    String wellknownEndpoint = credentialIssuerHost + "/.well-known/openid-credential-issuer?issuer_id=" + issuerId;
```

**Como aplicar na v0.19.2:**
```java
// INJIBR-CUSTOM: cache by issuerId (not URL) and append issuer_id param to well-known endpoint
@Cacheable(value = "issuerId", key = "#p1")
public CredentialIssuerWellKnownResponse getIssuerWellknown(String credentialIssuerHost, String issuerId) {
    String wellknownEndpoint = credentialIssuerHost + "/.well-known/openid-credential-issuer?issuer_id=" + issuerId;
```

**Verificar na v0.19.2:** se a assinatura de `getIssuerWellknown` mudou — pode ter
recebido parâmetros adicionais entre v0.17.0 e v0.19.2.

---

### 8. `IdpServiceImpl.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/service/impl/IdpServiceImpl.java`

**O que muda:** Adicionar `@Slf4j` e log do nome do keystore.

**Evidência no patch:**
```diff
+@Slf4j
 @Service
 public class IdpServiceImpl implements IdpService {
+    log.info("KeyStore filename: {}", fileName);
```

**Como aplicar na v0.19.2:** Trivial — adicionar `@Slf4j` e a linha de log.
Sem conflito esperado.

---

### 9. `CredentialShareController.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/controller/CredentialShareController.java`

**O que muda:** Adicionar log no início do método `download()`.

**Evidência no patch:**
```diff
+log.info("Calling download credential for request id in credential share: {}", requestDTO.getRequestId());
```

**Como aplicar na v0.19.2:** Trivial — adicionar a linha de log. Sem conflito esperado.

---

### 10. `VCCredentialRequest.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/dto/mimoto/VCCredentialRequest.java`

**O que muda:** Adicionar campos `doctype` e `issuerId`.

**Evidência no patch:**
```diff
+//Added additional fields for issuerId and doctype, to integrate with govbr
+private String doctype;
+private String issuerId;
```

**Como aplicar na v0.19.2:**
```java
// INJIBR-CUSTOM: certify uses doctype and issuerId for credential dispatch and multi-issuer lookup
private String doctype;
private String issuerId;
```

**Verificar na v0.19.2:** se `VCCredentialRequest` já tem campos similares adicionados
na upstream entre v0.17.0 e v0.19.2.

---

### 11. `RestApiClient.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/util/RestApiClient.java`

**O que muda:** Método `getApi()` com `queryParams` foi adicionado mas **já comentado**
no patch — era código experimental que não chegou a ser usado.

**Evidência no patch:**
```diff
+//    public <T> T getApi(String url, Class<?> responseType, Map<String,String> queryParams) {
+//        ...
+//    }
```

**Como aplicar na v0.19.2:** **Não portar** — o método está comentado e não é usado.

---

### 12. `Utilities.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/util/Utilities.java`

**O que muda:** Corrigir o path de templates para desenvolvimento local.

**Evidência no patch:**
```diff
-Path basePath = Paths.get("templates").toAbsolutePath().normalize();
+Path basePath = Paths.get("src/main/resources/templates").toAbsolutePath().normalize();
...
-return Files.readString(credentialTemplateResource.getFile().toPath());
+return Files.readString(resolvedPath);
```

**Como aplicar na v0.19.2:**
```java
// INJIBR-CUSTOM: fix template path for local development profile
Path basePath = Paths.get("src/main/resources/templates").toAbsolutePath().normalize();
...
// INJIBR-CUSTOM: read directly from resolved path instead of ClassPathResource
return Files.readString(resolvedPath);
```

**Verificar na v0.19.2:** se `getCredentialSupportedTemplateString()` ainda existe
com a mesma lógica — pode ter sido refatorado.

---

### 13. Templates HTML (novos arquivos — portar direto)

**Diretório:** `src/main/resources/templates/`

Arquivos novos, sem conflito com a upstream:

| Arquivo | Credencial |
|---|---|
| `INCRA-CCIRCredential-template.html` | Certificado de Cadastro de Imóvel Rural |
| `MDA-CAFCredential-template.html` | Extrato Público da UFPA - CAF |
| `MGI-CARDocument-template.html` | Demonstrativo do Status no CAR (3 páginas) |
| `MGI-CARReceipt-template.html` | Recibo de Inscrição do Imóvel Rural no CAR |

**Ponto de atenção:** O template `MDA-CAFCredential-template.html` usa variáveis
Velocity com sintaxe incorreta (ex: `$rowProperties.Ultima Atualização` com espaço,
`$membros -> nome` com setas). Precisam ser corrigidas para bater com os campos
reais retornados pelo `CAFDataProvider` do certify.

---

### 14. `mimoto-issuers-config.json`

**Arquivo:** `src/main/resources/mimoto-issuers-config.json`

**O que muda:** Substituir o issuer `StayProtected` pelos issuers INJIBR:
`INCRA`, `MGI` e `MDA`.

**Como aplicar na v0.19.2:** Substituir o conteúdo do arquivo pelo do patch.
Verificar se a v0.19.2 adicionou campos novos no schema do issuer config que
precisem ser incluídos.

---

### 15. `application-local.properties`

**Arquivo:** `src/main/resources/application-local.properties`

**Mudanças:**
- URLs de `api-internal.dev1.mosip.net` → `localhost`
- `keycloak.internal.url` sem `/auth` (Keycloak 17+)
- `token.request.issuerUrl` sem `/auth`
- `auth.server.admin.issuer.uri` sem `/auth`
- `mosip.oidc.p12.password` → `abc123`
- `mosip.iam.adapter.clientsecret` → novo valor
- `server.port` → `8099` (em `bootstrap.properties`)
- Adicionar propriedades `sso.*`

**Como aplicar na v0.19.2:** Aplicar as mudanças de URL e senha. As propriedades
`sso.*` são novas e devem ser adicionadas ao final.

---

### 16. `bootstrap.properties`

**Arquivo:** `src/main/resources/bootstrap.properties`

**O que muda:** `server.port` de `8088` para `8099`.

**Como aplicar na v0.19.2:** Verificar se a v0.19.2 já usa porta diferente.

---

### 17. Certificados binários

**Arquivos:**
- `src/main/resources/certs/keystore.p12`
- `src/main/resources/certs/keystoreold.p12`
- `src/main/resources/certs/oidckeystore.p12`

**Como aplicar na v0.19.2:** Copiar os arquivos binários do patch diretamente.
Verificar se a v0.19.2 já tem keystores próprios que não devem ser sobrescritos.

---

### 18. `pom.xml`

**O que muda:** Versão de `0.17.0` para `1.0.0` (versão INJIBR).

**Como aplicar na v0.19.2:** A versão atual já é `0.19.2`. Manter `0.19.2` ou
definir a versão INJIBR conforme padrão do projeto.

---

### 19. Infraestrutura INJIBR (portar direto)

| Arquivo | Descrição |
|---|---|
| `Jenkinsfile` | Pipeline Dataprev/prevnet |
| `README.adoc` | Documentação de builds INJIBR |
| `update_script.ps1` | Script PowerShell de atualização |
| `update_script.sh` | Script Bash de atualização |
| `.github/workflows/clear-artifacts.yml` | Cron de `0 * * * *` → `0 0 1 * *` (mensal) |

---

### 20. Testes

**Arquivos:**
- `src/test/java/io/mosip/mimoto/service/IssuersServiceTest.java`
- `src/test/java/io/mosip/mimoto/util/IssuerConfigUtilTest.java`

**O que muda:** Atualizar mocks para a nova assinatura de `getIssuerWellknown(host, issuerId)`.

**Como aplicar na v0.19.2:** Atualizar os `Mockito.when()` para incluir o segundo
parâmetro `issuerId`. Verificar se há outros testes que chamam `getIssuerWellknown`
e precisam ser atualizados.

---

## Ordem de Aplicação Recomendada

1. **Pacote `govbr`** — novos arquivos (sem dependência de outros)
2. **`VCCredentialRequest.java`** — campos `doctype` e `issuerId`
3. **`IssuerConfigUtil.java`** — nova assinatura `getIssuerWellknown(host, issuerId)`
4. **`IssuersServiceImpl.java`** — usar nova assinatura + remover AuthServer wellknown
5. **`CredentialServiceImpl.java`** — injetar doctype/issuerId + comentar throw de verificação
6. **`DataShareServiceImpl.java`** — comentar push real
7. **`IdpServiceImpl.java`** — adicionar `@Slf4j` e log
8. **`CredentialsController.java`** — trocar token call por `govBRService.getToken()`
9. **`IdpController.java`** — trocar token call por `govBRService.getToken()`
10. **`CredentialShareController.java`** — adicionar log
11. **`Utilities.java`** — corrigir path de templates
12. **Templates HTML** — copiar 4 arquivos novos
13. **`mimoto-issuers-config.json`** — substituir issuers
14. **Properties** — `application-local.properties` + `bootstrap.properties`
15. **Certificados** — copiar keystores binários
16. **Testes** — atualizar mocks
17. **Infraestrutura** — `Jenkinsfile`, `README.adoc`, scripts

---

## Pontos de Atenção

1. **`getTokenResponse()` removido:** Os controllers `CredentialsController` e
   `IdpController` chamavam `credentialService.getTokenResponse()`. Na v0.19.2
   verificar se esse método ainda existe — se foi removido, o código já não compila
   e a substituição por `govBRService.getToken()` é obrigatória.

2. **`VCVerificationException` comentada:** Desabilitar a verificação de VC é uma
   decisão de compatibilidade com o formato govbr. Verificar se a v0.19.2 mudou
   o verificador para suportar o formato INJIBR antes de manter comentado.

3. **`DataShare` retornando `"sas"`:** Solução temporária. Verificar se o DataShare
   é usado em algum outro fluxo na v0.19.2 que possa ser afetado.

4. **Templates com sintaxe Velocity incorreta:** `MDA-CAFCredential-template.html`
   tem variáveis como `$rowProperties.Ultima Atualização` (espaço no nome) e
   `$membros -> nome` (setas) que não são sintaxe Velocity válida. Precisam ser
   corrigidas para bater com os campos do `CAFDataProvider`.

5. **Keystores:** Os arquivos `keystore.p12` e `oidckeystore.p12` contêm chaves
   criptográficas. Verificar se são os corretos para o ambiente de destino antes
   de sobrescrever.

6. **Cache `issuerId`:** O nome do cache foi mudado de `issuerWellknown` para
   `issuerId`. Verificar se a configuração de cache da v0.19.2 (`CacheConfig.java`)
   precisa ser atualizada para incluir o novo nome.

7. **`AuthorizationServerWellKnownResponse` vazio:** Retornar um objeto vazio pode
   causar `NullPointerException` em código da v0.19.2 que acesse campos desse objeto.
   Verificar todos os usos de `CredentialIssuerConfiguration` na v0.19.2.

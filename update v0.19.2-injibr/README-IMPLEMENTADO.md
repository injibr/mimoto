# README — Alterações Implementadas (v0.19.2-injibr)

Este documento descreve todas as alterações efetivamente implementadas na migração
da branch `entrega` (baseada em v0.17.0) para a v0.19.2, nesta sessão de trabalho.

---

## 1. `pom.xml`

Versão alterada de `0.19.2` para `4.0.0` (versão INJIBR).

---

## 2. `CredentialsController.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/controller/CredentialsController.java`

- Adicionado `@Autowired GovBRService govBRService`
- Substituída chamada `idpService.getTokenResponse(params)` por `govBRService.getToken(code, codeVerifier)`

```java
// INJIBR-CUSTOM: govbr uses its own token endpoint via GovBRService instead of esignet
// TokenResponseDTO response = idpService.getTokenResponse(params);
TokenResponseDTO response = govBRService.getToken(params.get("code"), params.get("code_verifier"));
```

---

## 3. `IdpController.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/controller/IdpController.java`

- Adicionado `@Autowired GovBRService govBRService`
- Substituída chamada `idpService.getTokenResponse(params)` por `govBRService.getToken(code, codeVerifier)` no endpoint `get-token/{issuer}`

```java
// INJIBR-CUSTOM: govbr uses its own token endpoint via GovBRService instead of esignet
// TokenResponseDTO response = idpService.getTokenResponse(params);
TokenResponseDTO response = govBRService.getToken(params.get("code"), params.get("code_verifier"));
```

---

## 4. `VCCredentialRequest.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/dto/mimoto/VCCredentialRequest.java`

Adicionados campos `doctype` e `issuerId`:

```java
// INJIBR-CUSTOM: certify uses doctype and issuerId for credential dispatch and multi-issuer lookup
private String doctype;
private String issuerId;
```

**Motivo:** o certify usa `docType` no `DataProviderPluginImpl` para despachar ao provider correto
(CAR, CAF, CCIR, etc) e `issuerId` para o lookup multi-issuer na `credential_config`.

---

## 5. `CredentialServiceImpl.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/service/impl/CredentialServiceImpl.java`

**a) Injeção de `doctype` e `issuerId` após `buildRequest()`**

```java
// INJIBR-CUSTOM: certify uses doctype and issuerId for credential dispatch and multi-issuer lookup
vcCredentialRequest.setDoctype(credentialConfigurationId);
vcCredentialRequest.setIssuerId(issuerId);
```

**b) Bypass de verificação de VC**

O verificador do mimoto não suporta o formato de VC assinado pelo certify com `Ed25519Signature2020`.
O método `verifyCredential()` foi alterado para retornar `true` silenciosamente em caso de falha:

```java
// INJIBR-CUSTOM: VC verification failure does not block issuance (govbr VC format compatibility)
// return credentialVerifierService.verify(vcCredentialResponse);
credentialVerifierService.verify(vcCredentialResponse);
return true;
// ...
// INJIBR-CUSTOM: VC verification failure does not block issuance (govbr VC format compatibility)
// throw new VCVerificationException(...);
return true;
```

---

## 6. `DataShareServiceImpl.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/service/impl/DataShareServiceImpl.java`

O DataShare do MOSIP não está disponível no ambiente govbr. O método `storeDataInDataShare()`
retorna placeholder `"sas"` em vez de chamar o DataShare real:

```java
// INJIBR-CUSTOM: DataShare not used in govbr flow, returning placeholder
return "sas";
```

**Impacto:** o QR code de apresentação (OpenID4VP) dentro do PDF gerado não funcionará.
O download do PDF em si não é afetado.

---

## 7. `IdpServiceImpl.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/service/impl/IdpServiceImpl.java`

- Adicionado `@Slf4j` e import `lombok.extern.slf4j.Slf4j`
- Adicionado log do nome do keystore em `constructGetTokenRequest()`

```java
log.info("KeyStore filename: {}", fileName);
```

---

## 8. `IssuersServiceImpl.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/service/impl/IssuersServiceImpl.java`

- `getIssuerWellknown()` passa a receber `issuerId` como segundo parâmetro em ambas as chamadas
- Removida chamada a `getAuthServerWellknown()` — govbr não expõe AuthorizationServer well-known
- Retornado `new AuthorizationServerWellKnownResponse()` vazio em `getIssuerConfiguration()`

```java
// INJIBR-CUSTOM: govbr does not expose AuthorizationServer well-known; skip the call
// AuthorizationServerWellKnownResponse authorizationServerWellKnownResponse =
//     issuersConfigUtil.getAuthServerWellknown(...);
```

---

## 9. `IssuerConfigUtil.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/util/IssuerConfigUtil.java`

- Assinatura de `getIssuerWellknown()` alterada para receber `issuerId` como segundo parâmetro
- Cache alterado de `"issuerWellknown"` (chave por URL) para `"issuerId"` (chave por issuerId)
- URL do well-known passa a incluir `?issuer_id=` para o certify filtrar por issuer

```java
// INJIBR-CUSTOM: cache by issuerId (not URL) and append issuer_id param to well-known endpoint
@Cacheable(value = "issuerId", key = "#p1")
public CredentialIssuerWellKnownResponse getIssuerWellknown(String credentialIssuerHost, String issuerId) {
    String wellknownEndpoint = credentialIssuerHost + "/.well-known/openid-credential-issuer?issuer_id=" + issuerId;
```

---

## 10. `CredentialShareController.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/controller/CredentialShareController.java`

Adicionado log no início do método `download()`:

```java
log.info("Calling download credential for request id in credential share: {}", requestDTO.getRequestId());
```

---

## 11. `Utilities.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/util/Utilities.java`

- Path base de templates corrigido para desenvolvimento local: `"templates"` → `"src/main/resources/templates"`
- Removido `ClassPathResource` inútil — leitura feita diretamente via `Files.readString(resolvedPath)`

```java
// INJIBR-CUSTOM: fix template path for local development profile
Path basePath = Paths.get("src/main/resources/templates").toAbsolutePath().normalize();
// ...
// INJIBR-CUSTOM: read directly from resolved path instead of ClassPathResource
return Files.readString(resolvedPath);
```

---

## 12. `RestApiClient.java`

**Arquivo:** `src/main/java/io/mosip/mimoto/util/RestApiClient.java`

Método `getApi()` com `queryParams` adicionado comentado — era código experimental não usado.
A solução adotada foi passar `?issuer_id=` diretamente na URL em `IssuerConfigUtil`.

```java
// INJIBR-CUSTOM: experimental method to pass queryParams to getApi, not used
// — issuer_id appended directly in IssuerConfigUtil
```

---

## 13. `application-local.properties`

**Arquivo:** `src/main/resources/application-local.properties`

- Removido `/auth` das URLs do Keycloak (`token.request.issuerUrl`, `auth.server.admin.issuer.uri`,
  `mosip.iam.adapter.issuerURL`) — Keycloak 17+ não usa o prefixo `/auth`
- Adicionadas propriedades `sso.*` para o `GovBRServiceImpl`

```properties
# INJIBR-CUSTOM: SSO govbr endpoints para autenticação
sso.token-url=to_set
sso.userinfo-url=to_set
sso.redirect-uri=to_set
sso.auth-header=to_set
```

---

## 14. Pacote `govbr` (novos arquivos)

**Módulo:** `src/main/java/io/mosip/mimoto/govbr/`

Todos os arquivos abaixo são novos — portados diretamente da `entrega` v0.17.0:

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

---

## 15. Templates HTML (novos arquivos)

**Diretório:** `src/main/resources/templates/`

| Arquivo | Credencial |
|---|---|
| `INCRA-CCIRCredential-template.html` | Certificado de Cadastro de Imóvel Rural |
| `MDA-CAFCredential-template.html` | Extrato Público da UFPA - CAF |
| `MGI-CARDocument-template.html` | Demonstrativo do Status no CAR |
| `MGI-CARReceipt-template.html` | Recibo de Inscrição do Imóvel Rural no CAR |

---

## 16. `mimoto-issuers-config.json`

**Arquivo:** `src/main/resources/mimoto-issuers-config.json`

Substituídos issuers de exemplo (`StayProtected`) pelos issuers INJIBR: `INCRA`, `MGI` e `MDA`.

---

## 17. Keystores binários

**Diretório:** `src/main/resources/certs/`

Arquivos copiados do `scm/entrega`:
- `keystore.p12`
- `keystoreold.p12`
- `oidckeystore.p12`

---

## 18. `Dockerfile`

**Arquivo:** `Dockerfile`

Imagem base alterada de `eclipse-temurin:21-jre-alpine` para `eclipse-temurin:21-jre`
e comandos `apk` substituídos por `apt-get` para evitar problemas de TLS com proxy corporativo.

```dockerfile
# INJIBR-CUSTOM: switched from alpine to jre base to avoid TLS issues with corporate proxy
FROM eclipse-temurin:21-jre
```

---

## 19. Infraestrutura INJIBR (novos arquivos)

| Arquivo | Descrição |
|---|---|
| `Jenkinsfile` | Pipeline Dataprev/prevnet |
| `README.adoc` | Documentação de builds INJIBR |
| `update_script.ps1` | Script PowerShell de atualização |
| `update_script.sh` | Script Bash de atualização |
| `.github/workflows/clear-artifacts.yml` | Cron alterado para mensal (`0 0 1 * *`) |

---

## 20. Testes

**Arquivos:**
- `src/test/java/io/mosip/mimoto/service/IssuersServiceTest.java` — todos os mocks de `getIssuerWellknown` atualizados para 2 parâmetros `(host, issuerId)`
- `src/test/java/io/mosip/mimoto/util/IssuerConfigUtilTest.java` — idem

---

## Status geral

| Item | Status |
|---|---|
| Pacote `govbr` | ✅ implementado |
| `VCCredentialRequest` — `doctype` + `issuerId` | ✅ implementado |
| `IssuerConfigUtil` — nova assinatura + cache + `?issuer_id=` | ✅ implementado |
| `IssuersServiceImpl` — nova assinatura + remover AuthServer wellknown | ✅ implementado |
| `CredentialServiceImpl` — injetar doctype/issuerId + bypass verificação | ✅ implementado |
| `DataShareServiceImpl` — bypass DataShare | ✅ implementado |
| `IdpServiceImpl` — `@Slf4j` + log | ✅ implementado |
| `CredentialsController` — `govBRService.getToken()` | ✅ implementado |
| `IdpController` — `govBRService.getToken()` | ✅ implementado |
| `CredentialShareController` — log | ✅ implementado |
| `Utilities` — path templates + remover ClassPathResource | ✅ implementado |
| Templates HTML | ✅ implementado |
| `mimoto-issuers-config.json` | ✅ implementado |
| `application-local.properties` — Keycloak 17+ + `sso.*` | ✅ implementado |
| Keystores binários | ✅ implementado |
| `Dockerfile` — base image | ✅ implementado |
| Testes | ✅ implementado |
| Infraestrutura (`Jenkinsfile`, scripts, etc) | ✅ implementado |

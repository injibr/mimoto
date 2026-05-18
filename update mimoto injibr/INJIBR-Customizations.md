# INJIBR — Customizações do mimoto

Este documento descreve todas as customizações feitas pelo time INJIBR sobre o upstream
`mimoto` da MOSIP/inji. Serve como referência para futuras atualizações de versão e como
contexto para ferramentas de IA assistindo no processo.

---

## Visão Geral

O mimoto upstream foi adaptado para o fluxo govbr (Gov.br SSO) com múltiplos emissores
(MGI, INCRA, MDA). As principais diferenças em relação ao upstream são:

- Autenticação via Gov.br SSO em vez de eSignet
- Pacote `govbr` completo para integração com SSO govbr
- `doctype` e `issuerId` injetados no request ao certify para dispatch multi-issuer
- Bypass de verificação de VC (formato govbr não compatível com verificador upstream)
- `IssuerConfigUtil` com cache por `issuerId` e parâmetro `?issuer_id=` no well-known
- `IssuersServiceImpl` sem chamada ao AuthorizationServer well-known (govbr não expõe)
- Dockerfile com base `eclipse-temurin:21-jre` (Debian) em vez de Alpine
- Templates HTML para credenciais INJIBR (CAR, CCIR, CAF)

---

## Arquivos Modificados

### `src/main/java/io/mosip/mimoto/controller/CredentialsController.java`

**Customizações:**

1. **Substituição do token endpoint**
   - `@Autowired GovBRService govBRService`
   - Substituída chamada `idpService.getTokenResponse(params)` por `govBRService.getToken(code, codeVerifier, redirectUri)`
   - Comentário: `// INJIBR-CUSTOM: govbr uses its own token endpoint via GovBRService instead of esignet`

---

### `src/main/java/io/mosip/mimoto/controller/IdpController.java`

**Customizações:**

1. **Substituição do token endpoint no `get-token/{issuer}`**
   - `@Autowired GovBRService govBRService` adicionado ao constructor injection do upstream
   - Substituída chamada `idpService.getTokenResponse(params)` por `govBRService.getToken(code, codeVerifier)`
   - Comentário: `// INJIBR-CUSTOM: govbr uses its own token endpoint via GovBRService instead of esignet`

---

### `src/main/java/io/mosip/mimoto/dto/mimoto/VCCredentialRequest.java`

**Customizações:**

1. **Campos `doctype` e `issuerId`**
   - Necessários para o certify despachar ao provider correto e fazer lookup multi-issuer
   - Comentário: `// INJIBR-CUSTOM: certify uses doctype and issuerId for credential dispatch and multi-issuer lookup`

---

### `src/main/java/io/mosip/mimoto/service/impl/CredentialServiceImpl.java`

**Customizações:**

1. **Injeção de `doctype` e `issuerId` após `buildRequest()`**
   - `vcCredentialRequest.setDoctype(credentialConfigurationId)`
   - `vcCredentialRequest.setIssuerId(issuerId)`

2. **Bypass de verificação de VC**
   - O verificador upstream não suporta `Ed25519Signature2020` do certify govbr
   - `verifyCredential()` retorna `true` silenciosamente em caso de falha
   - Comentário: `// INJIBR-CUSTOM: VC verification failure does not block issuance (govbr VC format compatibility)`

---

### `src/main/java/io/mosip/mimoto/service/impl/IssuersServiceImpl.java`

**Customizações:**

1. **`getIssuerWellknown` com 2 parâmetros**
   - Passa `issuerId` como segundo argumento em `getIssuerConfiguration()` e `getIssuerConfig()`

2. **Skip do AuthorizationServer well-known**
   - Gov.br não expõe AuthorizationServer well-known
   - Retornado `new AuthorizationServerWellKnownResponse()` vazio
   - Comentário: `// INJIBR-CUSTOM: govbr does not expose AuthorizationServer well-known; skip the call`

---

### `src/main/java/io/mosip/mimoto/util/IssuerConfigUtil.java`

**Customizações:**

1. **Assinatura de `getIssuerWellknown` com `issuerId`**
   - Cache alterado para chave por `issuerId` em vez de URL
   - URL do well-known inclui `?issuer_id=` para o certify filtrar por issuer
   - Comentário: `// INJIBR-CUSTOM: cache by issuerId (not URL) and append issuer_id param to well-known endpoint`

---

### `src/main/java/io/mosip/mimoto/service/impl/DataShareServiceImpl.java`

**Customizações:**

1. **DataShare ativo com bypass comentado**
   - Bypass `return "sas"` mantido comentado — reativar se DataShare não estiver disponível
   - Comentário: `// INJIBR-CUSTOM: DataShare not used in govbr flow, returning placeholder`

---

### `src/main/java/io/mosip/mimoto/service/impl/IdpServiceImpl.java`

**Customizações:**

1. **`@Slf4j` e log do keystore**
   - `log.info("KeyStore filename: {}", fileName)` adicionado em `constructGetTokenRequest()`

---

### `src/main/java/io/mosip/mimoto/govbr/` (pacote novo)

Todos os arquivos abaixo são INJIBR — não existem no upstream:

| Arquivo | Descrição |
|---|---|
| `ApiResponse.java` | Wrapper genérico de resposta da API govbr |
| `GovBRController.java` | Endpoint `GET /user/profile` — perfil do usuário govbr |
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

### `src/main/resources/templates/` (novos arquivos)

Templates HTML para renderização das credenciais INJIBR:

| Arquivo | Credencial |
|---|---|
| `INCRA-CCIRCredential-template.html` | Certificado de Cadastro de Imóvel Rural |
| `MDA-CAFCredential-template.html` | Extrato Público da UFPA - CAF |
| `MGI-CARDocument-template.html` | Demonstrativo do Status no CAR |
| `MGI-CARReceipt-template.html` | Recibo de Inscrição do Imóvel Rural no CAR |

---

### `src/main/resources/mimoto-issuers-config.json`

Substituídos issuers de exemplo pelos issuers INJIBR: `INCRA`, `MGI` e `MDA`.

---

### `src/main/resources/application-local.properties`

**Customizações:**

1. **Propriedades `sso.*` para o `GovBRServiceImpl`**
   ```properties
   # INJIBR-CUSTOM: SSO govbr endpoints para autenticação
   sso.token-url=to_set
   sso.userinfo-url=to_set
   sso.redirect-uri=to_set
   sso.auth-header=to_set
   ```

2. **Google OAuth2 com valores dummy** (feature desabilitada no govbr)

---

### `Dockerfile`

**Customizações:**

1. **Base image Debian em vez de Alpine**
   - `FROM eclipse-temurin:21-jre` (era `eclipse-temurin:21-jre-alpine`)
   - `apk` substituído por `apt-get`
   - Necessário para evitar problemas de TLS com proxy corporativo (Zscaler)

---

### `pom.xml`

- Versão: seguir convenção INJIBR (`MAJOR.MINOR.PATCH`)

---

## Banco de Dados

Sem alterações no DDL — o mimoto usa o schema `inji_mimoto` padrão do upstream.

---

## Fluxo govbr no mimoto

```
InjWeb → mimoto /credentials/download
    1. govBRService.getToken(code, codeVerifier, redirectUri) → access_token
    2. issuersService.getIssuerConfiguration(issuerId)
       → IssuerConfigUtil.getIssuerWellknown(host, issuerId)
       → certify /.well-known/openid-credential-issuer?issuer_id=MGI
    3. credentialRequestService.buildRequest(...)
    4. vcCredentialRequest.setDoctype(credentialConfigurationId)  ← INJIBR
    5. vcCredentialRequest.setIssuerId(issuerId)                  ← INJIBR
    6. certify /issuance/credential → VC assinada
    7. verifyCredential() → bypass (retorna true)                 ← INJIBR
    8. credentialPDFGeneratorService.generatePdf(...)
```

---

## Configurações Relevantes (mimoto-default.properties)

```properties
# SSO GovBR
sso.token-url=https://sso.staging.acesso.gov.br/token
sso.userinfo-url=https://sso.staging.acesso.gov.br/userinfo/
sso.redirect-uri=http://localhost:30300/redirect
sso.auth-header=Basic <base64(client_id:client_secret)>

# Cache Redis
spring.cache.type=redis

# Database (novo padrão v0.22.0)
inji.mimoto.database.hostname=<hostname>
inji.mimoto.database.port=5432
```

---

## Checklist de Migração (após rebase para nova versão upstream)

Arquivos que **sempre** precisam revisão manual após rebase:

- [ ] `CredentialsController.java` — `govBRService.getToken()` com 3 parâmetros
- [ ] `IdpController.java` — `govBRService.getToken()` + constructor injection upstream
- [ ] `VCCredentialRequest.java` — campos `doctype` e `issuerId`
- [ ] `CredentialServiceImpl.java` — injeção doctype/issuerId + bypass verificação
- [ ] `IssuersServiceImpl.java` — `getIssuerWellknown(host, issuerId)` + skip AuthServer wellknown
- [ ] `IssuerConfigUtil.java` — assinatura com `issuerId` + cache + `?issuer_id=`
- [ ] `IssuersServiceTest.java` — mocks com 2 parâmetros em `getIssuerWellknown`
- [ ] `Dockerfile` — base image `eclipse-temurin:21-jre` com `apt-get`
- [ ] `pom.xml` — versão INJIBR correta + verificar versões mosip (`1.3.0` vs `1.3.0-beta.1`)
- [ ] `application-default.properties` — verificar URLs de ignore-auth e novas propriedades
- [ ] `CacheConfig.java` — verificar se `spring.cache.type=redis` está configurado no config server

---

## Regras para IA (Amazon Q / Copilot)

Ao assistir em tarefas neste repositório, seguir estas regras:

### Regras gerais

1. **Nunca remover customizações INJIBR** marcadas com comentário `// INJIBR-CUSTOM`
2. **Nunca remover o bypass de verificação de VC** — o formato govbr não é compatível com o verificador upstream
3. **Nunca remover `doctype` e `issuerId`** do `VCCredentialRequest` — são necessários para o certify

### Regras de rebase

4. Ao fazer rebase, aplicar o checklist acima em ordem
5. O upstream pode refatorar para constructor injection — manter o padrão upstream e adicionar `@Autowired GovBRService` separado
6. Verificar versões das libs mosip — upstream usa `1.3.0`, INJIBR pode precisar de `1.3.0-beta.1`
7. `spring.cache.type=redis` deve estar no config server — sem ele o Spring auto-detecta Redis com JDK serializer que quebra

### Regras de código

8. Manter comentários `// INJIBR-CUSTOM` em todas as customizações
9. O pacote `govbr` é completamente INJIBR — nunca remover
10. Templates HTML em `src/main/resources/templates/` são INJIBR — nunca remover

---

## Versionamento

O INJIBR usa versionamento semântico próprio (`MAJOR.MINOR.PATCH`), independente do upstream.

- `MAJOR` — incrementado a cada rebase sobre uma nova versão upstream
- `MINOR` — incrementado para novas funcionalidades INJIBR dentro da mesma base upstream
- `PATCH` — incrementado para correções de bugs

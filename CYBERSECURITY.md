# Cybersecurity README (Ford VIN Share)

Este documento descreve a camada de segurança aplicada ao projeto `ford-vinshare` (escopo acadêmico/portfólio), seguindo princípios de OWASP Top 10, Secure by Default e uma abordagem Zero Trust (verificação explícita, privilégio mínimo, negação por padrão).

## 1. Visão Geral da Arquitetura de Segurança

Componentes principais implementados:

- Autenticação e autorização com Spring Security (JWT stateless + refresh token rotativo).
- RBAC com roles: `ADMIN`, `ANALYST`, `USER`.
- Hardening HTTP (headers, CSP, HSTS, clickjacking, content sniffing).
- CORS restritivo via allowlist.
- Rate limiting in-memory (por IP e por usuário).
- Normalização/sanitização centralizada de strings (Unicode NFKC + trim + bloqueio de controles).
- Validação forte com Bean Validation (regex/size/whitelist) nos DTOs de entrada.
- Criptografia em repouso (AES-256-GCM) para campos sensíveis via JPA `AttributeConverter`.
- Logs estruturados em JSON com `requestId` e trilha de auditoria persistida em banco.
- DevSecOps mínimo (CodeQL, Dependency-Check, secret scanning com Gitleaks, Dependabot).
- Testes básicos de segurança (auth, RBAC, rate limit, validação).

## 2. Ameaças Mitigadas (Exemplos)

- SQL Injection: uso de JPA repositories + validação/normalização de entradas; não há queries string-concat no código.
- XSS refletido: API é JSON; ainda assim entradas são normalizadas e controles são bloqueados para reduzir vetores de log/header injection.
- Mass Assignment: DTOs explícitos + `spring.jackson.deserialization.fail-on-unknown-properties=true`.
- Brute force: lock temporário após N falhas de login + rate limit em `/api/auth/*`.
- Replay de refresh token: refresh token rotativo + detecção de reuso após rotação (revogação em massa + bump de `tokenVersion`).
- Clickjacking: `X-Frame-Options: DENY` e `frame-ancestors 'none'`.
- MIME sniffing: `X-Content-Type-Options: nosniff`.
- CSRF: arquitetura stateless com Bearer token. Refresh token está em cookie HttpOnly; endpoints permanecem POST, mas o fluxo recomendado é SPA consumindo API sob HTTPS e CORS allowlist.

## 3. Decisões Técnicas (Por Escopo Acadêmico)

Implementado de forma prática e funcional, com simplificações conscientes:

- Rate limiting é in-memory (não distribuído). Em produção: preferir API Gateway/WAF/Redis.
- Criptografia em repouso usa chave via env (`APP_CRYPTO_KEY_BASE64`). Em `dev/test`, se a chave não estiver configurada, uma chave efêmera é gerada (dados criptografados não sobrevivem reinício).
- Rotação/revogação completa de sessões foi implementada focando refresh tokens. Access tokens são curtos e verificados com `tokenVersion`.
- Payload signature (HMAC) é opcional e desligada por padrão. É um “demo” de integridade em trânsito para cenários B2B/internal.

## 4. Fluxo de Autenticação (JWT + Refresh Rotativo)

Endpoints:

- `POST /api/auth/register` cria usuário `USER`.
- `POST /api/auth/login` autentica e retorna `accessToken` e cookie `refresh_token` (HttpOnly).
- `POST /api/auth/refresh` rotaciona o refresh token e retorna novo `accessToken` + novo cookie.
- `POST /api/auth/logout` revoga o refresh token atual e limpa cookie.

Detalhes:

- Access token:
  - TTL curto (default: 5 min).
  - Claims: `sub` (userId), `role` (`ROLE_*`), `tv` (token version), `jti`.
- Refresh token:
  - Aleatório (32 bytes) e armazenado apenas como hash SHA-256 no banco.
  - Rotação a cada refresh.
  - Reuso após rotação é tratado como replay (revogação em massa + incremento de `tokenVersion`).
- Senha:
  - Hash: BCrypt forte (custo 12).
  - Policy mínima (register): 12+ chars, maiúscula, minúscula, número, símbolo.
  - Proteção brute force: lock temporário após 5 falhas.

## 5. RBAC (Princípio do Menor Privilégio)

Roles:

- `ADMIN`: operações de escrita/remoção (cadastros e manutenção).
- `ANALYST`: leitura de dados consolidados e gestão de leads/manutenções (leitura).
- `USER`: leitura básica (onde aplicável).

Aplicação:

- Method-level security com `@PreAuthorize` nos controllers.
- Security default: `deny by default` (qualquer rota fora allowlist exige JWT).

## 6. Proteção de APIs (Hardening)

Implementações:

- Headers: HSTS, CSP, X-Frame-Options, nosniff, Referrer-Policy, Permissions-Policy.
- CORS: allowlist via `APP_CORS_ALLOWED_ORIGINS` (sem wildcard).
- Rate limiting:
  - Por IP e por usuário para `/api/*`.
  - Mais restritivo para `/api/auth/*` (anti brute force/scraping).
- Erros seguros:
  - Não expõem stack trace, paths internos, detalhes do banco/ORM.
  - Sempre retornam payload padronizado com `requestId`.

## 7. Criptografia em Repouso

Campos criptografados (AES-256-GCM) via annotation `@EncryptedString`:

- `Cliente.email`
- `Cliente.telefone`
- `Manutencao.observacoes`

Chave:

- `APP_CRYPTO_KEY_BASE64`: base64 de 32 bytes.

Nota:

- Migração: versões antigas podem ter valores em plaintext; por escopo acadêmico, o converter aceita plaintext ao ler. Em produção, a migração deve ser obrigatória e “fail closed”.

## 8. Logs, Observabilidade e Auditoria

Logs:

- Logback em JSON (`logback-spring.xml`).
- `X-Request-Id` é gerado/propagado e incluído no MDC para rastreabilidade.
- Evita logar dados sensíveis (refresh token, senhas, etc).

Auditoria:

- Tabela `audit_events` com eventos de auth e operações críticas (create/update/delete).
- Endpoint admin: `GET /api/audit` retorna últimos 100 eventos (apenas `ADMIN`).

## 9. DevSecOps (CI/CD Mínimo)

Arquivos adicionados:

- `.github/workflows/codeql.yml`: SAST com CodeQL (Java).
- `.github/workflows/security.yml`:
  - Secret scanning com Gitleaks
  - OWASP Dependency-Check (CVE scanning)
  - Build + tests
- `.github/dependabot.yml`: atualizações semanais de dependências Maven

## 10. Variáveis de Ambiente Necessárias

Arquivo exemplo: `.env.example`

Obrigatórias (para rodar com segurança real):

- `APP_JWT_SECRET_BASE64`
- `APP_CRYPTO_KEY_BASE64`
- `DB_PASSWORD` (se o Postgres estiver com senha)

Recomendadas:

- `APP_CORS_ALLOWED_ORIGINS`
- `APP_COOKIES_SECURE=true` quando atrás de HTTPS
- `APP_BOOTSTRAP_ADMIN_EMAIL` / `APP_BOOTSTRAP_ADMIN_PASSWORD`
- `APP_BOOTSTRAP_ANALYST_EMAIL` / `APP_BOOTSTRAP_ANALYST_PASSWORD`

## 11. Checklist OWASP (Resumo)

- A01 Broken Access Control: RBAC + deny by default + method security.
- A02 Cryptographic Failures: AES-GCM at-rest + segredos via env.
- A03 Injection: DTO validation + JPA + fail-on-unknown-properties.
- A05 Security Misconfiguration: headers + safe defaults + stacktrace off.
- A07 Identification & Auth Failures: Argon2 + refresh rotation + lock + rate limit.
- A09 Logging & Monitoring: requestId + audit trail + JSON logs.

## 12. Checklist LGPD/GDPR (Básico)

- Minimização: apenas campos necessários (e-mail/telefone).
- Proteção: criptografia em repouso para PII.
- Logging: evitar PII em logs/auditoria.
- Retenção/expurgo (política):
  - Para escopo acadêmico, a política está documentada; automação pode ser futura (job/cron).

## 13. Instruções de Segurança para Deploy

1. Rodar atrás de HTTPS/TLS 1.2+ (reverse proxy como Nginx/Traefik).
2. Definir `APP_JWT_SECRET_BASE64` e `APP_CRYPTO_KEY_BASE64` com valores aleatórios fortes.
3. Definir `APP_COOKIES_SECURE=true`.
4. Configurar `APP_CORS_ALLOWED_ORIGINS` com domínios reais (sem `*`).
5. Desabilitar Swagger em produção (recomendação futura: profile `prod` bloqueando `/swagger-ui/**`).
6. Garantir que banco use credenciais fortes e rotações de segredo (fora do escopo acadêmico).

## 14. Limitações Conhecidas (Escopo Acadêmico)

- Rate limiting não é distribuído.
- Não há integração com Secret Manager (Vault/ASM/AKV); secrets são via env.
- Não há MFA/TOTP implementado (pode ser adicionado como evolução).
- Integração de WAF/IDS não faz parte do repositório.
- Não foi possível executar `mvn test` neste ambiente sandbox por restrição de escrita no repositório Maven do usuário; os testes foram adicionados e devem rodar em ambiente local/CI.

## 15. Recomendações Futuras

1. Adicionar profile `prod` para bloquear Swagger e habilitar “fail closed” em criptografia.
2. Migrar rate limiting para Redis/API Gateway.
3. Implementar MFA (TOTP) opcional para `ADMIN`.
4. Implementar rotação programática de chaves (JWT e AES) com dual-key de transição.
5. Adicionar endpoint de “revoke all sessions” para o próprio usuário e para admin.

## 16. Exemplos de Ataques Mitigados (Como Testar)

- Brute force:
  - Repetir `POST /api/auth/login` com senha errada e observar lock + rate limit.
- Privilege escalation:
  - Usuário `USER` tentando `POST /api/clientes` recebe 403.
- Payload malformado:
  - Enviar JSON com campos desconhecidos retorna 400.
- VIN malicioso:
  - Enviar `vin: \"../etc/passwd\"` é rejeitado por regex.

## 17. Resposta a Incidentes (Fluxo Simplificado)

1. Detectar via `audit_events` e logs (requestId).
2. Revogar refresh tokens do usuário afetado.
3. Incrementar `tokenVersion` (invalida access tokens).
4. Rotacionar segredos (JWT/AES) quando apropriado e reemitir tokens.

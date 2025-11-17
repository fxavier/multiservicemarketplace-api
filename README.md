# Multiservice Marketplace API

Projeto base em **Spring Boot 3** + **Spring Modulith** para suportar o marketplace multi-tenant/multi-service descrito no blueprint arquiteto. Esta fundação já inclui os módulos principais (IAM, Tenancy, Catálogo, Clientes, Checkout, Scheduling, Dashboard) e integrações essenciais: Web, Security, Data JPA, Flyway, PostgreSQL, MapStruct, Lombok e Actuator.

## Requisitos
- Java 21 (OpenJDK).
- Maven Wrapper (`./mvnw`) – já presente no repositório.
- PostgreSQL local (opcional para desenvolvimento inicial, mas exigido para execução completa).

## Estrutura de pacotes (Spring Modulith)
```
com.xavier.multiservicemarketplaceapi
├── checkout
├── customers
├── dashboard
├── iam
├── scheduling
├── sharedkernel
├── catalog
└── tenancy
```
Cada pacote já está anotado com `@ApplicationModule`, permitindo testes e visualizações de módulo via Spring Modulith.

## Configuração
As propriedades residem em `src/main/resources/application.yml`, com perfis:
- `dev` (padrão): aponta para `jdbc:postgresql://localhost:5432/multiservicemarketplace_dev` com utilizador/senha `dev`.
- `test`: usa base separada (`multiservicemarketplace_test`) e desativa Flyway.
- `prod`: espera variáveis `SPRING_DATASOURCE_*`.

Variáveis principais:
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `SERVER_PORT`

## Build e execução
```bash
# Compilar e executar testes
./mvnw verify

# Subir aplicação (perfil dev padrão)
./mvnw spring-boot:run

# Alternar perfil
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```
O endpoint de health fica disponível em `http://localhost:8080/actuator/health`.

## Multi-tenancy
- Todos os requests protegidos devem enviar o cabeçalho `X-Tenant-ID`.
- O `TenantResolverInterceptor` valida o tenant (via `TenantProvider`) e disponibiliza o contexto através de `TenantContextHolder`.
- Paths ignorados (ex.: `/actuator/**`) podem ser configurados em `tenancy.ignored-paths`.

## Próximos passos
- Implementar os agregados e casos de uso em cada módulo.
- Adicionar migrations Flyway em `src/main/resources/db/migration`.
- Escrever testes de módulo com suporte Spring Modulith.

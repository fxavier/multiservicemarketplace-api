# Multiservice Marketplace – Data Model & ERD

## Contexto e Objetivo
Este documento consolida o modelo de dados atualmente implementado no backend FastAPI e o alinha com os bounded contexts definidos no [blueprint arquitetural](blueprint.md). Ele será a referência oficial para a modelagem JPA/Flyway durante a migração para Spring Modulith, cobrindo:

- Inventário das entidades persistidas, respetivas chaves e relacionamentos.
- Diagrama ERD completo em Mermaid.
- Mapa de agregados/roots e value objects seguindo DDD.
- Colunas JSON/Snapshot que exigirão conversores ou `@Embeddable` especializados no novo backend.

## ERD consolidado
> Nota: todas as entidades que herdavam `TenantScopedMixin` possuem `tenant_id` obrigatório apontando para `tenants.id`. Para garantir legibilidade, o relacionamento é explicitado individualmente no diagrama.

```mermaid
erDiagram
    TENANTS {
        uuid id PK
        string nome
        string slug UNIQUE
        bool ativo
        string timezone
        string moeda_padrao
        json config_checkout
        json branding
    }
    ROLES {
        uuid id PK
        string name
        string description
        json permissions
    }
    USERS {
        uuid id PK
        uuid tenant_id FK
        string email UNIQUE
        string password_hash
        enum role
        bool is_active
    }
    MERCHANTS {
        uuid id PK
        uuid tenant_id FK
        string nome
        string slug
        string tipo
        numeric avaliacao
        uuid owner_id FK
        json horario_funcionamento
        numeric pedido_minimo
        bool aceita_produtos
        bool aceita_servicos
    }
    CATEGORIAS {
        uuid id PK
        uuid tenant_id FK
        uuid merchant_id FK
        string nome
        string slug
        bool ativa
    }
    PRODUTOS {
        uuid id PK
        uuid tenant_id FK
        uuid merchant_id FK
        uuid categoria_id FK
        string nome
        numeric preco
        bool disponivel
        int stock_atual
        json imagens
        json atributos_extras
    }
    PRESTADORES {
        uuid id PK
        uuid tenant_id FK
        string nome
        uuid user_id FK
        numeric preco_base
        json profissoes
        json zona_atendimento
        json linguas
    }
    SERVICOS {
        uuid id PK
        uuid tenant_id FK
        uuid prestador_id FK
        uuid categoria_id FK
        string nome
        numeric preco
        int duracao_minutos
        enum tipo_atendimento
        json imagens
        json tags
    }
    CLIENTES {
        uuid id PK
        uuid tenant_id FK
        uuid user_id FK UNIQUE
        string nome
        string email
        string telefone
        uuid default_address_id FK
        json metadata_extra
    }
    CLIENTES_ENDERECOS {
        uuid id PK
        uuid tenant_id FK
        uuid cliente_id FK
        string apelido
        string linha1
        string cidade
        string codigo_postal
        string pais
        numeric latitude
        numeric longitude
        bool ativo
    }
    CART_ITEMS {
        uuid id PK
        uuid tenant_id FK
        uuid cliente_id FK
        string tipo
        uuid ref_id
        int quantidade
        numeric preco_unitario
    }
    PEDIDOS {
        uuid id PK
        uuid tenant_id FK
        uuid cliente_id FK
        numeric subtotal
        numeric total
        enum status
        enum origem
        json endereco_entrega_snapshot
        string metodo_pagamento
        string estado_pagamento
    }
    ITENS_PEDIDO {
        uuid id PK
        uuid tenant_id FK
        uuid pedido_id FK
        string tipo
        uuid ref_id
        int quantidade
        numeric preco_unitario
        string nome_snapshot
        uuid merchant_id FK
        uuid prestador_id FK
        numeric total_linha
    }
    AGENDAMENTOS {
        uuid id PK
        uuid tenant_id FK
        uuid cliente_id FK
        uuid prestador_id FK
        uuid servico_id FK
        datetime data_hora
        enum status
        json metadados_formulario
        json endereco_atendimento
        enum canal
    }

    TENANTS ||--o{ USERS : "tenant_id"
    TENANTS ||--o{ MERCHANTS : "tenant_id"
    TENANTS ||--o{ CATEGORIAS : "tenant_id"
    TENANTS ||--o{ PRODUTOS : "tenant_id"
    TENANTS ||--o{ PRESTADORES : "tenant_id"
    TENANTS ||--o{ SERVICOS : "tenant_id"
    TENANTS ||--o{ CLIENTES : "tenant_id"
    TENANTS ||--o{ CLIENTES_ENDERECOS : "tenant_id"
    TENANTS ||--o{ CART_ITEMS : "tenant_id"
    TENANTS ||--o{ PEDIDOS : "tenant_id"
    TENANTS ||--o{ ITENS_PEDIDO : "tenant_id"
    TENANTS ||--o{ AGENDAMENTOS : "tenant_id"

    USERS ||--o| CLIENTES : "user_id"
    USERS ||--o{ MERCHANTS : "owner_id"
    USERS ||--o| PRESTADORES : "user_id"

    MERCHANTS ||--o{ CATEGORIAS : "merchant_id"
    MERCHANTS ||--o{ PRODUTOS : "merchant_id"
    MERCHANTS ||--o{ ITENS_PEDIDO : "merchant_id"

    CATEGORIAS ||--o{ PRODUTOS : "categoria_id"
    CATEGORIAS ||--o{ SERVICOS : "categoria_id"

    PRESTADORES ||--o{ SERVICOS : "prestador_id"
    PRESTADORES ||--o{ ITENS_PEDIDO : "prestador_id"
    PRESTADORES ||--o{ AGENDAMENTOS : "prestador_id"

    SERVICOS ||--o{ AGENDAMENTOS : "servico_id"

    CLIENTES ||--o{ CLIENTES_ENDERECOS : "cliente_id"
    CLIENTES ||--o{ CART_ITEMS : "cliente_id"
    CLIENTES ||--o{ PEDIDOS : "cliente_id"
    CLIENTES ||--o{ AGENDAMENTOS : "cliente_id"

    CLIENTES ||--|| CLIENTES_ENDERECOS : "default_address_id"

    PEDIDOS ||--o{ ITENS_PEDIDO : "pedido_id"
```

## Notas sobre entidades e relacionamentos
- **Tenant** é a raiz de isolamento lógico. Todas as tabelas aplicacionais apontam para `tenants.id`, garantindo multi-tenancy rígido.
- **User** (IAM) mantém um vínculo 1:1 opcional com `Cliente` e `PrestadorServico` e 1:N com `Merchant` via `owner_id`.
- **Merchant** agrega catálogo próprio (categorias + produtos), aceitando simultaneamente produtos físicos e serviços.
- **PrestadorServico** representa profissionais e centraliza disponibilidade/serviços individuais, mantendo vínculo opcional com `User`.
- **Cliente** agrega perfis, endereços e carrinho; `ClienteEndereco` utiliza cascade e `default_address_id` para apontar o endereço preferido.
- **Pedido** agrega `ItemPedido` com snapshots do produto/serviço no momento do checkout, permitindo referências opcionais a `Merchant` e `Prestador`.
- **Agendamento** conecta cliente, prestador e serviço e mantém payloads JSON para formulários e endereço dinâmico.
- **CartItem** usa `ref_id` para apontar dinamicamente para `Produto` ou `Servico`; a diferenciação ocorre via `tipo`.

## Agregados, entidades raiz e bounded contexts
| Bounded Context (Blueprint) | Agregado / Root | Entidades internas | Observações DDD |
| --- | --- | --- | --- |
| Tenancy Management | `Tenant` | Configurações (`config_checkout`, `branding`) como value objects | Raiz publica eventos de provisionamento/desativação para outros módulos. |
| Identity & Access | `UserAccount (User)`, `Role` | `Role` atua como entidade de referência com `permissions` | `User` guarda `tenant_id`, garantindo IAM multi-tenant conforme blueprint §4.1. |
| Service Providers Catalog | `MerchantCatalog (Merchant)` | `Categoria`, `Produto` | `Merchant` consolida catálogo e endereço; invariantes (slug único por tenant) residem aqui. |
| Service Providers Catalog | `PrestadorServico` | `Servico` | Prestador é root; serviços seguem ciclo de vida do prestador. |
| Customer Profiles | `Cliente` | `ClienteEndereco` | Endereços fazem parte do agregado via cascade; `default_address_id` é invariante interno. |
| Checkout & Payments | `Pedido` | `ItemPedido` | Responsável por snapshots, status e pagamentos; `CartItem` representa agregado `Cart` separado por cliente. |
| Scheduling & Fulfillment | `Agendamento` | – | Controla fluxo de confirmação/cancelamento e referencia outros agregados por ID. |
| Shared Kernel | `TenantScopedEntity` (MappedSuperclass) | – | Proverá enforcement automático de `tenant_id` e filtros em Spring Modulith. |

## Value Objects e estruturas `@Embeddable`
- **Money** (`preco`, `subtotal`, `total`, `preco_unitario`, `preco_base`, etc.) – encapsular moeda/precisão; evita repetir `BigDecimal`.
- **GeoPoint** (`latitude`/`longitude` presentes em `Merchant`, `ClienteEndereco`, `Prestador`). Pode ser `@Embeddable`.
- **AddressSnapshot** – usado em `Pedido.endereco_entrega_snapshot` e `Agendamento.endereco_atendimento`; pode reutilizar a estrutura de `ClienteEndereco`.
- **BusinessHours** – modela `Merchant.horario_funcionamento`.
- **CheckoutConfig / BrandingTheme** – encapsular JSON de `Tenant`.
- **ServiceArea** – deriva de `PrestadorServico.zona_atendimento`.
- **ProductAttributes / MediaGallery / TagList** – para arrays JSON de `Produto` e `Servico`.
- **PermissionSet** – mapea `Role.permissions`.
- **FormResponse / MetadataExtra** – `Agendamento.metadados_formulario` e `Cliente.metadata_extra`.
- **CartLineSnapshot** – para `CartItem` e `ItemPedido` snapshots (nome, ref_id, total_linha).

## Colunas JSON / Snapshot e estratégia JPA
| Tabela.Coluna | Descrição | Value Object / Conversor sugerido | Observações |
| --- | --- | --- | --- |
| `tenants.config_checkout` | Configurações de checkout por tenant | `CheckoutConfig` + `@Convert(Jsonb)` | Necessário carregar limites, métodos de pagamento e textos específicos. |
| `tenants.branding` | Paleta/logo customizados | `BrandingTheme` | Usado por front-ends multi-tenant. |
| `roles.permissions` | Mapa de permissões dinâmicas | `PermissionSet` | Permite adicionar políticas sem alterar schema. |
| `merchants.horario_funcionamento` | Agenda semanal | `BusinessHours` | Conversor garante validação (dia/hora). |
| `produtos.imagens`, `servicos.imagens` | Galeria de URLs | `List<ImageRef>` | Pode usar `@JdbcTypeCode(SqlTypes.JSON)` com Hibernate 6. |
| `produtos.atributos_extras` | Key-value customizado | `ProductAttributes` | Útil para filtros dinâmicos no catálogo. |
| `prestadores.profissoes`, `prestadores.linguas` | Listas desnormalizadas | `List<String>` via conversor JSON | Manter ordenação/idiomas. |
| `prestadores.zona_atendimento` | Polígono/raio de atendimento | `ServiceArea` | Validar formato (geojson vs bounding box). |
| `clientes.metadata_extra` | Preferências do cliente | `CustomerMetadata` | Permite guardar consentimentos e tags. |
| `pedidos.endereco_entrega_snapshot` | Endereço usado no checkout | `AddressSnapshot` | Congela dados mesmo se cliente alterar o endereço original. |
| `item_pedido.nome_snapshot` (string) & `categoria_id_snapshot` | Snapshot textual/IDs | Considerar `ItemSnapshot` VO que inclua nome/categoria | Mantém rastreabilidade. |
| `agendamentos.metadados_formulario` | Respostas do formulário de serviço | `FormResponse` | Estrutura varia por serviço. |
| `agendamentos.endereco_atendimento` | Local de execução pontual | `AddressSnapshot` | Pode ser domicilio do cliente ou endereço temporário. |
| `cart_items` (sem JSON) | – | – | Persistem apenas referências; sem conversor. |

## Validação com o Blueprint (Issue #1)
- Cada agregado foi mapeado para o módulo correspondente (vide tabela acima), cumprindo os boundaries definidos na secção 3 do blueprint.
- O ERD evidencia dependências entre módulos, mantendo apenas referências por ID entre agregados distintos, conforme orientação de DDD e arquitetura hexagonal.
- O padrão `TenantScopedMixin` será traduzido para um `@MappedSuperclass` compartilhado no Spring Modulith Shared Kernel, garantindo isolamento citado no blueprint §2 e §4.
- JSON/value objects destacados acima permitem implementar policies e configurações multi-tenant sem quebrar o schema, alinhado às necessidades de customização mencionadas no blueprint (catálogo flexível, checkout configurável, scheduling rico).

Este documento passa a ser a base para gerar entidades JPA e migrations com Flyway. Eventuais ajustes futuros (novas colunas/relacionamentos) devem manter o diagrama e as tabelas atualizadas, preservando alinhamento com o blueprint arquitetural.

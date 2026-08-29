# AutoBots — automanager

Micro-serviço para gestão de clientes de lojas especializadas em manutenção veicular e venda de autopeças. Cadastro completo de clientes, documentos, endereços e telefones, com API REST pronta para integração com outros sistemas.

## Sobre o projeto

O **AutoBots** nasceu para resolver um problema simples e recorrente no setor automotivo: agilizar a gestão de lojas de manutenção veicular. A API expõe operações completas de CRUD para as entidades centrais do domínio, com uma arquitetura pensada para crescer — separação clara entre entidades, regras de negócio e camada de exposição HTTP.

### Funcionalidades

- **Clientes** — cadastro, consulta, atualização e remoção
- **Documentos** — CPF, RG e demais documentos vinculados a cada cliente
- **Endereços** — um endereço por cliente, com dados completos de localização
- **Telefones** — múltiplos telefones por cliente

## Stack

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 26 |
| Framework | Spring Boot 4.1 |
| Persistência | Spring Data JPA / Hibernate |
| Banco de dados | H2 (em memória) |
| Build | Maven |

## Arquitetura

O projeto segue uma separação de responsabilidades inspirada em princípios SOLID:

```
com.autobots.automanager
├── entidades/      → Entidades JPA (Cliente, Documento, Endereco, Telefone)
├── repositorios/    → Interfaces Spring Data JPA
├── modelo/          → Regras de negócio (seleção e atualização de entidades)
└── controles/       → Controllers REST
```

Cada entidade possui um `Selecionador` (busca por id em coleções) e um `Atualizador` (aplica atualizações campo a campo), mantendo os controllers enxutos e a lógica de negócio isolada e testável.

## Como rodar o projeto

### Pré-requisitos

- **JDK 26** instalado
- Maven Wrapper incluso no projeto (não é necessário instalar Maven)

### Configurando o JAVA_HOME (Windows / PowerShell)

Se você tem mais de uma versão do Java instalada, aponte o terminal para o JDK 26 antes de rodar o projeto:

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-26.0.2.1"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

> Ajuste o caminho acima para o local onde o JDK 25 está instalado na sua máquina. Para não repetir esse passo a cada terminal novo, defina `JAVA_HOME` como variável de ambiente permanente do sistema (Painel de Controle → Sistema → Variáveis de Ambiente).

### Executando

```bash
cd automanager
./mvnw spring-boot:run
```

No Windows, use `.\mvnw.cmd spring-boot:run`.

A aplicação sobe em `http://localhost:8080`.

### Console do banco H2

Com a aplicação rodando, acesse `http://localhost:8080/h2-console`:

- **JDBC URL:** `jdbc:h2:mem:automanager`
- **Usuário:** `sa`
- **Senha:** *(em branco)*

## Endpoints da API

### Cliente

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/cliente/lista` | Lista todos os clientes |
| `GET` | `/cliente/{id}` | Busca um cliente por id |
| `POST` | `/cliente/cadastro` | Cadastra um novo cliente |
| `PUT` | `/cliente/atualizar` | Atualiza um cliente existente |
| `DELETE` | `/cliente/excluir/{id}` | Remove um cliente |

### Documento, Endereço e Telefone

Seguem o mesmo padrão, aninhados sob o cliente:

| Método | Rota |
|---|---|
| `GET` | `/cliente/{clienteId}/documento/lista` |
| `GET` | `/cliente/{clienteId}/documento/{id}` |
| `POST` | `/cliente/{clienteId}/documento/cadastro` |
| `PUT` | `/cliente/{clienteId}/documento/atualizar` |
| `DELETE` | `/cliente/{clienteId}/documento/excluir/{id}` |

O mesmo padrão vale para `/telefone`. Como cada cliente possui um único endereço, `/cliente/{clienteId}/endereco` não usa id na rota:

| Método | Rota |
|---|---|
| `GET` | `/cliente/{clienteId}/endereco` |
| `POST` | `/cliente/{clienteId}/endereco/cadastro` |
| `PUT` | `/cliente/{clienteId}/endereco/atualizar` |
| `DELETE` | `/cliente/{clienteId}/endereco/excluir` |

## Licença

Este projeto foi desenvolvido para fins acadêmicos.

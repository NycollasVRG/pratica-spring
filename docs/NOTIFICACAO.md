# Documentação do Domínio: Notificação

## 1. Visão Geral
Esta documentação descreve a modelagem e a estrutura das classes responsáveis por representar as **Notificações** de agravos e doenças no sistema. A arquitetura foi pensada para manter a organização e manutenção no código Java (Orientação a Objetos), ao mesmo tempo em que otimiza o armazenamento no Banco de Dados.

## 2. Decisão Arquitetural: Tabela Única (Flat Table)
O formulário de notificação possui uma quantidade muito grande de campos. Dividir esses campos em múltiplas tabelas SQL exigiria muitos relacionamentos (JOINs) e poderia impactar o desempenho de exportação/importação (arquivos CSV, integrações com o DataSUS/SINAN).

Por isso, utilizamos o padrão de composição do JPA (`@Embeddable` e `@Embedded`).
* **No Java:** Os dados estão devidamente separados por contexto em diferentes classes.
* **No Banco de Dados:** O Hibernate unifica os campos de todas essas classes em apenas uma grande tabela chamada `notificacao`.

## 3. Estrutura das Classes

### Entidade Principal
* `Notificacao.java` (`@Entity`): É a raiz do nosso domínio. Ela representa a tabela `notificacao` e detém a chave primária (`numeroNotificacao`), além de informações primárias sobre o atendimento (tipo da notificação, agravo, unidade de saúde, data de preenchimento).

### Classes Embutidas (`@Embeddable`)
Para evitar uma "God Class" (Classe Deus com dezenas de propriedades) em `Notificacao.java`, separamos os atributos logicamente:

1. **`Paciente.java`**:
   Agrupa informações pessoais do indivíduo afetado.
   * *Campos:* nome, data de nascimento, idade, escolaridade, número do cartão SUS, nome da mãe e contatos.
   * *Composição:* Contém internamente um objeto de `Endereco`.

2. **`Endereco.java`**:
   Agrupa todos os campos geográficos e de localização.
   * *Campos:* UF, município, distrito, bairro, logradouro, número, complemento, campos de geolocalização, ponto de referência e CEP.

3. **`Investigacao.java`**:
   Agrupa as informações clínicas e epidemiológicas que geralmente são preenchidas durante ou no encerramento da investigação do caso.
   * *Campos:* data da investigação, classificação final, informações de local de infecção (autoctonia), evolução do caso (cura, óbito), e datas de desfecho.

## 4. Tipos Enumerados (Enums)
Para garantir a integridade da informação e aplicar regras de negócio fortes, os campos que aceitam apenas valores limitados (ex: "M" ou "F") foram substituídos por Enums em vez de `String` ou `Integer` puros.

* **`TipoNotificacao`**: 1-Negativa, 2-Individual, 3-Surto, 4-Tracoma.
* **`Sexo`**: MASCULINO ('M'), FEMININO ('F'), IGNORADO ('I').
* **`RacaCor`**: 1-Branca, 2-Preta, 3-Amarela, 4-Parda, 5-Indígena.
* **`ZonaResidencia`**: 1-Urbana, 2-Rural, 3-Periurbana, 9-Ignorado.
* **`EvolucaoCaso`**: 1-Cura, 2-Óbito pelo agravo, 3-Óbito por outras causas, 9-Ignorado.
* **`CriterioConfirmacao`**: 1-Laboratorial, 2-Clínico-Epidemiológico.
* **`SimNaoIgnorado`**: Enum padronizado (1-Sim, 2-Não, 3-Indeterminado) utilizado em vários atributos, como *doencaRelacionadaTrabalho* e *autoctoneMunicipioResidencia*.

## 5. Como Utilizar
Para criar e salvar uma nova notificação no código, a estrutura fluída ficaria semelhante a isto:

```java
Notificacao novaNotificacao = new Notificacao();
novaNotificacao.setNumeroNotificacao("123456");
novaNotificacao.setTipoNotificacao(TipoNotificacao.INDIVIDUAL);

Endereco endereco = new Endereco();
endereco.setUfResidencia("SP");
endereco.setMunicipioResidencia("São Paulo");

Paciente paciente = new Paciente();
paciente.setNomePaciente("João da Silva");
paciente.setSexo(Sexo.MASCULINO);
paciente.setEndereco(endereco);

novaNotificacao.setPaciente(paciente);

// O repositório irá salvar tudo na mesma linha da tabela "notificacao"
notificacaoRepository.save(novaNotificacao);
```

# Auditoria de Software - ISO/IEC 25010 🔍📊

Esta é uma ferramenta de auditoria de software automatizada desenvolvida em Java com Spring Boot. O sistema tem como objetivo avaliar repositórios de código-fonte (especialmente projetos Java/Spring Boot) aplicando métricas de qualidade de software baseadas na norma **ISO/IEC 25010**.

A API recebe a URL de um repositório Git, clona o projeto em um diretório temporário, executa uma bateria de análises e devolve um diagnóstico detalhado que pode ser visualizado em formato JSON ou exportado como um relatório PDF.

## 🚀 Funcionalidades

O sistema avalia a qualidade do software através de três módulos principais:

* **Módulo I: Análise de Manutenibilidade (Estática)**
    * Avalia o código sem executá-lo usando o *JavaParser*.
    * Mede a **Complexidade Ciclomática** (limite de aprovação: <= 150).
    * Mede o **Acoplamento de Objetos (CBO)** (limite de aprovação: <= 100).
    * Identifica **Duplicação de Código (DRY)** analisando blocos repetidos.

* **Módulo II: Eficiência de Desempenho (Dinâmica)**
    * Inicia a aplicação alvo localmente para benchmarking.
    * Mede o **Tempo de Inicialização** (falha se exceder 20 segundos).
    * Verifica a eficiência de recursos e se a aplicação sobe corretamente dentro da janela de latência esperada.

* **Módulo III: Confiabilidade (Testabilidade)**
    * Utiliza o *Maven Invoker* para rodar programaticamente a suíte de testes do projeto alvo (`mvn test`).
    * Verifica se o build dos testes obteve sucesso.
    * Calcula a proporção de arquivos de teste em relação aos arquivos de produção (cobertura mínima exigida: 20%).

* **📄 Geração de Relatórios (PDF)**
    * Gera dinamicamente um laudo de auditoria em PDF utilizando o *OpenPDF*, contendo o sumário executivo, status de aprovação de cada módulo e os valores obtidos nas métricas.

## 🛠️ Tecnologias Utilizadas

* **Java 17**
* **Spring Boot 3.2.5** (Web, Validation)
* **Eclipse JGit** (Clonagem e manipulação de repositórios Git)
* **JavaParser** (Análise de Árvore Sintática e métricas estáticas)
* **Maven Invoker** (Execução de goals do Maven programaticamente)
* **OpenPDF** (Geração dos relatórios de auditoria em PDF)
* **Lombok** (Redução de boilerplate)

## ⚙️ Pré-requisitos

Para rodar o projeto localmente, você precisará de:
* [JDK 17](https://jdk.java.net/17/) instalado.
* [Git](https://git-scm.com/) instalado no ambiente onde a API vai rodar.
* [Maven](https://maven.apache.org/) instalado e configurado nas variáveis de ambiente (necessário para a análise dinâmica e de testes funcionarem nos projetos alvo).

## 🏃‍♂️ Como Executar o Projeto

1. Clone este repositório:
   ```bash
   git clone https://github.com/DouglasBohmer/Auditoria-ISO25010

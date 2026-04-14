<h1 align="center">📦 Order Service</h1>

<p align="center">
  Microserviço responsável pelo gerenciamento de pedidos em uma arquitetura distribuída
</p>

<hr/>

<h2>🧩 Visão Geral</h2>
<p>
O <b>Order Service</b> é um microserviço responsável por gerenciar pedidos dentro de uma arquitetura distribuída,
realizando criação, validação, persistência e controle de status dos pedidos.
</p>

<h2>🎯 Objetivo</h2>
<ul>
  <li>Criar e gerenciar pedidos</li>
  <li>Validar clientes via API externa</li>
  <li>Aplicar regras de negócio</li>
  <li>Preparar para arquitetura orientada a eventos</li>
</ul>

<h2>🏗️ Arquitetura</h2>
<ul>
  <li>🔍 Service Discovery (Eureka)</li>
  <li>🚪 API Gateway</li>
  <li>🔗 Client API (OpenFeign)</li>
  <li>🐘 PostgreSQL</li>
</ul>

<h2>⚙️ Tecnologias</h2>
<p>
Java 17 • Spring Boot • Spring Data JPA • Spring Cloud • PostgreSQL • Docker • Maven
</p>

<hr/>

<h2>📁 Estrutura do Projeto</h2>

<pre>
order-service
├── controller
├── service
├── repository
├── entity
├── enums
├── dto
├── client
├── exception
</pre>

<hr/>

<h2>🔄 Fluxo de Criação de Pedido</h2>

<pre>
Client → API Gateway → Order Service → Client API → Banco de Dados
</pre>

<ol>
  <li>Requisição chega pelo Gateway</li>
  <li>Order Service valida os dados</li>
  <li>Consulta Client API</li>
  <li>Valida cliente</li>
  <li>Salva no banco</li>
  <li>Retorna resposta</li>
</ol>

<hr/>

<h2>📌 Endpoints</h2>

<h3>➕ Criar Pedido</h3>
<pre>
POST /api/orders
</pre>

<b>Request</b>
<pre>
{
  "clientId": 1,
  "description": "Compra de insumos",
  "amount": 150.00
}
</pre>

<b>Response</b>
<pre>
{
  "id": 10,
  "clientId": 1,
  "description": "Compra de insumos",
  "amount": 150.00,
  "status": "CREATED"
}
</pre>

<h3>🔍 Buscar Pedido</h3>
<pre>GET /api/orders/{id}</pre>

<h3>📋 Listar Pedidos</h3>
<pre>GET /api/orders?page=0&size=10</pre>

<h3>🔄 Atualizar Status</h3>
<pre>PATCH /api/orders/{id}/status?status=PROCESSING</pre>

<hr/>

<h2>⚠️ Regras de Negócio</h2>
<ul>
  <li>Cliente deve existir</li>
  <li>Cliente deve estar ativo</li>
  <li>Valor deve ser maior que zero</li>
  <li>Status inicial: <b>CREATED</b></li>
</ul>

<h3>🔄 Fluxo de Status</h3>
<pre>
CREATED → PROCESSING → COMPLETED
</pre>

<ul>
  <li>Não permite alterar pedidos finalizados</li>
</ul>

<hr/>

<h2>🚨 Tratamento de Erros</h2>

<table border="1" cellpadding="6">
<tr>
<th>Situação</th>
<th>Status</th>
</tr>
<tr>
<td>Cliente não encontrado</td>
<td>404</td>
</tr>
<tr>
<td>Cliente inativo</td>
<td>400</td>
</tr>
<tr>
<td>Dados inválidos</td>
<td>400</td>
</tr>
<tr>
<td>Erro interno</td>
<td>500</td>
</tr>
</table>

<p><b>Classes utilizadas:</b></p>
<ul>
  <li>BusinessException</li>
  <li>ResourceNotFoundException</li>
  <li>GlobalExceptionHandler</li>
</ul>

<hr/>

<h2>📊 Paginação</h2>

<pre>
GET /api/orders?page=0&size=5&sort=createdAt,desc
</pre>

<p>Retorno inclui:</p>
<ul>
  <li>content</li>
  <li>totalElements</li>
  <li>totalPages</li>
  <li>page</li>
</ul>

<hr/>

<h2>🧪 Testes</h2>

<ul>
  <li>✅ Criar pedido válido</li>
  <li>❌ Cliente inexistente</li>
  <li>❌ Cliente inativo</li>
  <li>🔁 Atualizar status</li>
  <li>📄 Paginação</li>
</ul>

<p><b>Ferramentas:</b> Postman, cURL</p>

<hr/>

<h2>🔗 Integração</h2>

<pre>
GET /clients/{id}
</pre>

<ul>
  <li>Validação de cliente</li>
  <li>Status ativo/inativo</li>
</ul>

<hr/>

<h2>🚀 Execução</h2>

<h3>🐳 Docker</h3>
<pre>docker-compose up --build</pre>

<h3>💻 Local</h3>
<pre>
mvn clean package
mvn spring-boot:run
</pre>

<hr/>

<h2>📡 Service Discovery</h2>
<pre>http://localhost:8761</pre>

<hr/>

<h2>📈 Melhorias Futuras</h2>
<ul>
  <li>Resilience4j</li>
  <li>Retry e Timeout</li>
  <li>RabbitMQ</li>
  <li>Actuator + métricas</li>
  <li>Testes automatizados</li>
</ul>

<hr/>

<h2>🧠 Destaques Técnicos</h2>
<ul>
  <li>Arquitetura de microserviços</li>
  <li>OpenFeign</li>
  <li>Eureka</li>
  <li>Paginação</li>
  <li>Regras de negócio</li>
  <li>Logs estruturados</li>
</ul>

<hr/>

<h2>👨‍💻 Autor</h2>
<p><b>Jonathan Leite</b></p>

<hr/>

<p align="center">
  🚀 Projeto desenvolvido para demonstrar arquitetura moderna de microserviços
</p>
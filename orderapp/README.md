# Order Management REST API

This is an order and customer management application built on **Spring Boot**.
The project uses **Spring REST API** to create, view, and edit customers and orders,
and **Hibernate** (Spring Data JPA) to work with the database.

## Technologies

- **Spring Boot** for creating REST API
- **Spring Data JPA** for working with relational database (PostgreSQL)
- **PostgreSQL** and **H2** as relational database(H2 for integration tests)
- **Docker** and **Docker Compose** for containerization and easy launch

## Description

The program allows you to perform the following operations:

1. **Customer Management**:
    - Create, view, edit customers
    - Search for customers by name, email, address, and range min and max profit.
    - Deactivate customer status
    - View all orders of a customer as a supplier or consumer with calculated the total profit of a customer on all his orders

2. **Order Management**:
    - Create, view orders
    - Check business logic: prohibit creating an order with 0 or negative price, prohibit creating an order for an inactive customer, limit on the total profit of the customer

3. **Delay Emulation**:
    - When creating an order, a random delay of 1 to 10 seconds is added before saving to the database

## API Endpoints

### Clients

- **GET /client** - get a list of all clients
- **GET /client/{id}** - get information about a client by ID with orders as a supplier or consumer and total profit
- **POST /client** - create a new client
- **PUT /client/{id}** - update information about a client
- **PATCH /client/{id}/deactivate** - mark a client as inactive (do not delete)
- **GET /client/search** - search for clients by keywords (name, email, phoneNumber, minProfit, maxProfit, etc.)
- **GET /client/reset-profits** - resets the profit of all clients

### Orders

- **GET /order** - get a list of all orders
- **GET /order/{id}** - get order details by ID
- **POST /order** - create a new order

## Installation and run with Docker Compose

### Step 1: Cloning the repository:

```bash
git clone https://github.com/KostiantynVorobiov/order-app.git
cd orderapp
```

### Step 2: Run the application and database using Docker Compose:
```bash
docker-compose up
```

### Step 3: Access the API
```bash
http://localhost:8080
```

## Installation and run Locally

### Step 1: Cloning the repository:

```bash
git clone https://github.com/yourusername/order-management.git
cd order-management
```

### Step 2: Build the project
```bash
# Для Linux та macOS:
./mvnw clean install

# Для Windows:
mvnw.cmd clean install
```

### Step 3: Run the application
```bash
# Для Linux та macOS:
./mvnw spring-boot:run

# Для Windows:
mvnw.cmd spring-boot:run
```

### Step 4: Access the API
```bash
http://localhost:8080
```

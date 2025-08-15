# Idempotency and Concurrency Testing Cases

This is an automated test suite designed to verify idempotence and correct handling of concurrent access in a RESTful API.

## Technologies

- **Java Spring Boot** to create a client that interacts with an external API
- **RestTemplate** for making HTTP requests
- **ExecutorService** for simulating simultaneous user actions
- **Apache HttpClient** to support all HTTP methods, including PATCH

## Description

This project contains test scripts to verify the behavior of the API under the following conditions:

1. **Idempotence test**:
    - Checking that when sending multiple identical requests to create a single order, the API should handle repetitions correctly.

2. **Price reduction test**:
    - Checking how the API behaves when orders have a discounted price and how this affects business logic (e.g. profit limits).

3. **Race condition test**:
    - Testing how the system handles simultaneous requests to create an order and deactivate a customer.

## Project structure

The project consists of a **Java Spring Boot** application that is a client for an external API. **RestTemplate** is used 
to process HTTP requests, and **ExecutorService** is used to simulate concurrent user requests.

# How to Run the Tests
- To run these tests, you must have your main API application running on http://localhost:8081.
- To trigger a specific scenario, send a GET request to the corresponding endpoint in this test application.

## Test scenarios
The suite includes three main scenarios, each designed to test a specific aspect of the API's robustness.

### Add: Add ten random clients

**Goal**: Add test clients for testing.

**How it works**:
- The test sends 10 requests to create clients.

**Expected Result**:
- All 10 should be created and save in DB.

**How to Run**: Send a GET request to: http://localhost:8081/test/add-clients

**Example Result**:
```
Client with 1 created.
Client with 2 created..
...
Client with 10 created.
```

---

### Scenario 1: N+1 similar orders

**Goal**: Check the idempotency of the API. The system should create a single order for a series of similar requests,
discarding further duplicates using the business key `idempotencyId`.

**How it works**:
- The test sends 10 requests to create one order.
- All requests use the same `idempotencyId`.

**Expected Result**:
- The first request completes successfully with a 200 OK.
- The next 9 requests should be rejected with a error, indicating that the order already exists.

**How to Run**: Send a GET request to: http://localhost:8081/scenario-1

**Example Result**:
```
Attempt 1: SUCCESS! Order created.
Attempt 2: ERROR: This order is already being processed..
...
Attempt 10: ERROR: This order is already being processed..
```

---

### Scenario 2: N+1 orders with reduced prices

**Goal**: Check idempotence together with a specific business rule: the total customer profit cannot be lower than a certain limit (e.g. -1000).

**How it works**:
- The test sends 10 similar orders, each with a slightly lower price than the previous one.
- The client has an initial profit that is close to the limit (e.g. -970).

**Expected result**:
- The first valid order (the one that doesn't violate the profit limit) should be created successfully.
- All subsequent requests will be rejected due to idempotency, even if they would also be valid on their own.

**How to Run**: Send a GET request to: http://localhost:8081/scenario-2

**Example Result**:
```
Attempt 1 (price 100.0): ERROR: Profit limit exceeded..
Attempt 2 (price 90.0): ERROR: Profit limit exceeded.
...
Attempt 6 (price 30,00): SUCCESS! Order created.
...
Attempt 10 (price 10.0): ERROR: Order already exists with this business key.
```

---

### Scenario 3: Order creation and parallel deactivation (Race Condition)

**Goal**: Check how the system handles a race condition. A request to deactivate a client is sent in multiple simultaneous order creation requests.

**How it works**:
- The test uses **ExecutorService** to simultaneously send 5 order creation requests and 1 client deactivation request.
- The order creation logic in the API has a simulated delay to increase the chance of a race condition.

**Expected result**:
- Successfully created orders: orders that were fully processed before the client was deactivated.
- Error: orders that were initiated after the client was deactivated.
- Error: orders that started processing while the client was active but were stopped during processing due to deactivation.

**How to Run**: Send a GET request to: http://localhost:8081/scenario-3

**Example Result**:
```
Order 4: SUCCESS! Order created.
Order 2: SUCCESS! Order created.
Client with ID 1 was successfully deactivated.
Order 5: ERROR: Consumer became inactive during processing.
Order 1: ERROR: Consumer became inactive during processing.
Order 3: ERROR: Consumer became inactive during processing.
```

This script tests the API's ability to correctly handle asynchronous events and maintain data consistency.


# Kit Java Backend Library

[![CI](https://github.com/suprim-corp/kit-backend-base-java/actions/workflows/ci.yml/badge.svg)](https://github.com/suprim-corp/kit-backend-base-java/actions/workflows/ci.yml)
[![](https://jitpack.io/v/suprim-corp/kit-backend-base-java.svg)](https://jitpack.io/#suprim-corp/kit-backend-base-java)
[![codecov](https://codecov.io/gh/suprim-corp/kit-backend-base-java/branch/main/graph/badge.svg)](https://codecov.io/gh/suprim-corp/kit-backend-base-java)
![Java](https://img.shields.io/badge/Java-17+-orange?logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-3.9+-blue?logo=apachemaven)
![License](https://img.shields.io/badge/License-MIT-green)

Reusable backend utilities for Java applications.

## Modules

| Module             | Description                                        | Dependencies                    |
|--------------------|----------------------------------------------------|---------------------------------|
| `kit-java-core`      | UUID generation, string utilities, data validation | java-uuid-generator             |
| `kit-java-json`      | Jackson-based JSON utilities                       | kit-java-core, jackson            |
| `kit-java-exception` | Exception framework with error codes               | kit-java-core                     |
| `kit-java-crypto`    | AES-256-GCM encryption, HKDF key derivation        | (none)                          |
| `kit-java-web`       | HTTP utilities, responses, trace propagation       | kit-java-core, kit-java-exception   |
| `kit-java-grpc`      | gRPC utilities, exception mapping, trace propagation | kit-java-exception, grpc, protobuf|

## Installation

Add the JitPack repository and dependencies to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Then add the modules you need:

```xml
<!-- Core utilities -->
<dependency>
    <groupId>dev.suprim</groupId>
    <artifactId>kit-java-core</artifactId>
    <version>1.1.0</version>
</dependency>

<!-- JSON utilities -->
<dependency>
    <groupId>dev.suprim</groupId>
    <artifactId>kit-java-json</artifactId>
    <version>1.1.0</version>
</dependency>

<!-- Exception framework -->
<dependency>
    <groupId>dev.suprim</groupId>
    <artifactId>kit-java-exception</artifactId>
    <version>1.1.0</version>
</dependency>

<!-- Crypto utilities -->
<dependency>
    <groupId>dev.suprim</groupId>
    <artifactId>kit-java-crypto</artifactId>
    <version>1.1.0</version>
</dependency>

<!-- Web utilities -->
<dependency>
    <groupId>dev.suprim</groupId>
    <artifactId>kit-java-web</artifactId>
    <version>1.1.0</version>
</dependency>

<!-- gRPC utilities -->
<dependency>
    <groupId>dev.suprim</groupId>
    <artifactId>kit-java-grpc</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Quick Start

### UUID Generation

```java
import dev.suprim.kit.core.UUIDUtils;

// Random UUID v4
UUID id = UUIDUtils.v4();

// Time-ordered UUID v7 (better for database indexes)
UUID timeId = UUIDUtils.v7();
```

### JSON Utilities

```java
import dev.suprim.kit.json.JsonUtils;

// Serialize to JSON
String json = JsonUtils.toJsonString(object);

// Deserialize from JSON
MyClass obj = JsonUtils.fromJson(json, MyClass.class);

// Convert to/from Map
Map<String, Object> map = JsonUtils.toMap(object);
MyClass obj = JsonUtils.fromMap(map, MyClass.class);
```

### Exception Handling

```java
import dev.suprim.kit.exception.*;

// ApiException (checked) - requires throws declaration
throw new ApiException(ApiStatus.NOT_FOUND);
throw new ApiException(ApiStatus.NOT_FOUND, "User not found");

// ApiRuntimeException (unchecked) - no throws required
throw new ApiRuntimeException(ApiStatus.UNAUTHORIZED);

// Static factory methods
throw ApiException.notFound("User not found");
throw ApiException.badRequest("Invalid email format");
throw ApiException.unauthorized("Token expired");

// With data payload
throw ApiException.validationError("Validation failed", errors);

// Builder pattern
throw ApiException.builder()
    .errorCode(ApiStatus.FORBIDDEN)
    .message("Access denied")
    .data(details)
    .build();

// Convert checked to unchecked
throw apiException.toUnchecked();
```

#### ApiStatus Codes

| Code | Status          | Message                                               |
|------|-----------------|-------------------------------------------------------|
| 1    | SUCCESS         | Success!                                              |
| 99   | SERVER_ERROR    | The server is being upgraded, please try again later! |
| 400  | INVALID_REQUEST | Invalid request!                                      |
| 401  | UNAUTHORIZED    | Unauthorized!                                         |
| 403  | FORBIDDEN       | Forbidden!                                            |
| 404  | NOT_FOUND       | Not found!                                            |
| 409  | CONFLICT        | Conflict!                                             |
| 503  | NOT_CONFIGURED  | Service not configured!                               |

All responses return HTTP 200, with the status code in the response body.

### Crypto Utilities

```java
import dev.suprim.kit.crypto.SecretUtils;

// Create from environment key
SecretUtils crypto = SecretUtils.fromBase64Key(System.getenv("MASTER_KEY"));

// Encrypt with per-secret key derivation
UUID secretId = UUID.randomUUID();
byte[] plaintext = "sensitive data".getBytes();
SecretUtils.EncryptedSecret encrypted = crypto.encrypt(plaintext, secretId, null);

// Decrypt
byte[] decrypted = crypto.decrypt(encrypted, secretId, null);

// With AAD (Additional Authenticated Data)
byte[] aad = "tenant:123".getBytes();
encrypted = crypto.encrypt(plaintext, secretId, aad);
decrypted = crypto.decrypt(encrypted, secretId, aad);

// Generate new master key
String newKey = SecretUtils.generateBase64Key();
```

### Web Utilities

```java
import dev.suprim.kit.web.*;
import dev.suprim.kit.web.response.*;

// Get client IP (handles proxy headers)
String ip = RequestUtils.getClientIpAddress(request);

// Extract Bearer token
String token = RequestUtils.extractBearerToken(request);

// Extract domain from request (X-Forwarded-Host > Origin > Referer > Host)
String domain = DomainUtils.extractDomain(request);

// HTTP constants
String contentType = HttpConstants.CONTENT_TYPE_JSON;
String authHeader = HttpConstants.HEADER_AUTHORIZATION;

// API responses
BaseResponse<Void> ok = BaseResponse.success();
BaseResponse<User> response = BaseResponse.success(user);
BaseResponse<Void> error = new BaseResponse<>(ApiStatus.NOT_FOUND, "User not found");

// Paginated responses
PaginatedResponse<Item> paginated = new PaginatedResponse<>(items, totalCount, pageable);

// Validation errors
ValidationError error = new ValidationError("email", "Invalid format");
```

### Trace Propagation (HTTP)

```java
import dev.suprim.kit.web.context.*;

// Register the filter (Spring Boot)
@Bean
FilterRegistrationBean<RequestContextFilter> requestContextFilter() {
    FilterRegistrationBean<RequestContextFilter> registration = new FilterRegistrationBean<>(new RequestContextFilter());
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
}

// Access trace/request ID anywhere in the request thread
Optional<String> traceId = RequestContext.getTraceId();
Optional<String> requestId = RequestContext.getRequestId();

// Propagate context to async tasks
ExecutorService executor = Executors.newFixedThreadPool(4);
executor.submit(ContextPropagation.wrap(() -> {
    // RequestContext and MDC are available here
    String trace = RequestContext.getTraceId().orElse("unknown");
}));
```

The filter automatically:
- Extracts `X-Trace-ID` and `X-Request-ID` from incoming headers (or generates UUID v7 if absent)
- Stores them in `RequestContext` (ThreadLocal) and SLF4J MDC (`traceId`, `requestId`)
- Sets `X-Trace-ID` and `X-Request-ID` response headers
- Cleans up after request completes

### Trace Propagation (gRPC)

```java
import dev.suprim.kit.grpc.*;

// Server-side: extract trace context from incoming metadata
Server server = ServerBuilder.forPort(9090)
    .addService(new MyServiceImpl())
    .intercept(new ContextPropagationInterceptor())
    .intercept(new ExceptionInterceptor())
    .build();

// Client-side: forward trace context to outgoing calls
ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:9090")
    .intercept(new ContextForwardingInterceptor())
    .build();

// Access context in service implementation
String traceId = GrpcContext.getTraceId();
String requestId = GrpcContext.getRequestId();
```

`ContextPropagationInterceptor` extracts `x-trace-id`, `x-request-id`, `x-user-id`, `x-tenant-id` from gRPC metadata, attaches to gRPC Context, and sets SLF4J MDC for log correlation.

`ContextForwardingInterceptor` forwards these values from the current gRPC Context to outgoing call metadata for service-to-service propagation.

### gRPC Utilities

```java
import dev.suprim.kit.grpc.*;
import dev.suprim.kit.exception.*;
import io.grpc.*;

// Map HTTP status to gRPC Status
Status grpcStatus = GrpcStatusMapper.fromHttpStatus(404); // NOT_FOUND
int httpStatus = GrpcStatusMapper.toHttpStatus(Status.PERMISSION_DENIED); // 403

// Convert exceptions to gRPC StatusRuntimeException
ApiRuntimeException apiEx = ApiRuntimeException.notFound("User not found");
StatusRuntimeException grpcEx = ExceptionInterceptor.toStatusException(apiEx);

// Add exception interceptor to server
Server server = ServerBuilder.forPort(9090)
    .addService(new MyServiceImpl())
    .intercept(new ExceptionInterceptor())
    .build();

// Work with metadata
Metadata metadata = MetadataUtils.withRequestId("req-123");
String reqId = MetadataUtils.getString(metadata, MetadataUtils.REQUEST_ID);
```

## Requirements

- Java 17+
- Maven 3.9+

## Building

```bash
mvn clean install
```

## Running Tests

```bash
mvn test
```

## License

MIT

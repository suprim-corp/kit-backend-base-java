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
| `kit-java-web`       | HTTP utilities, responses, domain extraction       | kit-java-core, kit-java-exception   |
| `kit-java-grpc`      | gRPC utilities, exception mapping, proto defs      | kit-java-exception, grpc, protobuf|

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
    <groupId>com.github.suprim-corp.kit-backend-base-java</groupId>
    <artifactId>kit-java-core</artifactId>
    <version>1.0.2</version>
</dependency>

<!-- JSON utilities -->
<dependency>
    <groupId>com.github.suprim-corp.kit-backend-base-java</groupId>
    <artifactId>kit-java-json</artifactId>
    <version>1.0.2</version>
</dependency>

<!-- Exception framework -->
<dependency>
    <groupId>com.github.suprim-corp.kit-backend-base-java</groupId>
    <artifactId>kit-java-exception</artifactId>
    <version>1.0.2</version>
</dependency>

<!-- Crypto utilities -->
<dependency>
    <groupId>com.github.suprim-corp.kit-backend-base-java</groupId>
    <artifactId>kit-java-crypto</artifactId>
    <version>1.0.2</version>
</dependency>

<!-- Web utilities -->
<dependency>
    <groupId>com.github.suprim-corp.kit-backend-base-java</groupId>
    <artifactId>kit-java-web</artifactId>
    <version>1.0.2</version>
</dependency>

<!-- gRPC utilities -->
<dependency>
    <groupId>com.github.suprim-corp.kit-backend-base-java</groupId>
    <artifactId>kit-java-grpc</artifactId>
    <version>1.0.2</version>
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
BaseResponse<User> response = new BaseResponse<>(user);
BaseResponse<Void> error = new BaseResponse<>(ApiStatus.NOT_FOUND, "User not found");

// Paginated responses
PaginatedResponse<Item> paginated = new PaginatedResponse<>(items, totalCount, pageable);

// Validation errors
ValidationError error = new ValidationError("email", "Invalid format");
```

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

// Access context in service implementation
String requestId = GrpcContext.getRequestId();
String userId = GrpcContext.getUserId();

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

# Code Standards

## Type Safety

### No `var` keyword
Always use explicit types. Type inference reduces readability and type safety.

```java
// BAD
var result = service.process();
var list = new ArrayList<>();

// GOOD
ProcessResult result = service.process();
List<String> list = new ArrayList<>();
```

### No raw null comparisons
Use `Objects.isNull()` and `Objects.nonNull()` with static imports.

```java
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

// BAD
if (value == null) { }
if (value != null) { }

// GOOD
if (isNull(value)) { }
if (nonNull(value)) { }
```

### No @SuppressWarnings
Cast properly instead of suppressing warnings. If generics cause issues, restructure the code.

```java
// BAD
@SuppressWarnings("unchecked")
List<String> list = (List<String>) rawList;

// GOOD - use proper generics or redesign
List<String> list = rawList.stream()
    .map(String.class::cast)
    .toList();
```

## Serialization

### Prefer Jackson 3
Use Jackson 3.x for JSON serialization. Configure properly for records and sealed types.

## Null Handling

### Use Optional for return types
When a method may not return a value, use `Optional<T>`.

```java
// BAD
public Payment findById(PaymentId id) { return null; }

// GOOD
public Optional<Payment> findById(PaymentId id) { }
```

### Use Objects.requireNonNull for validation
Validate non-null arguments in constructors and public methods.

```java
public Payment(PaymentId id, Money amount) {
    this.id = Objects.requireNonNull(id, "PaymentId required");
    this.amount = Objects.requireNonNull(amount, "Amount required");
}
```

## Records and Immutability

### Prefer records for value objects
Use Java records for immutable value objects.

```java
public record PaymentId(UUID value) {
    public PaymentId {
        Objects.requireNonNull(value, "PaymentId value required");
    }
}
```

## Naming Conventions

### Packages
- Lowercase, no underscores
- Module structure: `dev.suprim.kit.{module}.{layer}`

### Classes
- PascalCase
- Suffix with role: `PaymentRepository`, `PaymentService`, `PaymentController`

### Methods
- camelCase
- Verbs for actions: `createPayment()`, `findById()`, `processRefund()`

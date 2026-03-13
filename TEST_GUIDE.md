# Hướng dẫn Test — Saga Orchestration Demo

## Chuẩn bị

### Khởi động tất cả services (thứ tự không quan trọng)

Mở **4 terminal riêng biệt**, mỗi terminal chạy một lệnh:

```bash
# Terminal 1 — Payment Service (port 8080)
cd payment-service
mvn spring-boot:run

# Terminal 2 — Inventory Service (port 8081)
cd inventory-service
mvn spring-boot:run

# Terminal 3 — Shipping Service (port 8082)
cd shipping-service
mvn spring-boot:run

# Terminal 4 — Order Service / Orchestrator (port 8083)
cd order-service
mvn spring-boot:run
```

> **Lưu ý:** Mỗi request mất khoảng **9–12 giây** do mỗi service sleep 3 giây để simulate delay.

---

### Quy tắc về `quantity` (điều kiện hardcode trong code)

| `quantity` | Kết quả Inventory |
|---|---|
| `1` đến `9` | ✅ Pass (`10 > quantity`) |
| `10` trở lên | ❌ Fail (`10 > quantity` = false) |

`productId` có thể là bất kỳ giá trị nào (không được validate trong demo).

---

## Luồng 1 — Happy Path (Thành công toàn bộ)

**Mô tả:** Tất cả services đang chạy, `quantity < 10` → Saga hoàn thành thành công.

**Điều kiện:** 4 services đều đang chạy.

### Request

```bash
curl -X POST http://localhost:8083/api/order/process \
  -H "Content-Type: application/json" \
  -d '{"productId": "ticket1", "quantity": 5}'
```

### Luồng thực thi

```
OrderService → [1] PaymentService.processPayment()   → "Payment successful"
             → [2] InventoryService.processInventory() → "Inventory reserved"
             → [3] ShippingService.processShipping()   → "Shipping scheduled"
             → status = "Completed"
```

### Response mong đợi (HTTP 200)

```json
{
  "orderId": "<uuid ngẫu nhiên>",
  "productId": "ticket1",
  "quantity": 5,
  "status": "Completed"
}
```

### Log mong đợi (order-service)

```
[OrderService:processOrder] = Started
[processOrder] Calling Payment service: processPayment
[processOrder] processPayment successfully: processPayment
[processOrder] Calling Inventory service: processInventory
[processOrder] processInventory successfully: processInventory
[processOrder] Calling Shipping service: processShipping
[processOrder] processShipping successfully: processShipping
[OrderService] Order processed successfully: Completed
```

---

## Luồng 2 — Inventory Fail (Hàng tồn kho không đủ)

**Mô tả:** Payment thành công, nhưng Inventory thiếu hàng → Saga rollback, compensating transaction được gọi.

**Điều kiện:** 4 services đều đang chạy.

### Request

```bash
curl -X POST http://localhost:8083/api/order/process \
  -H "Content-Type: application/json" \
  -d '{"productId": "ticket1", "quantity": 10}'
```

### Luồng thực thi

```
OrderService → [1] PaymentService.processPayment()     → "Payment successful"
             → [2] InventoryService.processInventory()  → ❌ throw AppException ("Insufficient inventory")
             ↓
             [COMPENSATE]
             → cancelPayment()    → "Payment canceled"
             → cancelInventory()  → "Inventory reservation canceled"
             → cancelShipping()   → "Shipping canceled"   ⚠️ (gọi dù shipping chưa từng chạy)
             → status = "Failed"  → throw AppException
```

> ⚠️ **Lưu ý về behavior hiện tại:** `cancelShipping` được gọi dù bước Shipping chưa bao giờ được thực thi. Đây là bug nhỏ trong logic compensating — không ảnh hưởng đến kết quả cuối cùng của demo nhưng tạo ra log thừa.

### Response mong đợi (HTTP 500)

```json
{
  "timestamp": "...",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/order/process"
}
```

> **Tại sao HTTP 500?** Vì không có `@ControllerAdvice` nên `AppException` được Spring xử lý mặc định. Muốn thấy `Order` object trong response, cần thêm global exception handler.

### Log mong đợi (order-service)

```
[OrderService:processOrder] = Started
[processOrder] Calling Payment service: processPayment
[processOrder] processPayment successfully: processPayment
[processOrder] Calling Inventory service: processInventory
[OrderService] Error processing order: ...
[OrderService] Calling Payment service to cancel payment
[OrderService] Calling Inventory service to cancel inventory
[OrderService] Calling Shipping service to cancel shipping
```

### Log mong đợi (inventory-service)

```
Processing inventory for order: <uuid>
Insufficient inventory for order: <uuid>
Canceling inventory for order: <uuid>
```

---

## Luồng 3 — Payment Service Down

**Mô tả:** Payment service bị tắt → Feign không kết nối được → Saga rollback ngay từ bước đầu.

**Điều kiện:** Tắt `payment-service` (Ctrl+C Terminal 1), giữ nguyên 3 service còn lại.

### Request

```bash
curl -X POST http://localhost:8083/api/order/process \
  -H "Content-Type: application/json" \
  -d '{"productId": "ticket1", "quantity": 5}'
```

### Luồng thực thi

```
OrderService → [1] PaymentService.processPayment() → ❌ Connection refused (Feign exception)
             ↓
             [COMPENSATE]
             → cancelPayment()    → ❌ Connection refused (bắt lỗi, bỏ qua)
             → cancelInventory()  → "Inventory reservation canceled"  ⚠️ (gọi dù inventory chưa chạy)
             → cancelShipping()   → "Shipping canceled"               ⚠️ (gọi dù shipping chưa chạy)
             → status = "Failed"
```

### Response mong đợi (HTTP 500)

```json
{
  "timestamp": "...",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/order/process"
}
```

### Log mong đợi (order-service)

```
[OrderService:processOrder] = Started
[processOrder] Calling Payment service: processPayment
[OrderService] Error processing order: ...
[OrderService] Calling Payment service to cancel payment
[OrderService] Error canceling payment: ...          ← payment vẫn down, bắt lỗi
[OrderService] Calling Inventory service to cancel inventory
[OrderService] Calling Shipping service to cancel shipping
```

---

## Luồng 4 — Inventory Service Down

**Mô tả:** Payment thành công, nhưng Inventory service bị tắt → Feign lỗi ở bước 2 → rollback.

**Điều kiện:** Tắt `inventory-service` (Ctrl+C Terminal 2), giữ nguyên payment, shipping, order.

### Request

```bash
curl -X POST http://localhost:8083/api/order/process \
  -H "Content-Type: application/json" \
  -d '{"productId": "ticket1", "quantity": 5}'
```

### Luồng thực thi

```
OrderService → [1] PaymentService.processPayment()     → "Payment successful"
             → [2] InventoryService.processInventory()  → ❌ Connection refused
             ↓
             [COMPENSATE]
             → cancelPayment()    → "Payment canceled"              ✅
             → cancelInventory()  → ❌ Connection refused (bắt lỗi, bỏ qua)
             → cancelShipping()   → "Shipping canceled"             ⚠️ (chưa chạy)
             → status = "Failed"
```

### Log mong đợi (order-service)

```
[processOrder] Calling Payment service: processPayment
[processOrder] processPayment successfully: processPayment
[processOrder] Calling Inventory service: processInventory
[OrderService] Error processing order: ...
[OrderService] Calling Payment service to cancel payment
[OrderService] Calling Inventory service to cancel inventory
[OrderService] Error canceling inventory: ...          ← inventory vẫn down
[OrderService] Calling Shipping service to cancel shipping
```

---

## Luồng 5 — Shipping Service Down

**Mô tả:** Payment và Inventory thành công, nhưng Shipping service bị tắt → Feign lỗi ở bước 3 → rollback.

**Điều kiện:** Tắt `shipping-service` (Ctrl+C Terminal 3), giữ nguyên payment, inventory, order.

### Request

```bash
curl -X POST http://localhost:8083/api/order/process \
  -H "Content-Type: application/json" \
  -d '{"productId": "ticket1", "quantity": 5}'
```

### Luồng thực thi

```
OrderService → [1] PaymentService.processPayment()     → "Payment successful"
             → [2] InventoryService.processInventory()  → "Inventory reserved"
             → [3] ShippingService.processShipping()    → ❌ Connection refused
             ↓
             [COMPENSATE]
             → cancelPayment()    → "Payment canceled"                ✅
             → cancelInventory()  → "Inventory reservation canceled"  ✅
             → cancelShipping()   → ❌ Connection refused (bắt lỗi, bỏ qua)
             → status = "Failed"
```

> **Đây là luồng rollback đầy đủ nhất** — cả payment và inventory đều đã thực thi thật sự trước khi rollback.

### Log mong đợi (order-service)

```
[processOrder] Calling Payment service: processPayment
[processOrder] processPayment successfully: processPayment
[processOrder] Calling Inventory service: processInventory
[processOrder] processInventory successfully: processInventory
[processOrder] Calling Shipping service: processShipping
[OrderService] Error processing order: ...
[OrderService] Calling Payment service to cancel payment
[OrderService] Calling Inventory service to cancel inventory
[OrderService] Calling Shipping service to cancel shipping
[OrderService] Error canceling shipping: ...           ← shipping vẫn down
```

---

## Tổng hợp tất cả luồng

| # | Tên luồng | `quantity` | Services | Kết quả |
|---|---|---|---|---|
| 1 | Happy Path | `5` | Tất cả bật | ✅ `"Completed"` |
| 2 | Inventory Insufficient | `10` | Tất cả bật | ❌ Rollback (payment + inventory + shipping canceled) |
| 3 | Payment Down | `5` | Tắt payment | ❌ Rollback (ngay từ đầu) |
| 4 | Inventory Down | `5` | Tắt inventory | ❌ Rollback (payment đã chạy → cancel payment) |
| 5 | Shipping Down | `5` | Tắt shipping | ❌ Rollback đầy đủ (payment + inventory đã chạy → cancel cả hai) |

---

## Test nhanh bằng Postman

1. Import request sau vào Postman:
   - **Method:** `POST`
   - **URL:** `http://localhost:8083/api/order/process`
   - **Headers:** `Content-Type: application/json`
   - **Body (raw JSON):**
     ```json
     {
       "productId": "ticket1",
       "quantity": 5
     }
     ```
2. Thay đổi `quantity` giữa `5` (thành công) và `10` (thất bại inventory) để quan sát 2 luồng cơ bản mà không cần tắt service nào.
3. Để test luồng 3–5, dừng service tương ứng rồi gửi lại request.

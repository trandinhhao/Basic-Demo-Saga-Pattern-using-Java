# Luồng lỗi saga (Camunda orchestrator — `OrderSaga`)

Quy trình chuẩn: **Thanh toán → Kho → Giao hàng**. Mỗi bước là service task; nếu ném `BpmnError` với mã tương ứng, Camunda chạy **nhánh bù trừ** rồi kết thúc ở trạng thái thất bại trong bảng `order_saga`.

---

## 1. Lỗi ở **Payment** (mã `PAYMENT_FAILED`)

**Khi nào:** Gọi `payment-service` lỗi (mạng, 5xx) hoặc service ném `AppException` (ví dụ gián đoạn khi xử lý).

**Đã làm được gì trước đó:** Chưa thanh toán thành công theo nghiệp vụ demo.

**Camunda làm gì:**

1. Boundary trên bước thanh toán bắt `PAYMENT_FAILED`.
2. Chạy **Hoàn tiền / hủy thanh toán** (`compensatePaymentDelegate`) — trong demo thường là thao tác an toàn (idempotent).
3. Chạy **Đánh dấu saga thất bại** (`markSagaFailedDelegate`).
4. Kết thúc tại end event **thất bại**.

**Trong DB `order_saga`:** thường thấy `PAYMENT_FAILED` hoặc sau bù `COMPENSATED_PAYMENT` / `COMPENSATE_PAYMENT_ERROR`, cuối cùng `SAGA_FAILED` (xem cột `last_error`).

---

## 2. Lỗi ở **Inventory** (mã `INVENTORY_FAILED`)

**Khi nào:** Không đủ tồn (demo: `quantity` > tồn cố định 10) hoặc lỗi khi giữ hàng.

**Đã làm được:** Thanh toán đã xong (`PAID`).

**Camunda làm gì:**

1. Boundary trên bước kho bắt `INVENTORY_FAILED`.
2. **Hủy giữ hàng** (`compensateInventoryDelegate`).
3. **Hoàn tiền** (`compensatePaymentDelegate`).
4. **Đánh dấu saga thất bại** → end thất bại.

**Thứ tự bù:** kho trước (nếu đã giữ — demo có thể chưa giữ), rồi **luôn gọi hoàn tiền** vì tiền đã trừ.

---

## 3. Lỗi ở **Shipping** (mã `SHIPPING_FAILED`)

**Khi nào:** `shipping-service` ném lỗi (ví dụ `IllegalStateException` khi xử lý giao hàng thất bại).

**Đã làm được:** Thanh toán + giữ hàng xong.

**Camunda làm gì:**

1. Boundary trên bước giao hàng bắt `SHIPPING_FAILED`.
2. **Hủy giao hàng** (`compensateShippingDelegate`).
3. **Hủy giữ hàng** (`compensateInventoryDelegate`).
4. **Hoàn tiền** (`compensatePaymentDelegate`).
5. **Đánh dấu saga thất bại** → end thất bại.

**Thứ tự bù:** ngược với luồng thuận — giao → kho → thanh toán.

---

## 4. Ghi chú

- **HTTP API** vẫn trả `200` với body `Order` khi saga kết thúc (thành công hoặc thất bại): hãy đọc field `status` trong response và/hoặc bản ghi `order_saga`.
- **Log ứng dụng** (các service + tiền tố `[Orchestrator]`, `[Thanh toán]`, `[Kho]`, `[Giao hàng]`, `[Saga-DB]`) đều bằng tiếng Việt để theo dõi từng bước.
- **Camunda** còn ghi lịch sử vào các bảng `ACT_*` trên cùng database (nếu dùng Supabase làm datasource).

---

## 5. Thử lỗi kho (Postman)

`POST` order-service `/api/order/process`:

```json
{ "productId": "P1", "quantity": 99 }
```

Với tồn demo = 10 → kho từ chối → nhánh `INVENTORY_FAILED` → bù như mục 2.

## AI Usage

### 1. AI Tools Used
Trong suốt quá trình phát triển dự án, nhóm đã tận dụng hai nền tảng AI chính, mỗi nền tảng mang lại những thế mạnh riêng biệt:

- **Claude Sonnet 4.5** (Anthropic): Hỗ trợ chính trong việc review code, gợi ý cấu trúc design pattern và tối ưu hóa logic nghiệp vụ. Claude đặc biệt hữu ích khi phân tích các tình huống edge cases phức tạp và đề xuất cách tổ chức code theo nguyên tắc SOLID.

- **Gemini 3.1 Pro** (Google): Tham khảo cú pháp Java Swing, gợi ý cách xử lý exception và validate dữ liệu đầu vào. Gemini tỏ ra hiệu quả trong việc cung cấp các ví dụ code cụ thể cho các thành phần GUI và các thao tác với database JDBC.

### 2. Prompt Mẫu Đã Sử Dụng

**Prompt 1 - Thiết kế Billing Service:**
```
Tôi đang xây dựng hệ thống tính tiền cho ứng dụng bán hàng Java OOP.
Cần implement 3 phương thức computeBill() overload:
- computeBill(price)
- computeBill(price, quantity)
- computeBill(price, quantity, couponDiscount)
Hãy gợi ý cách tính tax 8% và xử lý trường hợp discount lớn hơn subtotal.
```

**Prompt 2 - GUI Layout:**
```
Làm thế nào để tạo login form đẹp với Java Swing?
Tôi muốn chia màn hình thành 2 phần: bên trái là brand logo,
bên phải là form nhập username/password. Sử dụng GridBagLayout.
```

**Prompt 3 - Database Validation:**
```
Viết helper method kiểm tra trùng phone/email trong CustomerRepository.
Sử dụng PreparedStatement để tránh SQL injection.
```

### 3. Code Do AI Gợi Ý:

Nhóm đã xem xét, điều chỉnh và tích hợp các đoạn code và gợi ý này vào codebase chung, đảm bảo phù hợp với kiến trúc tổng thể và yêu cầu nghiệp vụ của dự án. Dù AI tạo ra code khung, việc kiểm thử và tinh chỉnh vẫn do con người thực hiện:

#### 3.1. Utility Methods trong BillingServiceImpl
AI hỗ trợ viết các hàm tiện ích như `truncate()`, `centerText()` và logic tính tax. Những method này tuy đơn giản nhưng rất quan trọng để đảm bảo tính nhất quán trong format dữ liệu hiển thị trên invoice:

```java
// BillingServiceImpl.java - Lines 313-332
private String truncate(String str, int maxLen) {
    if (str == null) return "";
    return str.length() <= maxLen ? str : str.substring(0, maxLen - 2) + "..";
}

private String centerText(String text, int width) {
    if (text == null) text = "";
    if (text.length() >= width) return text;
    int padding = (width - text.length()) / 2;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < padding; i++) sb.append(" ");
    sb.append(text);
    for (int i = 0; i < width - text.length() - padding; i++) sb.append(" ");
    return sb.toString();
}
```

#### 3.2. Generate Invoice Template
AI gợi ý format invoice dạng text với căn chỉnh cột. Đây là một tính năng quan trọng của hệ thống billing, giúp hóa đơn in ra có bố cục rõ ràng, chuyên nghiệp và dễ đọc:

```java
// BillingServiceImpl.java - Lines 174-237
public String generateInvoice(Order order) {
    StringBuilder sb = new StringBuilder();
    sb.append("================================================================\\n");
    sb.append(centerText(storeName, 64)).append("\\n");
    sb.append("----------------------------------------------------------------\\n");
    sb.append("                            INVOICE                             \\n");
    // ... AI gợi ý cách format từng dòng với String.format()
    sb.append(String.format(" %-22s | %4s | %13s | %14s\\n", "Item Name", "Qty", "Price", "Total"));
}
```

#### 3.3. Login Frame UI Layout
AI tư vấn cách dùng GridBagLayout và UITheme để tạo giao diện đăng nhập hiện đại, cân đối. Việc bố trí layout hợp lý giúp trải nghiệm người dùng trở nên mượt mà và chuyên nghiệp hơn:

```java
// LoginFrame.java - Lines 41-61
JPanel brand = new JPanel(new GridBagLayout());
brand.setBackground(new Color(10, 14, 24));
brand.setPreferredSize(new Dimension(280, 0));
GridBagConstraints g = new GridBagConstraints();
g.gridx = 0;
g.insets = new Insets(6, 0, 6, 0);
// ... AI gợi ý constraint layout cho brand panel
```

#### 3.4. Repository Helper Methods
AI đề xuất pattern map ResultSet sang Model, một kỹ thuật phổ biến trong layered architecture giúp tách biệt giữa lớp truy xuất dữ liệu và lớp business logic:

```java
// CustomerRepositoryImpl.java - Lines 13-28
private Customer mapRow(ResultSet rs) throws SQLException {
    Customer c = new Customer(
            rs.getInt("customer_id"),
            rs.getString("customer_name"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getString("address"),
            rs.getTimestamp("created_date"));
    try {
        c.setActive(rs.getBoolean("is_active"));
    } catch (SQLException e) {
        // ignore if column doesn't exist yet
    }
    return c;
}
```

#### 3.5. Validation Rules cho Dữ Liệu Đầu Vào
AI đóng vai trò quan trọng trong việc cung cấp code mẫu cho các **nguyên tắc validate cú pháp** của dữ liệu đầu vào. Các thông tin nhạy cảm và quan trọng như số điện thoại, email, địa chỉ đều cần được kiểm tra định dạng chặt chẽ trước khi lưu trữ vào database. AI đã hỗ trợ nhóm xây dựng:

- Các pattern regex để kiểm tra định dạng email hợp lệ (ví dụ: `^[A-Za-z0-9+_.-]+@(.+)$`)
- Quy tắc validate số điện thoại (độ dài, ký tự số, prefix quốc gia nếu cần)
- Logic kiểm tra độ dài tối thiểu/tối đa cho các trường văn bản
- Pattern xác thực địa chỉ không chứa ký tự đặc biệt nguy hiểm

Nhóm sau đó đã tùy biến, mở rộng và tích hợp các nguyên tắc này vào hệ thống validation chung của dự án, đảm bảo mọi dữ liệu khách hàng nhập vào đều được rà soát kỹ lưỡng trước khi chấp nhận:

```java
// ValidationHelper.java - Ví dụ các method validate do AI gợi ý
public static boolean isValidEmail(String email) {
    if (email == null || email.trim().isEmpty()) return false;
    String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
    return email.matches(emailRegex);
}

public static boolean isValidPhone(String phone) {
    if (phone == null || phone.trim().isEmpty()) return false;
    // AI gợi ý pattern: chỉ chứa số, độ dài 10-11 ký tự, có thể bắt đầu bằng 0 hoặc +84
    String phoneRegex = "^(0|\\+84)[0-9]{9,10}$";
    return phone.matches(phoneRegex);
}

public static boolean isValidAddress(String address) {
    if (address == null || address.trim().length() < 5) return false;
    // AI tư vấn không cho phép ký tự đặc biệt nguy hiểm như < > / \
    return !address.matches(".*[<>\\\\/].*");
}
```

### 4. Code Tự Chỉnh Sửa & Phát Triển Thêm

Sau khi nhận được các gợi ý ban đầu từ AI, nhóm đã dành phần lớn thời gian để nghiên cứu sâu, phân tích yêu cầu nghiệp vụ thực tế, và triển khai các giải pháp tối ưu. Các đoạn code dưới đây hoàn toàn do nhóm tự viết hoặc chỉ sử dụng AI như một công cụ tham khảo ở mức độ hạn chế:

#### 4.1. Business Logic Xử Lý Discount Edge Cases
Nhóm tự nghiên cứu và thêm logic xử lý trường hợp đặc biệt khi discount ≥ subtotal. Đây là một tình huống thực tế mà nếu không xử lý đúng có thể dẫn đến âm tiền hoặc tính toán sai lệch. Team đã phân tích kỹ các scenario và đưa ra giải pháp an toàn:

```java
// BillingServiceImpl.java - Lines 77-90
if (discountAmount.compareTo(subtotal) >= 0 && subtotal.compareTo(BigDecimal.ZERO) > 0) {
    discountAmount = subtotal.subtract(BigDecimal.ONE);
    order.setDiscountAmount(discountAmount);
    order.setTaxRate(BigDecimal.ZERO);
    order.setFinalTotal(BigDecimal.ONE);
} else {
    order.setTaxRate(taxRate);
    BigDecimal afterDiscount = subtotal.subtract(discountAmount);
    if (afterDiscount.compareTo(BigDecimal.ZERO) < 0)
        afterDiscount = BigDecimal.ZERO;
    BigDecimal taxAmount = afterDiscount.multiply(taxRate)
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    order.setFinalTotal(finalTotal);
}
```

#### 4.2. Stock Validation Logic
Nhóm tự implement logic kiểm tra tồn kho trước khi tạo order. Việc kiểm tra này giúp đảm bảo tính toàn vẹn dữ liệu, tránh trường hợp hệ thống ghi nhận đơn hàng nhưng thực tế không đủ hàng, dẫn đến sai lệch số liệu và không đáp ứng được nhu cầu khách hàng:

```java
// BillingServiceImpl.java - Lines 239-253
private void validateStock(List<OrderDetail> items) {
    if (items == null) return;
    for (OrderDetail detail : items) {
        if (detail.getItem() != null && detail.getItem().getItemSku() != null) {
            Item dbItem = itemRepo.findBySku(detail.getItem().getItemSku());
            if (dbItem != null && detail.getQuantity() > dbItem.getStockQuantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for '" + dbItem.getItemName() +
                        "'. Requested: " + detail.getQuantity() +
                        ", Available: " + dbItem.getStockQuantity() + ".");
            }
        }
    }
}
```

#### 4.3. Async Login với SwingWorker
Nhóm tự nghiên cứu tìm hiểu SwingWorker thông qua các công cụ chatbot AI, và tự áp dụng để tránh block UI thread. Kỹ thuật này rất quan trọng trong các ứng dụng Desktop sử dụng Swing để đảm bảo giao diện luôn phản hồi nhanh với người dùng, tránh tình trạng "đóng băng" (freezing) khi thực hiện các tác vụ tốn thời gian như gọi API, truy vấn database:

```java
// LoginFrame.java - Lines 132-154
SwingWorker<User, Void> w = new SwingWorker<>() {
    protected User doInBackground() {
        return authService.login(user, pass);
    }

    protected void done() {
        try {
            User u = get();
            dispose();
            SwingUtilities.invokeLater(() -> new MainFrame(u, authService).setVisible(true));
        } catch (Exception ex) {
            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
            errorLabel.setText(cause instanceof AuthenticationException
                    ? cause.getMessage()
                    : "Login error: " + cause.getMessage());
        }
    }
};
w.execute();
```

#### 4.4. Search với PreparedStatement
Nhóm tự viết logic search customer với wildcard pattern. Tính năng tìm kiếm linh hoạt giúp nhân viên dễ dàng tra cứu thông tin khách hàng dựa trên tên hoặc số điện thoại, nâng cao hiệu quả phục vụ:

```java
// CustomerRepositoryImpl.java - Lines 79-95
public List<Customer> searchByNameOrPhone(String keyword) {
    List<Customer> list = new ArrayList<>();
    String sql = "SELECT * FROM customers WHERE LOWER(customer_name) LIKE LOWER(?) OR phone LIKE ? ORDER BY is_active DESC, customer_id";
    try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {
        String pattern = "%" + keyword + "%";
        ps.setString(1, pattern);
        ps.setString(2, pattern);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapRow(rs));
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return list;
}
```

#### 4.5. Audit Log Integration
Nhóm tự tích hợp audit logging vào mọi thao tác CRUD:

```java
// BillingServiceImpl.java - Lines 106, 149, 168
logAudit(currentUser, "CREATE_ORDER", "ORDER", String.valueOf(orderId));
logAudit(currentUser, "UPDATE_ORDER", "ORDER", String.valueOf(order.getOrderId()));
logAudit(currentUser, "CANCEL_ORDER", "ORDER", String.valueOf(orderId));

// BillingServiceImpl.java - Lines 303-311
private void logAudit(User user, String action, String targetType, String targetId) {
    AuditLog log = new AuditLog();
    log.setUser(user);
    log.setActions(action);
    log.setTargetType(targetType);
    log.setTargetId(targetId);
    log.setCreatedDate(new Timestamp(System.currentTimeMillis()));
    auditLogRepo.insert(log);
}
```

### 5. Tóm Tắt Mức Độ Sử Dụng AI

| Hạng mục | Tỷ lệ AI | Ghi chú |
|----------|---------|---------|
| **UI Layout (Swing)** | ~45% | AI gợi ý GridBagLayout constraints, color scheme |
| **Business Logic** | ~30% | Team tự nghiên cứu edge cases, validation rules |
| **Repository/DAO** | ~35% | AI đề xuất pattern, team tự viết SQL queries |
| **Utility Functions** | ~50% | AI viết helper methods nhỏ |
| **Architecture Design** | ~25% | Team tự quyết định layered architecture, DI pattern |
| **Error Handling** | ~20% | Team tự research exception types và cách handle |

**Tổng kết:** Dự án sử dụng AI chủ yếu như một công cụ tham khảo và hỗ trợ (reference tool), tương đương khoảng **35-40%** tổng thể codebase. Phần lớn logic nghiệp vụ phức tạp, kiến trúc phân tầng (layered architecture), xử lý ngoại lệ, và tích hợp database đều do nhóm tự nghiên cứu, thảo luận và triển khai. AI đóng vai trò như một 'pair programmer' giúp tăng tốc độ phát triển ở những phần boilerplate và gợi ý best practices, nhưng không thay thế hoàn toàn quá trình tư duy thiết kế của con người.

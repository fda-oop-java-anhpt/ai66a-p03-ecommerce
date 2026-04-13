# Design Document

## 1. Danh sách các lớp và vai trò (Class List & Responsibilities)

| Class | Package | Vai trò |
|------|--------|--------|
| `User` | `model` | Đại diện cho thực thể người dùng, bao gồm thông tin đăng nhập và phân quyền (admin/staff). |
| `Customer` | `model` | Chứa thông tin hồ sơ của khách (tên, liên lạc, địa chỉ, ngày tạo). |
| `Item` | `model` | Trạng thái của một sản phẩm lưu kho (số lượng, giá tiền, mô tả). |
| `Order` | `model` | Chứa thông tin tổng quan của một hóa đơn (khách hàng, tổng giá trị, trạng thái). |
| `OrderDetail` | `model` | Biểu diễn chi tiết một sản phẩm trong đơn hàng (mối liên hệ giữa Order và Item). |
| `UserRepositoryImpl` | `repository.impl` | Lớp JDBC truy xuất CSDL trực tiếp để tạo/cập nhật/truy vấn `User`. |
| `OrderRepositoryImpl`| `repository.impl` | Lớp JDBC chịu trách nhiệm lưu trữ và cập nhật trạng thái của Order xuống hệ cơ sở dữ liệu. |
| `AuthServiceImpl` | `service.impl` | Nơi chứa logic nghiệp vụ xử lý quy trình đăng nhập, xác thực quyền truy cập. |
| `BillingServiceImpl` | `service.impl` | Chứa logic nghiệp vụ tính toán tổng hóa đơn, áp dụng thuế (tax) và lưu trữ đơn. |
| `MainFrame` | `ui.frames` | Cửa sổ làm việc chính (Swing) của ứng dụng, chứa các navigation Tabs (Orders, Items, Customers...). |
| `LoginFrame` | `ui.frames` | Cửa sổ giao diện đăng nhập đầu tiên khi ứng dụng khởi chạy. |
| `OrderDialog` | `ui.dialogs` | Hộp thoại đa phương thức (modal) cung cấp input cho thao tác tạo mới hoặc sửa `Order`. |
| `DatabaseConnection` | `util` | Lớp nạp cấu hình `.env` để duy trì kết nối chung (Connection pool) tới PostgreSQL. |

---

## 2. Áp dụng các nguyên lý OOP

Mô tả rõ **từng nguyên lý OOP được áp dụng ở đâu trong hệ thống**.

### 2.1. Encapsulation
- Các thuộc tính nào được khai báo `private`? Toàn bộ thuộc tính của các lớp Model (vd: `Customer.java`, `User.java`, `Item.java`) đều là `private`.
- Truy cập thông qua getter/setter nào? Thông qua các method public như `getUserName()`, `setUserName()`, `getEmail()`, vv.
- Lý do áp dụng encapsulation? Để bảo vệ tính toàn vẹn của dữ liệu và kiểm duyệt các giá trị đầu vào hợp lệ khi gán vào Model (vd: validate chuỗi tên không rỗng, validate password lớn hơn 6 kí tự).

**Mô tả:**
> Đảm bảo hạn chế tối đa việc truy xuất trực tiếp trạng thái bên trong của object, đồng thời linh hoạt thay đổi logic ở getter/setter mà không làm vỡ (break) kiến trúc của các lớp gọi tới nó.

---

### 2.2. Inheritance
- Class cha là gì? Các lớp thư viện sẵn của Swing như `JFrame`, `JDialog`, `JPanel`.
- Các class con kế thừa từ đâu? `MainFrame`, `LoginFrame` mở rộng `JFrame`. Các Frame con phân mảnh như `OrderDialog`, `ProfileDialog` kế thừa `JDialog`.
- Lý do sử dụng kế thừa? Thừa hưởng toàn bộ thuộc tính UI, cấu trúc Window và các phương thức xử lý vòng đời hiển thị sẵn có của Java Swing mà không phải tự xây dựng lại một cửa sổ từ đầu chân không.

**Mô tả:**
> Trong toàn bộ phần `com.oop.project.ui`, code được chia lớp rõ ràng nhờ kế thừa các Abstract Class/Superclass hiển thị của Java `javax.swing.*`. Cây phân cấp GUI giúp dễ bảo trì và mở rộng thêm thành phần giao diện mới.

---

### 2.3. Polymorphism
- Phương thức nào được override? Trong các Data Model, override (ghi đè) phương thức `toString()`, `equals()`, `hashCode()` của lớp cao nhất `Object`. Override method `actionPerformed()` từ Interface `ActionListener` tại các button. Các service implement Override behavior từ interface.
- Được gọi thông qua reference kiểu cha ở đâu? Ví dụ khi gán Interface Reference: `IUserService userService = new UserServiceImpl(userRepo);` 

**Mô tả:**
> Lợi dụng đa hình giúp code ứng dụng có thể thay đổi cách hoạt động của nhiều thành phần giống nhau. Ví dụ: Khi log thông tin User, `System.out.println(user)` sẽ tự động gọi phương thức `toString()` vừa override để hiển thị định dạng phù hợp.

---

### 2.4. Interface
- Interface nào được sử dụng? Interface các nghiệp vụ tại `service.interfaces.*` (ví dụ `IAuthService`, `IItemService`) và DAO ở `repository.interfaces.*` (ví dụ `UserRepository`).
- Vai trò của interface trong thiết kế? Xác định các Contract (giao kèo) thiết kế mà class hiện thực phải tuân theo. Tách rời (decoupling) phần định nghĩa (method signature) khỏi nội dung code chi tiết. Rất hữu ích khi ta viết Mock code để Test.

**Mô tả:**
> Giúp các developer làm việc với nhau dễ hơn. Phần UI (trên) chỉ cần làm việc với Method của Interface, phần Repository (dưới) có thể thoải mái code/đổi database mà phía trên UI không bị ảnh hưởng.

---

### 2.5. Abstraction
- Abstract class / method nào được sử dụng? Ẩn giấu đi toàn bộ việc query `SELECT...`, `UPDATE...` của JDBC khỏi tầng giao diện (UI) hoặc Controller thông qua lớp Service.
- Phần chi tiết nào được ẩn đi? `MainFrame` không cần biết việc làm thế nào để validate user từ Database, mà chỉ cần trừu tượng hóa bằng lệnh `authService.login()`.

**Mô tả:**
> Tập trung vào "đây là chức năng gì" thay vì "chức năng này xử lí cụ thể từng dòng code / truy xuất Database như thế nào".

---

## 3. Design Patterns được sử dụng

Liệt kê các design pattern (nếu có) và giải thích ngắn gọn cách áp dụng.

| Design Pattern | Áp dụng ở đâu | Mục đích |
|---------------|-------------|---------|
| Singleton Pattern | `DatabaseConnection.java` | Quản lý đúng một đối tượng Connection duy nhất truy vấn tới PostgreSQL cho tất cả các Repo thay vì liên tục gọi connection mới gây nghẽn RAM. |
| Layered / MVC | Cấu trúc toàn bộ phần `src` | Chia cắt theo chiều dọc các phần: View (ui), Controller/Service (service), Data/Model (model + repo). |
| Dependency Injection | Khởi tạo Service trong UI | UI truyền đối số Object Repository đã được khỏi tạo trước (inject qua tham số Constructor) cho Service thay vì để Service `new Repo`. |

> Kết hợp Layered và DI giúp ứng dụng dễ bảo trì và có luồng rác thải kiểm soát tốt hơn.

---

## 4. Luồng hoạt động chính (Main Application Flows)

Mô tả các luồng xử lý chính của hệ thống theo dạng từng bước.

### 4.1. Login
1. Người dùng chạy chương trình, màn hình `LoginFrame` hiện lên nhập User/Pass.
2. Nút Login gửi thông tin đến `IAuthService.login()`.
3. `IAuthService` gọi tiếp `UserRepository` query SQL lên CSDL.
4. Nếu kiểm tra Hash Password DB là đúng, khởi tạo session (nội bộ).
5. Load cửa sổ `MainFrame`, pass instance `User` vào Constructor để xác định Admin/Staff nhằm load đúng UI Tab hiển thị.

---

### 4.2. Quản lí và Tạo đơn hàng (Order Creation)
1. User ở cửa sổ `MainFrame`, click vào Tab Order.
2. Bấm Button `Create Order`. Khởi tạo `OrderDialog` cho khách hàng trực tiếp.
3. Nhập chọn ID/Tên Khách Hàng (Customer), add từng Item sản phẩm với Quantity.
4. `OrderDialog` liên tục gọi hàm `Total` từ `IBillingService` theo số lượng để Update giao diện.
5. Khi người dùng click `Save` -> Lưu Header Order và các Detail phụ xuống kho và tính tiền, refresh kho.

---

## 5. Class Diagram

- Vẽ **class diagram** cho hệ thống bằng **draw.io**.
- Sơ đồ phải thể hiện:
  - Quan hệ kế thừa
  - Quan hệ association / composition (nếu có)
  - Interface và class implement

📌 **Yêu cầu:**
- Xuất sơ đồ thành file ảnh (PNG hoặc JPG).
- Lưu tại: `docs/class-diagram.png`

---

## 6. Thiết kế lưu trữ dữ liệu (Database / File Design)

Mô tả cách hệ thống lưu trữ dữ liệu.

### 6.1. Hình thức lưu trữ
- Database (MySQL, SQLite, ...)

**Mô tả lý do lựa chọn:**
> Ứng dụng E-Commerce sử dụng CSDL PostgreSQL (Relational Database) qua JDBC. Sử dụng RDBMS cung cấp sự nhất quán dữ liệu ở quy mô lớn (ACID), khả năng ràng buộc dữ liệu kho tốt (Foreign Keys), và tránh xung đột khi có trên 2 user cùng sử dụng ứng dụng chỉnh sửa 1 item.

---

### 6.2. Cấu trúc dữ liệu lưu trữ

Mô tả các bảng / file chính và dữ liệu được lưu trữ.

| Tên bảng / file | Mô tả | Dữ liệu chính |
|----------------|------|--------------|
| `users` | Thông tin account, staff hoặc admin | ID, userName, userPassword, role |
| `items` | Kho sản phẩm của e-commerce | itemId, Tên sp, Số lượng, Giá, Mô tả |
| `customers` | Hồ sơ người mua hàng | ID, name, email, phone, address |
| `orders` | Giao dịch bill chính mua hàng | OrderID, Customer ID, Total, Status, Ngày |
| `order_details`| Lưu hàng mua cụ thể ở giỏ nảo | Detail ID, Order ID, Item ID, Số Lượng, Đơn giáo |

---

## 7. Nhận xét về thiết kế (Optional)

- **Ưu điểm của thiết kế hiện tại:** Các package model, service, repo và interface giao thức được chia cấu trúc sạch đẹp, dễ dọc code ngay từ ngày làm quen đầu tiên.
- **Hạn chế:** Còn tồn đọng code JDBC thủ công ở tầng Repo; nếu code phình to nhiều chức năng thì việc viết chuỗi truy vấn String để CRUD và set các Prepare Statement (ps.setString...) trở nên dài dòng và lặp lại code Boilerplate.

---

## 8. Kết luận

Dự án thiết kế mô hình E-Commerce bán hàng cơ bản được xây dựng dựa trên sự tuân thủ nghiêm ngặt 4 nguyên lý thiết kế cơ bản từ OOP: Encapsulation (gói gọn trạng thái qua Private), Inheritance (kế thừa Swing UI), Polymorphic (đa dạng các triển khai xử lý), Abstraction (ẩn các Logic kết nối Database phức tạp nhờ tầng Services). Nhờ vậy hệ thống hoạt động ổn định và sẵn sàng cho việc mở rộng tính năng mới ở mức độ mã nguồn (Source Corde).

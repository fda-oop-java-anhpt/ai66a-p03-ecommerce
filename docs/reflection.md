## Contribution
- Bui Dang Duong: Exeption + Hash password + Model + Util
- Nguyen Tuan Anh: Authentication + Database Implementation
- Nguyen Thanh Lan: Bussiness Logic + Service
- Le Ngoc Anh Thu: GUI + App tester

## Design Decisions
- **Vì sao dùng Inheritance (kế thừa) thay vì if-else?**
  Thay vì việc phải phân nhánh qua nhiều khối định tuyến `if-else` khổng lồ để xử lý các màn hình và logic khác nhau, dự án này tận dụng tối đa tính Kế thừa từ Java Swing (như `JFrame`, `JDialog`, `JPanel`) lẫn tính Đa hình (Polymorphism) qua các Interface ở tầng Repository / Service.
  Ví dụ tiêu biểu: Cửa sổ chi tiết như `ProfileDialog` hay `OrderDialog` kế thừa trực tiếp từ `JDialog`. Việc sử dụng tính kế thừa cho phép mỗi form tự quản lý sự kiện và vòng đời (lifecycle) độc lập. Hay với việc gọi dữ liệu, chúng ta chỉ gọi qua `IUserService.login()`, không phải dùng lệnh điều kiện `if-else` để xét duyệt từng trường hợp xem cần lấy dữ liệu nào.

- **Phần nào khó nhất?**
  - **Trải nghiệm gỡ lỗi và phân tách mã (Decoupling architecture):** Việc xây dựng các ứng dụng Giao diện bằng Java Swing rất dễ gặp tình trạng gom chung code UI và code truy xuất Database (SQL) vào chung một vị trí (God Class). Việc buộc phải tách rời mã thành các tầng model, repository, service và tiêm (inject) qua constructor là một nỗ lực lớn tốn thời gian.
  - **Quản lí kết nối CSDL và xử lý đồng bộ:** Việc dùng bộ kết nối JDBC thủ công yêu cầu phải luôn đóng/mở tài nguyên sau khi truy xuất (`Connection`, `PreparedStatement`) để chống memory leak. Nếu có lỗi mạng hoặc sai sót truy vấn, toàn bộ luồng hiển thị của ứng dụng có nguy cơ bị sập nếu Exception không bắt đúng cách.
  - **Quản lí luồng UX liên tục qua các Tab / Dialog:** Việc refactor UI, nhất là việc chuyển các chức năng (quản lí giỏ hàng/thanh toán hóa đơn) thành các Popup Modal (`JDialog` thay vì `JFrame`) để buộc người dùng xử lý xong Form hiện tại không thoát nửa vời là một thay đổi kiến trúc vừa trải qua, đòi hỏi độ hiểu biết sâu sắc vòng lặp của sự kiện Swing hạn chế gây block ứng dụng.

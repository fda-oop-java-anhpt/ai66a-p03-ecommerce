# OOP Java Project – Repository Setup Guide

Tài liệu này hướng dẫn **các bước bắt buộc** để sinh viên tạo repository cho project từ **template chính thức của học phần**, cũng như các **quy định về đặt tên, phân quyền và làm việc nhóm**.

⚠️ Không tuân thủ các bước dưới đây sẽ bị trừ điểm project.

---

## 1. Tạo repository từ template (BẮT BUỘC)

### Bước 1: Sử dụng template
1. Truy cập **template repository** của học phần.
2. Nhấn nút **Use this template**.
3. Chọn:
   - **Owner**: GitHub Organization của học phần.
   - **Repository visibility**: Private.

---

### Bước 2: Đặt tên repository (BẮT BUỘC)

Tên repository phải theo **đúng định dạng sau**:
`pXX-project-keyword-teamYY`


Trong đó:

| Thành phần | Ý nghĩa | Ví dụ |
|----------|--------|-------|
| `pXX` | Mã project (01 → 10) | `p01` |
| `project-keyword` | Tên project viết thường, không dấu, dùng dấu `-` | `yummy-catering` |
| `teamYY` | Số nhóm | `team03` |

### Ví dụ hợp lệ
p01-yummy-catering-team03
p04-library-system-team01


---

## 2. Thêm giảng viên vào repository (BẮT BUỘC)

Sau khi tạo repository, nhóm phải **thêm giảng viên làm collaborator với quyền Admin**.

### Các bước:
1. Vào **Settings** → **Collaborators**.
2. Thêm GitHub username của giảng viên: `anhpt204`
3. Chọn quyền **Admin**.
4. Gửi lời mời và đảm bảo giảng viên đã accept.

⚠️ Không thêm giảng viên = coi như **chưa nộp bài**.

---

## 3. Thêm các thành viên trong nhóm

### Yêu cầu:
- Mỗi sinh viên phải có **tài khoản GitHub cá nhân**.
- Không dùng chung tài khoản GitHub.

### Thực hiện:
1. Vào **Settings** → **Collaborators**.
2. Thêm GitHub username của từng thành viên.
3. Cấp quyền **Write** cho các thành viên.

---

## 4. Khai báo thông tin project và nhóm (BẮT BUỘC)

Sau khi tạo repository, nhóm phải cập nhật đầy đủ các file sau:

### 4.1. `docs/project-info.md`
Cần điền đầy đủ:
- Project ID
- Tên project
- Lớp
- Nhóm
- Danh sách thành viên và vai trò

---

### 4.2. `docs/requirements.md`
- Copy **toàn bộ nội dung đề bài project** được giao.
- Không được để trống file này.

---

## 5. Quy định làm việc với Git (BẮT BUỘC)

### 5.1. Commit
- Mỗi thành viên: **tối thiểu 5 commit**.
- Commit message phải rõ ràng, có ý nghĩa.

Ví dụ commit hợp lệ:
```
Add abstract class User
Implement login validation
Refactor OrderService logic
```

Ví dụ commit KHÔNG chấp nhận:
```
update
fix
done
```


---

### 5.2. Thời gian commit
- Không dồn toàn bộ commit vào 1–2 ngày cuối.
- Lịch sử commit sẽ được sử dụng để đánh giá mức độ đóng góp cá nhân.

---

## 6. Sử dụng AI coding tools

Sinh viên **được phép** sử dụng các AI coding tools (ChatGPT, Copilot, v.v.).

### BẮT BUỘC:
- Phải khai báo trung thực trong file: `docs/ai-usage.md`

Nội dung cần ghi:
- AI tool đã sử dụng.
- Prompt mẫu.
- Phần code có AI hỗ trợ.
- Phần code do sinh viên tự viết hoặc chỉnh sửa.

⚠️ Không khai báo AI usage được xem là **vi phạm học thuật**.

---

## 7. Checklist trước khi nộp bài

Trước khi nộp, nhóm phải tự kiểm tra:

- [ ] Repository được tạo từ template chính thức.
- [ ] Tên repository đúng định dạng.
- [ ] Đã thêm giảng viên với quyền Admin.
- [ ] Đã thêm đầy đủ các thành viên trong nhóm.
- [ ] `docs/project-info.md` đã điền đầy đủ.
- [ ] `docs/requirements.md` không để trống.
- [ ] Có commit đều của các thành viên.

---

## 8. Lưu ý quan trọng

- Mỗi nhóm chỉ có **01 repository duy nhất**.
- Không đổi tên repository sau khi đã bắt đầu làm project.
- Mọi chỉnh sửa và trao đổi kỹ thuật phải thể hiện qua **commit trên GitHub**.

---

## 9. Hỗ trợ

Nếu gặp vấn đề trong quá trình tạo repository hoặc phân quyền, liên hệ giảng viên **trước hạn nộp bài**.

Các vấn đề phát sinh do **không đọc kỹ README** sẽ không được giải quyết ngoại lệ.

---

**Repository là sản phẩm học tập chính thức của nhóm.  
Giảng viên sẽ đánh giá dựa trên code, lịch sử commit và khả năng giải thích khi bảo vệ project.**

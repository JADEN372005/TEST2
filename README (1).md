# Quản Lý Nhân Viên & Lương (Android)

Ứng dụng Android quản lý nhân viên, chấm công và tính lương, viết bằng **Kotlin + Jetpack Compose**, lưu dữ liệu cục bộ bằng **SQLite**.

## Mục lục

1. [Giới thiệu](#1-giới-thiệu)
2. [Công nghệ sử dụng](#2-công-nghệ-sử-dụng)
3. [Kiến trúc & cấu trúc thư mục](#3-kiến-trúc--cấu-trúc-thư-mục)
4. [Cơ sở dữ liệu](#4-cơ-sở-dữ-liệu)
5. [Phân chia công việc](#5-phân-chia-công-việc)
6. [Quy trình làm việc với Git](#6-quy-trình-làm-việc-với-git)
7. [Tiến độ](#7-tiến-độ)
8. [Hướng dẫn chạy dự án](#8-hướng-dẫn-chạy-dự-án)

---

## 1. Giới thiệu

Ứng dụng có 2 loại người dùng:

| Vai trò | Chức năng |
|---|---|
| **ADMIN** | Quản lý nhân viên (thêm, sửa, xóa, tìm kiếm); xem lịch sử chấm công toàn công ty; tính lương tự động và duyệt thanh toán |
| **EMPLOYEE** | Check-in / Check-out; xem lịch sử chấm công và bảng lương của chính mình |

## 2. Công nghệ sử dụng

- Ngôn ngữ: Kotlin
- Giao diện: Jetpack Compose, Material 3
- Cơ sở dữ liệu: SQLite (`SQLiteOpenHelper`)
- Lưu phiên đăng nhập: `SharedPreferences`
- Công cụ: Android Studio, Gradle (Kotlin DSL), Git/GitHub

## 3. Kiến trúc & cấu trúc thư mục

Ứng dụng chia thành 3 tầng: **UI → Repository → Database**. Giao diện không tự viết SQL, mọi truy vấn nằm trong `AppRepository`.

```
app/src/main/java/com/example/myapplication/
├── MainActivity.kt                    # Điểm vào, điều hướng theo vai trò
├── data/
│   ├── model/Models.kt                # Account, Employee, WorkLog, Salary, UserSession
│   ├── db/DatabaseHelper.kt           # Tạo 4 bảng SQLite + dữ liệu mẫu
│   ├── repository/AppRepository.kt    # Toàn bộ thao tác với dữ liệu
│   └── session/SessionManager.kt      # Lưu trạng thái đăng nhập
└── ui/
    ├── login/LoginScreen.kt           # Màn hình đăng nhập
    ├── admin/AdminDashboardScreen.kt  # Giao diện Admin (3 tab)
    ├── employee/EmployeeDashboardScreen.kt  # Giao diện Nhân viên
    └── theme/                         # Màu sắc, font, theme
```

## 4. Cơ sở dữ liệu

```
Account (id, username UNIQUE, password, role)
   ▲  accountId
Employee (id, code UNIQUE, fullname, email, phone, hourlyRate, accountId)
   ▲  employeeId                ▲  employeeId
WorkLog (id, employeeId, workDate, checkInTime,   Salary (id, employeeId, month, year,
         checkOutTime, totalHours, status)                 totalHours, overtimeHours,
                                                           totalSalary, status)
```

- `Account` ↔ `Employee` là quan hệ 1-1: Account lo đăng nhập, Employee lo thông tin cá nhân và lương.
- `WorkLog.status`: `CHECKED_IN`, `COMPLETED`.
- `Salary.status`: `PENDING`, `PAID`.

---

## 5. Phân chia công việc

| Thành viên | Họ tên | GitHub | Phụ trách |
|---|---|---|---|
| **TV1** | Đoàn Phan Anh | [@phananhh2805](https://github.com/phananhh2805) | Cơ sở dữ liệu, mô hình dữ liệu & Đăng nhập |
| **TV2** | Trịnh Quốc Toản | [@JADEN372005](https://github.com/JADEN372005) | Quản lý nhân sự & Giao diện quản trị viên |
| **TV3** | Chu Ngọc Giang | [@giangxautrai](https://github.com/giangxautrai) | Nghiệp vụ chấm công & Thuật toán tính lương |

### 👤 Thành viên 1 (Đoàn Phan Anh): Cơ sở dữ liệu, Mô hình dữ liệu & Đăng nhập

**Trọng tâm:** cấu trúc SQLite, lưu phiên bằng `SharedPreferences`, luồng đăng nhập và phân quyền.

| File | Nội dung |
|---|---|
| `data/model/Models.kt` | 5 data class: `Account`, `Employee`, `WorkLog`, `Salary`, `UserSession` |
| `data/db/DatabaseHelper.kt` | Tạo 4 bảng, nạp dữ liệu mẫu (seed data) |
| `data/session/SessionManager.kt` | Lưu / đọc / xóa phiên đăng nhập |
| `ui/login/LoginScreen.kt` | Giao diện đăng nhập, nút đăng nhập nhanh trên máy ảo |
| `MainActivity.kt` | Điều hướng giữa Login, Admin và Employee theo `role` |

**Nội dung báo cáo:**
- Quan hệ giữa 4 bảng (khóa chính, khóa ngoại).
- Cách giữ phiên đăng nhập để không phải đăng nhập lại khi mở lại app.
- Giao diện đăng nhập bằng Compose và phân quyền theo `ADMIN` / `EMPLOYEE`.

### 👤 Thành viên 2 (Trịnh Quốc Toản): Quản lý nhân sự & Giao diện quản trị viên

**Trọng tâm:** CRUD nhân viên (thêm, sửa, xóa, tìm kiếm) và giao diện điều hướng của Admin.

| File | Nội dung |
|---|---|
| `data/repository/AppRepository.kt` _(phần Nhân viên)_ | `getAllEmployees`, `addEmployeeWithAccount`, `updateEmployee`, `deleteEmployee` |
| `ui/admin/AdminDashboardScreen.kt` _(Tab 1)_ | Ô tìm kiếm, `LazyColumn`, dialog Thêm / Sửa / Xóa nhân viên |
| `ui/admin/AdminDashboardScreen.kt` _(Tab 2)_ | Xem toàn bộ lịch sử chấm công của công ty |

**Nội dung báo cáo:**
- Luồng thêm nhân viên: tạo đồng thời bản ghi `Account` và `Employee` trong một transaction.
- Luồng tìm kiếm real-time theo Mã NV hoặc Họ tên (`LIKE`).
- Thiết kế giao diện Admin bằng Compose: `Scaffold`, `NavigationBar`, `Card`, `AlertDialog`.

### 👤 Thành viên 3 (Chu Ngọc Giang): Nghiệp vụ chấm công & Thuật toán tính lương

**Trọng tâm:** Check-in / Check-out, tính tổng giờ làm, giờ tăng ca (OT) và lương hàng tháng.

| File | Nội dung |
|---|---|
| `data/repository/AppRepository.kt` _(phần Chấm công & Lương)_ | `checkIn`, `checkOut`, `calculateHours`, `calculateAndSaveSalary`, `updateSalaryStatus` |
| `ui/employee/EmployeeDashboardScreen.kt` | Tab Chấm công, tab Lịch sử, tab Lương cá nhân |
| `ui/admin/AdminDashboardScreen.kt` _(Tab 3)_ | Tính lương tự động toàn công ty, duyệt `PENDING` → `PAID` |

**Nội dung báo cáo:**
- Quy tắc kiểm tra: chặn check-in trùng ngày, báo lỗi khi check-out mà chưa check-in.
- Thuật toán tính giờ làm từ giờ vào / giờ ra.
- Công thức lương: giờ thường × đơn giá + giờ OT (trên 8 giờ/ngày) × đơn giá × 1.5.

---

## 6. Quy trình làm việc với Git

### Nhánh

| Thành viên | Nhánh |
|---|---|
| TV1 | `feature/tv1-database-auth` |
| TV2 | `feature/tv2-admin-employee` |
| TV3 | `feature/tv3-attendance-salary` |

Không đẩy trực tiếp lên `main`. Mọi thay đổi đi qua **Pull Request**.

### Quy ước commit

Bắt đầu bằng tiền tố của thành viên để nhìn `git log` biết ngay ai làm gì:

```
[TV1] Tao 4 bang SQLite va du lieu mau
[TV2] Man hinh Admin: tab Nhan vien
[TV3] Repository: check-in/check-out
```

### Thứ tự merge

1. Bộ khung dự án (Gradle, Manifest, resources, theme)
2. TV1: Database & Đăng nhập
3. TV2: Quản lý nhân viên & Admin
4. TV3: Chấm công & Lương

> **Lưu ý:** không dùng nút *Add files via upload* trên web GitHub. Chỉ đẩy code bằng `git push` để file đúng thư mục và lịch sử commit đúng tác giả.

---

## 7. Tiến độ

| Hạng mục | Người làm | Trạng thái |
|---|---|---|
| Bộ khung dự án | Trịnh Quốc Toản (chủ repo) | ✅ Hoàn thành |
| Database, Model, Session, Login, MainActivity | Đoàn Phan Anh (TV1) | ⬜ Chưa hoàn thành |
| Repository (Nhân viên) + Admin Tab 1, Tab 2 | Trịnh Quốc Toản (TV2) | ✅ Hoàn thành |
| Repository (Chấm công, Lương) + Employee + Admin Tab 3 | Chu Ngọc Giang (TV3) | ⬜ Chưa hoàn thành |
| Tài liệu từng thành viên (`docs/`) | Cả nhóm | ⬜ Chưa hoàn thành |

---

## 8. Hướng dẫn chạy dự án

1. Cài **Android Studio** (bản mới) và **Git**.
2. Clone repo:
   ```bash
   git clone https://github.com/JADEN372005/TEST2.git
   ```
3. Mở thư mục vừa clone bằng Android Studio (**File → Open**), chờ Gradle sync xong.
4. Chọn máy ảo hoặc thiết bị thật, bấm **Run 'app'**.

### Tài khoản mẫu

| Vai trò | Tên đăng nhập | Mật khẩu |
|---|---|---|
| Admin | `admin` | `admin123` |
| Nhân viên 1 | `emp001` | `123456` |
| Nhân viên 2 | `emp002` | `123456` |

### Kịch bản demo gợi ý

1. **TV1:** đăng nhập bằng tài khoản mẫu, giải thích phiên đăng nhập và phân quyền.
2. **TV2:** đăng nhập `admin`, thêm / sửa / xóa / tìm kiếm nhân viên.
3. **TV3:** đăng nhập `emp001`, Check-in / Check-out; sau đó đăng nhập `admin` bấm Tính lương và duyệt thanh toán.

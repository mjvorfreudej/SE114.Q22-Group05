# TourGo — Ứng dụng đặt Tour & Khách sạn

> Đồ án môn **Nhập môn Phát triển Ứng dụng Di động** — Lớp **SE114.Q22** — **Nhóm 05**, Trường Đại học Công nghệ Thông tin (UIT).

TourGo là ứng dụng Android (Java) cho phép người dùng tìm kiếm, đặt **tour du lịch** và **khách sạn**, thanh toán, đánh giá, nhắn tin với đối tác và nhận thông báo. Ứng dụng hỗ trợ **3 vai trò**: Khách hàng, Đối tác (doanh nghiệp) và Quản trị viên (Admin), kết nối tới một API service riêng (Node.js + Supabase).

---

## 1. Thành viên nhóm

| STT | Họ và tên | MSSV | Vai trò chính | GitHub |
|-----|-----------|------|---------------|--------|
| 1 | Lâm Đoàn Hạ Quyên | 24521498 | Fullstack | [@mjvorfreudej](https://github.com/mjvorfreudej) |
| 2 | Nguyễn Huỳnh Anh Trung | 24521886 | Backend / API, Fullstack | [@trungnha-uit](https://github.com/trungnha-uit) |
| 3 | Nguyễn Thị Ngọc Huyền | 24520714 | Fullstack | [@quackii00](https://github.com/quackii00) |
| 4 | Nguyễn Phương Nam | 24521116 | Fullstack | [@NAMNGUYEN-1012006](https://github.com/NAMNGUYEN-1012006) |
---

## 2. Liên kết mã nguồn (Git)

| Thành phần | Mô tả | Repository |
|------------|-------|------------|
| **Frontend** | Ứng dụng Android (Java) | https://github.com/mjvorfreudej/SE114.Q22-Group05 |
| **Backend** | API service (Node.js + Express + Supabase) | https://github.com/trungnha-uit/TourGo_API_Service |
| **API (đã deploy)** | Dùng cho bản chạy nhanh | https://tourgo-api-service.onrender.com |

---

## 3. Giới thiệu & Tính năng

Ứng dụng được chia theo **3 vai trò**:

- **Khách hàng**
  - Đăng ký / đăng nhập bằng Email, **Google**, **Facebook** (OAuth qua Supabase).
  - Trang chủ, **tìm kiếm & lọc** tour và khách sạn; xem chi tiết (ảnh, tiện ích, vị trí trên bản đồ).
  - **Đặt tour / phòng** (booking), chọn ngày; **thanh toán** chuyển khoản ngân hàng (QR + đối soát tự động qua Casso).
  - **Yêu thích**, viết & xem **đánh giá**.
  - **Nhắn tin** trực tiếp với đối tác (có badge tin chưa đọc) và **thông báo** (chuông + badge số).
  - Quản lý hồ sơ cá nhân.
- **Đối tác (doanh nghiệp)**
  - Đăng ký gian hàng (chờ admin duyệt).
  - Đăng & quản lý tin **tour / khách sạn**, quản lý **lịch trống (calendar)**.
  - Theo dõi **đơn đặt**, xem **đánh giá**, doanh thu & **rút tiền (payout)**, trả lời chat.
- **Quản trị viên (Admin)**
  - Bảng điều khiển tổng quan & hoạt động gần đây.
  - **Duyệt** đăng ký doanh nghiệp / tin đăng chờ duyệt.
  - **Kiểm duyệt & xử lý báo cáo** (báo cáo người dùng, báo cáo đánh giá).
  - Quản lý người dùng và cấu hình hệ thống.

---

## 4. Công nghệ sử dụng

**Frontend (Android)**
- Java, Android SDK (compileSdk **36**, minSdk **29**), Gradle Kotlin DSL.
- Retrofit 2 + OkHttp (gọi REST API), Gson, Glide (ảnh).
- Supabase Auth (Email/Google/Facebook), Google Maps, AndroidX Security (lưu token).

**Backend (API service)**
- Node.js (>= 18), Express 5.
- Supabase (PostgreSQL + Auth + Storage), Multer (upload), Nodemailer (email).

**Hạ tầng**
- API host trên **Render**, cơ sở dữ liệu trên **Supabase**.

---

## 5. Yêu cầu môi trường

| Công cụ | Phiên bản |
|---------|-----------|
| Android Studio | Bản mới (hỗ trợ AGP 9.x) |
| JDK | 11 |
| Android SDK | API 36 (cài qua SDK Manager) |
| Gradle | 9.4.1 (đã có sẵn Gradle Wrapper, không cần cài tay) |

---

## 6. Hướng dẫn chạy source code từ số 0

Ứng dụng đã trỏ sẵn `BASE_URL` tới API trên Render nên **chỉ cần chạy app Android**, không cần dựng server.

1. **Clone** mã nguồn frontend:
   ```bash
   git clone https://github.com/mjvorfreudej/SE114.Q22-Group05.git
   ```
2. Mở thư mục dự án bằng **Android Studio** (`File > Open`).
3. Tạo file **`local.properties`** ở thư mục gốc (file này *không* được commit lên Git) theo mẫu ở [mục 7](#7-mẫu-file-cấu-hình). Cần điền: đường dẫn SDK, khóa Supabase, Google Maps và Google Web Client ID.
4. Bấm **Sync Project with Gradle Files** để tải dependencies.
5. Chọn emulator (hoặc thiết bị thật) rồi bấm **Run ▶**.

> Nếu Gradle báo thiếu SDK, mở **SDK Manager** cài **Android SDK Platform 36**.
>
> Để **đăng nhập Google/Facebook** và **hiện bản đồ**, cần cài keystore dùng chung — xem **mục 7.1**.

---

## 7. Mẫu file cấu hình

### `local.properties` (đặt ở thư mục gốc, KHÔNG commit)
```properties
sdk.dir=C\:\\Users\\<tên-của-bạn>\\AppData\\Local\\Android\\Sdk

# API keys
SUPABASE_URL=https://<project-ref>.supabase.co
SUPABASE_ANON_KEY=<supabase-anon-key>
MAPS_API_KEY=<google-maps-api-key>
GOOGLE_WEB_CLIENT_ID=<google-oauth-web-client-id>

# Keystore dùng chung (xem mục 7.1)
KEYSTORE_FILE=app/tourgo-shared.jks
KEYSTORE_PASSWORD=tourgo123
KEY_ALIAS=tourgo
KEY_PASSWORD=tourgo123
```

> 🔐 **Không commit** `local.properties` (chứa API keys) và file `.jks` lên Git — đã được liệt kê trong `.gitignore`.

### 7.1. Keystore dùng chung (bắt buộc để đăng nhập Google/Facebook & hiện bản đồ)

Google/Facebook login và Google Maps được cấp phép theo **SHA‑1** của chứng chỉ ký app. Vì vậy cả nhóm dùng **chung 1 keystore** để mọi máy build ra cùng một SHA‑1 (đã được whitelist).

1. Xin file **`tourgo-shared.jks`** từ thành viên trong nhóm (gửi qua Drive/Zalo… — **không** đẩy lên Git).
2. Đặt file vào thư mục **`app/`** của project.
3. Thêm 4 dòng `KEYSTORE_*` ở trên vào `local.properties`.
4. **Sync Gradle**, gỡ app cũ trên máy/emulator rồi cài lại.

> Các key khác (Supabase, Maps…) mỗi người tự giữ trong `local.properties` của mình — không cần thay đổi.
> Nếu không có keystore chung, app vẫn build được nhưng **đăng nhập Google/Facebook và bản đồ sẽ không hoạt động** (SHA‑1 không khớp).

---

## 8. Cấu trúc thư mục (rút gọn)

```
SE114.Q22-Group05/                 # Frontend (Android)
├─ app/src/main/java/com/example/tourgo/
│  ├─ ui/        # Màn hình theo vai trò: auth, main, business, admin, chat, notification
│  ├─ remote/    # RetrofitClient, các *Api, interceptor xác thực
│  ├─ data/      # Lưu trữ cục bộ (SessionManager…)
│  ├─ models/    # Lớp dữ liệu (POJO)
│  ├─ adapters/  # Adapter cho RecyclerView
│  └─ utils/     # Tiện ích (ImageLoader, LocaleHelper…)
└─ app/src/main/res/                # Layout, drawable, strings…

TourGo_API_Service/                 # Backend (Node.js)
├─ src/
│  ├─ routes/        # Định nghĩa endpoint
│  ├─ controllers/   # Xử lý nghiệp vụ
│  ├─ middleware/    # Auth, phân quyền, xử lý lỗi, upload
│  ├─ config/        # Khởi tạo Supabase
│  ├─ services/ utils/ constants/
├─ chat_setup.sql    # Script tạo bảng chat
└─ server.js         # Điểm khởi chạy
```

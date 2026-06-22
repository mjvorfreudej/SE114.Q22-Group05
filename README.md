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
  - Duyệt, tìm kiếm và lọc **tour** & **khách sạn**.
  - Đặt chỗ (booking), **thanh toán** qua chuyển khoản ngân hàng (QR + đối soát tự động qua Casso).
  - Lưu **yêu thích**, viết **đánh giá**, **nhắn tin** trực tiếp với đối tác, nhận **thông báo**.
- **Đối tác (doanh nghiệp)**
  - Đăng ký gian hàng, quản lý tour/khách sạn, đơn đặt, doanh thu & rút tiền (payout), trả lời chat.
- **Quản trị viên (Admin)**
  - Quản lý người dùng, đối tác, duyệt đăng ký, cấu hình hệ thống và các màn hình quản trị.

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

---

## 7. Mẫu file cấu hình

### `local.properties` (Frontend — đặt ở thư mục gốc, KHÔNG commit)
```properties
sdk.dir=C\:\\Users\\<tên-của-bạn>\\AppData\\Local\\Android\\Sdk

SUPABASE_URL=https://<project-ref>.supabase.co
SUPABASE_ANON_KEY=<supabase-anon-key>
MAPS_API_KEY=<google-maps-api-key>
GOOGLE_WEB_CLIENT_ID=<google-oauth-web-client-id>
```

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

# 🎯 Scope v1: Tổng quan Chức năng (Features)

* **Authentication & User Profile:** Đăng ký, đăng nhập, quản lý thông tin cá nhân/địa chỉ.
* **Product Catalog:** Danh sách sản phẩm, bộ lọc, tìm kiếm, chi tiết sản phẩm.
* **Cart & Checkout:** Giỏ hàng, áp mã giảm giá, đặt hàng, tích hợp cổng thanh toán trực tuyến & COD.
* **Order Management (Customer):** Lịch sử đơn hàng, theo dõi trạng thái đơn.
* **Admin Dashboard (Basic):** Quản lý sản phẩm, quản lý đơn hàng, xem báo cáo doanh thu cơ bản.

---

# 🗓️ Lộ trình Phát triển: Sprint 1 đến Sprint 4

> **Giả định:** Mỗi Sprint kéo dài 2 tuần. Đội ngũ bao gồm BA, UI/UX, Dev (FE/BE), QA và PM.

---

## 🔷 Sprint 1: Nền tảng System, Authentication & Catalog Cơ bản

**Mục tiêu:** Dựng framework, database và hoàn thiện giao diện/tính năng cho người dùng xem sản phẩm.

### 1. Feature: Quản lý Tài khoản (Auth)

* **AC 1.1 (Đăng ký/Đăng nhập):**
    * Người dùng có thể đăng ký tài khoản bằng Email và Mật khẩu.
    * Hệ thống kiểm tra định dạng Email và độ mạnh mật khẩu (tối thiểu 8 ký tự).
    * Người dùng có thể đăng nhập bằng Email/Password đã đăng ký.
* **AC 1.2 (Quên mật khẩu):**
    * Gửi link reset mật khẩu qua Email khi người dùng yêu cầu.

### 2. Feature: Danh mục & Chi tiết Sản phẩm (Product Catalog)

* **AC 2.1 (Danh sách sản phẩm):**
    * Hiển thị danh sách sản phẩm dạng Grid có: Ảnh đại diện, Tên, Giá bán, Giá gạch (nếu có giảm giá).
    * Có phân trang (Pagination) hoặc Tải thêm (Load more) - tối đa 20 sản phẩm/trang.
* **AC 2.2 (Trang chi tiết sản phẩm - PDP):**
    * Hiển thị gallery ảnh sản phẩm, tên, giá, mô tả ngắn, mô tả chi tiết, số lượng tồn kho.
    * Cho phép chọn Variant (Size, Màu sắc). Giá và hình ảnh phải cập nhật tương ứng theo Variant được chọn.

---

## 🔷 Sprint 2: Tìm kiếm, Giỏ hàng & Admin Quản lý Sản phẩm

**Mục tiêu:** Cho phép người dùng tìm kiếm, đưa đồ vào giỏ và xây dựng trang Admin để Client có thể nhập dữ liệu sản
phẩm.

### 1. Feature: Tìm kiếm & Lọc sản phẩm (Search & Filter)

* **AC 1.1 (Tìm kiếm):**
    * Thanh tìm kiếm (Search bar) trả về kết quả theo Tên sản phẩm khi bấm Enter hoặc icon Search.
* **AC 1.2 (Bộ lọc):**
    * Lọc sản phẩm theo Category (Danh mục), Khoảng giá (Price range).
    * Sắp xếp theo: Giá tăng/giảm, Mới nhất.

### 2. Feature: Giỏ hàng (Shopping Cart)

* **AC 2.1 (Thao tác giỏ hàng):**
    * Người dùng (kể cả Guest chưa đăng nhập) có thể "Thêm vào giỏ hàng" từ trang chi tiết.
    * Cập nhật số lượng sản phẩm trong giỏ (tăng/giảm/xóa). Không cho phép tăng quá số lượng tồn kho.
    * Tự động tính toán: Tạm tính (Subtotal), Tổng tiền (Total).

### 3. Feature (Admin): Quản lý Sản phẩm (Product Management)

* **AC 3.1 (CRUD Sản phẩm):**
    * Admin có thể Tạo mới / Chỉnh sửa / Xóa / Ẩn-Hiện sản phẩm.
    * Admin có thể upload nhiều hình ảnh cho 1 sản phẩm.

---

## 🔷 Sprint 3: Luồng Đặt hàng (Checkout) & Thanh toán

**Mục tiêu:** Cốt lõi của E-commerce — Chuyển đổi từ Giỏ hàng thành Đơn hàng thành công.

### 1. Feature: Quy trình Đặt hàng (Checkout Flow)

* **AC 1.1 (Thông tin giao hàng):**
    * Thu thập thông tin: Họ tên, Số điện thoại, Địa chỉ nhận hàng (Tỉnh/Thành, Quận/Huyện, Phường/Xã).
    * Lưu danh sách địa chỉ nhận hàng vào Profile của User đăng nhập.
* **AC 1.2 (Mã giảm giá - Voucher):**
    * Cho phép nhập Coupon code tại bước Checkout. Hệ thống kiểm tra điều kiện và trừ tiền trực tiếp vào đơn hàng.

### 2. Feature: Thanh toán (Payment Integration)

* **AC 2.1 (Phương thức thanh toán):**
    * Hỗ trợ thanh toán COD (Nhận hàng trả tiền).
    * Tích hợp 01 Cổng thanh toán trực tuyến (ví dụ: VNPay / Momo / Stripe / Paypal tùy thị trường target).
* **AC 2.2 (Xác nhận đơn hàng):**
    * Sau khi đặt hàng thành công: Trả về trang "Order Success", trừ số lượng tồn kho của sản phẩm, gửi Email xác nhận
      đơn hàng tự động cho khách hàng.

---

## 🔷 Sprint 4: Admin Quản lý Đơn hàng, Lịch sử Mua hàng & Hardening/Release

**Mục tiêu:** Hoàn thiện luồng quản trị cho Client, xử lý bug, tối ưu hiệu năng và chuẩn bị Go-Live v1.

### 1. Feature (Customer): Lịch sử & Trạng thái Đơn hàng

* **AC 1.1 (Order History):**
    * User đăng nhập có thể xem danh sách đơn hàng đã đặt và trạng thái tương ứng (`Pending`, `Processing`, `Shipping`,
      `Completed`, `Cancelled`).
    * User xem được chi tiết từng đơn hàng.

### 2. Feature (Admin): Quản lý Đơn hàng (Order Management)

* **AC 2.1 (Xử lý đơn hàng):**
    * Admin xem danh sách đơn hàng toàn hệ thống, lọc theo trạng thái/ngày tháng/mã đơn.
    * Admin có quyền chuyển trạng thái đơn hàng (VD: *Xác nhận đơn -> Đang giao -> Đã hoàn thành / Hủy đơn*).
* **AC 2.2 (Báo cáo cơ bản):**
    * Dashboard hiển thị: Tổng doanh thu, Số lượng đơn hàng mới, Sản phẩm bán chạy nhất trong khoảng thời gian chọn.

### 3. Hardening & Launch Preparation (Cuối Sprint 4)

* **UAT (User Acceptance Testing):** Client nghiệm thu toàn bộ luồng.
* **Security & Performance:** Kiểm tra bảo mật cơ bản, tối ưu tốc độ load trang (< 3s).
* **Deployment:** Cấu hình Production environment, trỏ Domain, SSL và Go-Live v1.
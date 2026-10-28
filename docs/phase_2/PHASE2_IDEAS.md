# Ý tưởng cải tiến phase 2

## 1. Thêm Observability

**Lý do đề xuất:**

Khi hệ thống có nhiều người dùng, lỗi có thể đến từ API, cơ sở dữ liệu, MinIO hoặc quá tải tài nguyên. Nếu chỉ xem log thủ công thì khó phát hiện sớm nguyên nhân và đánh giá mức độ ảnh hưởng.

**Có thể cải tiến:**

Theo dõi trạng thái hoạt động của hệ thống, tốc độ phản hồi API, tỷ lệ lỗi, mức sử dụng tài nguyên. Nhờ đó nhóm có thể phát hiện sự cố sớm, xác định điểm nghẽn và rút ngắn thời gian khôi phục.

## 2. Thêm Redis Cache cho API công việc

**Lý do đề xuất:**

Các API xem danh sách, tìm kiếm và xem chi tiết công việc là các chức năng được gọi thường xuyên. Nếu mọi request đều truy vấn trực tiếp MySQL, tải lên database sẽ tăng nhanh khi số lượng người dùng đồng thời lớn.

**Có thể cải tiến:**

Lưu tạm các dữ liệu việc làm được truy cập nhiều để giảm số lần đọc từ MySQL. Điều này giúp phản hồi nhanh hơn, giảm tải cho database và hỗ trợ hệ thống phục vụ nhiều người dùng hơn trong cùng một thời điểm.

## 3. Tối ưu tìm kiếm việc làm

**Lý do đề xuất:**

Khi số lượng tin tuyển dụng tăng, việc lọc theo từ khóa, địa điểm, cấp bậc hoặc mức lương có thể trở nên chậm. Đây là chức năng cốt lõi của hệ thống vì ứng viên chủ yếu bắt đầu bằng việc tìm kiếm công việc phù hợp.

**Có thể cải tiến:**

- Thêm index và tối ưu truy vấn MySQL cho các điều kiện tìm kiếm phổ biến như địa điểm, cấp bậc, danh mục và trạng thái công việc. Áp dụng phân trang hiệu quả;

- Dùng Full-text Search hoặc Elasticsearch/OpenSearch khi cần tìm kiếm từ khóa trên lượng dữ liệu lớn.

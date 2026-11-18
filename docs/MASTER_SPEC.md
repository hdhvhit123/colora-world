# COLORA WORLD — MASTER SPEC CHO CLAUDE CODE

**Phiên bản:** 1.0 · **Ngày:** 17/11/2026 · **Ngôn ngữ đặc tả:** Tiếng Việt
**Sản phẩm:** Ứng dụng Android tô màu, sáng tạo và chơi với thế giới do trẻ xây dựng.
**Mục tiêu kinh doanh:** Hướng tới 10 triệu lượt tải toàn cầu. Đây là mục tiêu, không phải cam kết hay dự báo.
**Ưu tiên của chủ sản phẩm:** ĐẸP, BẮT MẮT, SINH ĐỘNG, DỄ DÙNG; nội dung phong phú; trẻ muốn quay lại sáng tạo qua nhiều tháng; phần chơi cốt lõi offline.

> Đây là đặc tả triển khai sản phẩm đầy đủ, không phải yêu cầu chỉ làm landing page, bộ mockup hoặc prototype. Hãy triển khai bằng mã nguồn chạy được, tài nguyên thật, lưu dữ liệu thật và chức năng hoạt động xuyên suốt. Thực hiện theo các mốc để kiểm soát chất lượng, nhưng không tự coi mốc thử nghiệm là hoàn thành toàn bộ yêu cầu.

## Mục lục

1. [Chỉ dẫn thực thi cho Claude](#s01)
2. [Tầm nhìn, đối tượng và phạm vi hoàn thành](#s02)
3. [Hệ thống mỹ thuật và thiết kế giao diện](#s03)
4. [Luồng sử dụng và đặc tả từng màn hình](#s04)
5. [Bộ công cụ tô màu](#s05)
6. [Biến tác phẩm thành đồ chơi](#s06)
7. [Thế giới tương tác của bé](#s07)
8. [Câu chuyện, sáng tạo và giữ chân](#s08)
9. [Kho 14 chủ đề và 168 mẫu tranh](#s09)
10. [Danh mục 28 câu chuyện](#s10)
11. [Danh mục 30 gợi ý sáng tạo](#s11)
12. [Tạo nhân vật, thử màu và sách của bé](#s12)
13. [Âm thanh, chuyển động và khả năng tiếp cận](#s13)
14. [Phụ huynh, quyền riêng tư và thời lượng](#s14)
15. [Kiến trúc Android và xử lý offline](#s15)
16. [Mô hình dữ liệu và hợp đồng nội dung](#s16)
17. [Lưu, khôi phục, sao lưu và xuất tác phẩm](#s17)
18. [Hiệu năng và tương thích thiết bị](#s18)
19. [Kinh doanh và mua nội dung](#s19)
20. [Quốc tế hóa và hướng tới 10 triệu lượt tải](#s20)
21. [Quy trình sản xuất tài nguyên](#s21)
22. [Lộ trình triển khai và bàn giao](#s22)
23. [Kiểm thử, nghiệm thu và Definition of Done](#s23)
24. [Rủi ro, quyết định và những việc không được làm](#s24)
25. [Nguồn tham khảo và prompt khởi động](#s25)

<a id="s01"></a>

## 1\. Chỉ dẫn thực thi cho Claude

### 1.1. Vai trò và cách làm

Đóng vai một nhóm chuyên môn trong cùng quy trình làm việc: Android engineer, game interaction designer, UI/UX designer, art director, content designer và QA. Điều này mô tả năng lực cần vận dụng; không yêu cầu công cụ đa tác nhân.

* Đọc toàn bộ tài liệu trước khi quyết định kiến trúc.
* Nếu có repository: đọc README, AGENTS.md, cấu hình build, cấu trúc assets, git status và các màn hình hiện tại; giữ lại công việc của người dùng.
* Nếu chưa có repository: tạo dự án Android thật với Gradle Wrapper, cấu hình module và tài nguyên có thể build.
* Chốt một kiến trúc nhất quán; không cùng lúc dựng hai renderer hoặc hai bộ lưu trạng thái cạnh tranh.
* Tự quyết định các chi tiết thông thường và ghi vào docs/DECISIONS.md. Chỉ cần hỏi khi thiếu tài khoản, khóa ký, quyền thương mại, chi phí hoặc quyết định không thể suy ra.
* Làm từng lát cắt hoàn chỉnh: chọn tranh → tô → lưu → hoạt hóa → đưa vào thế giới → đóng mở lại → vẫn còn dữ liệu.
* Sau mỗi mốc, cập nhật docs/IMPLEMENTATION\_STATUS.md: mã yêu cầu, trạng thái, bằng chứng, lỗi còn lại, việc kế tiếp.
* Nếu ngữ cảnh sắp hết, ghi checkpoint đủ để phiên tiếp theo làm tiếp, không bắt đầu lại.
* Không tự publish, mua tài sản, tạo tài khoản dịch vụ hoặc kích hoạt thanh toán thật chỉ vì tài liệu có kế hoạch phát hành.
* Không gọi một việc là “đã kiểm thử” nếu chưa chạy. Ghi rõ máy/emulator, lệnh, phiên bản và kết quả.
* Không báo “hoàn thành” khi còn nút giả, dữ liệu placeholder, tranh trùng lặp giả số lượng hoặc mua hàng mô phỏng trong release.

### 1.2. Thứ tự ưu tiên khi phải đánh đổi

1. Trẻ không mất tác phẩm; dữ liệu và tương tác phải đúng.
2. Giao diện đẹp, tài nguyên mỹ thuật nhất quán, thao tác dễ hiểu.
3. Tô màu mượt, giữ nguyên tác phẩm khi chuyển sang chơi.
4. Thế giới và câu chuyện có chiều sâu sử dụng lại.
5. Nội dung phong phú và mở rộng dễ.
6. Thương mại hóa và tính năng phân phối.

Không đánh đổi khả năng sử dụng bằng hiệu ứng dày đặc. Không tự giảm chất lượng mỹ thuật để chạy theo số màn hình.

### 1.3. Quy ước yêu cầu

* **MUST:** bắt buộc cho bản FULL.
* **SHOULD:** thực hiện trừ khi có lý do cụ thể, ghi rõ.
* **LATER:** mở rộng tương lai; không giả vờ đã có.
* Các mốc VS, LAUNCH, FULL là thứ tự triển khai. Chỉ FULL đáp ứng toàn bộ đặc tả sản phẩm.
* Các giá trị hiệu năng, tỷ lệ chuyển đổi và mức giá trong tài liệu là mục tiêu thiết kế/thử nghiệm, không phải kết quả đo thực tế.
* Phạm vi bàn giao mặc định: FULL offline có đầy đủ nội dung; tích hợp mua hàng nằm trong build riêng và chỉ phát hành thương mại khi cấu hình thật được cung cấp.

<a id="s02"></a>

## 2\. Tầm nhìn, đối tượng và phạm vi hoàn thành

### 2.1. Ý tưởng cốt lõi

**“Con tô màu, thế giới của con sống dậy.”**

Bé tô mèo tím, hoa cầu vồng, xe màu cam. Những tác phẩm giữ đúng màu và nét của bé; được đưa vào một khu vườn hoặc khu phố, tương tác với nhau và tham gia những câu chuyện nhỏ.

Ba tầng giá trị:

* Tức thì: thao tác tô đem lại phản hồi trực tiếp, dễ hiểu.
* Trung hạn: hoàn thành một cảnh hoặc câu chuyện bằng tác phẩm của mình.
* Dài hạn: phát triển thế giới, bộ sưu tập và sách sáng tạo riêng.

Không chấm tranh theo “đẹp/xấu”, không bắt tô theo màu thực tế trong chế độ sáng tạo.

### 2.2. Đối tượng

|Chế độ|Nhóm tuổi định hướng|Cách dùng|
|-|-|-|
|Little Artist|3–4|Ít nút, vùng tô lớn, 8 màu đầu, tự chặn tô ngoài vùng|
|Creative Explorer|5–6|Cọ, 16 màu, sticker, dựng cảnh và câu chuyện|
|World Maker|7–8|24 màu + bảng mở rộng, tranh chi tiết, thiết kế nhân vật và sách|

Nhóm tuổi là giả định thiết kế cần kiểm chứng. Phụ huynh chọn mức kỹ năng; không cần ngày sinh. Có thể đổi mức mà không mất tác phẩm hay quyền truy cập. Tất cả trẻ được tiếp cận các chủ đề, không chia chủ đề theo giới tính.

### 2.3. Phạm vi nội dung chốt

|Mốc|Mẫu tranh thực sự khác nhau|Chủ đề|Bối cảnh|Câu chuyện|
|-|-:|-:|-:|-:|
|VS — lát cắt thử nghiệm|12|3|1|1|
|LAUNCH — bản phát hành đầu|72|6|2|12|
|FULL — mục tiêu bàn giao|168|14|7|28|
|LATER|300+|Theo phản hồi|Mở rộng|Theo phản hồi|

VS dùng 4 mẫu đầu của animals, flowers, vehicles. LAUNCH dùng đủ 12 mẫu trong animals, flowers, nature, family, machines, vehicles. FULL dùng toàn bộ danh mục ở mục 9.

FULL còn bao gồm: 30 gợi ý sáng tạo; trình tạo nhân vật; phòng thử màu; sách 4–12 trang; xuất PNG/PDF/MP4; nhiều hồ sơ trên máy; sao lưu/khôi phục; 2 ngôn ngữ hoàn chỉnh EN/VI; lựa chọn giảm chuyển động.

### 2.4. Tiêu chí khác biệt

* Mỗi tác phẩm là tài sản có thể tái sử dụng, không biến mất sau khi “xong”.
* Các chủ đề tương tác xuyên suốt: máy xúc xây vườn, xe chở thỏ, gia đình dã ngoại dưới cầu vồng.
* Tô lại tạo phiên bản; không tự thay màu bản đã dùng trong cảnh.
* Mọi mẫu có ít nhất một phản hồi khi chạm; không nhất thiết tất cả đều có hoạt ảnh phức tạp.
* Không cần generative AI, máy chủ suy luận, đăng nhập hoặc Internet để chơi.
* Hệ thống cho phép chơi tự do ngay từ đầu; câu chuyện là lời gợi ý, không phải hàng rào khóa sáng tạo.

<a id="s03"></a>

## 3\. Hệ thống mỹ thuật và thiết kế giao diện — ưu tiên cao nhất

### 3.1. Art direction

Phong cách: **sách tranh tương tác cao cấp, 2D có chiều sâu nhẹ**, nhân vật tròn trịa, nét rõ, biểu cảm ấm áp, bề mặt hơi giống giấy và sáp màu. Cảm giác là một hộp đồ chơi được chăm chút, không phải dashboard có nhiều ô nội dung.

* Đồ họa gốc, có hệ thống nét và tỷ lệ thống nhất.
* Cảnh nền chia 3 lớp: xa, trung, tiền cảnh; parallax nhẹ khi có thao tác.
* Nội dung chính nổi bật; nền sáng dịu, không tranh giành chú ý với tranh.
* Tối đa 2–3 điểm chuyển động nhẹ trong màn hình home cùng lúc.
* Có bóng tiếp xúc và độ sâu vừa phải; không dùng blur lớn trên mọi thành phần.
* Không dùng emoji làm icon chính, hình stock lẫn phong cách, nhân vật có bản quyền hoặc placeholder hình học làm tài nguyên cuối.
* Không biến tất cả động vật thành cùng một thân hình rồi đổi tai/màu. Mỗi mẫu phải đọc được silhouette và đặc trưng riêng.
* Dùng “vui, tò mò, đáng yêu”; tránh chớp giật, gào thét, hiệu ứng thắng thưởng kiểu casino.

### 3.2. Bộ màu thiết kế

|Token|Giá trị gợi ý|Vai trò|
|-|-|-|
|bgCream|#FFF8EF|Nền ứng dụng|
|paperWhite|#FFFFFF|Trang tô và thẻ|
|inkNavy|#24324B|Chữ, viền và icon chức năng|
|brandCoral|#FF786D|Điểm nhấn, nút tô|
|brandTurquoise|#42C8BD|Thế giới, điểm nhấn phụ|
|sunshine|#FFD66B|Ngôi sao, gợi ý|
|lavender|#B8A0F3|Bộ sưu tập và tưởng tượng|
|skyBlue|#8BD4F6|Bầu trời và bối cảnh|
|leafGreen|#9DDC86|Thiên nhiên|
|softRose|#FFE6E0|Mảng nền phụ|
|focusDark|#34548D|Focus/selected outline|

Đây là palette khởi điểm. Kiểm tra contrast trên tổ hợp thực tế; mặc định chữ inkNavy trên nền pastel. Không dùng chữ trắng nhỏ trên vàng/coral/turquoise khi chưa đo tương phản.

Bảng màu của bé tách khỏi màu UI: có đen, trắng, nâu, xám, các màu cơ bản, nhiều màu da, pastel và palette mở rộng. Nút màu trắng có viền để không biến mất trên giấy.

### 3.3. Typography và khoảng cách

* Chọn font rounded có giấy phép phù hợp, hỗ trợ tiếng Việt; ưu tiên kiểm tra Nunito và Noto Sans. Bundle font local và giấy phép, không tải font khi runtime.
* Chữ tiêu đề gợi ý 24–32sp; nhãn 16–20sp; parent area 14–18sp, tôn trọng font scaling.
* Nhãn ngắn, câu rõ nghĩa; hỗ trợ text expansion 40%.
* Vùng chạm chính tối thiểu 56dp, nút chủ đạo 64–80dp; khoảng cách 8–12dp.
* Swatch có hit target tối thiểu 48dp; số swatch trên một hàng phụ thuộc chiều rộng thực.
* Radius: thẻ 24dp, panel 28–32dp, nút 18–24dp; không bo tròn mọi thứ giống nhau.
* Shadow có tối đa 2 cấp. Token dùng thống nhất, không hard-code tùy màn hình.

### 3.4. Linh vật

Tên tạm **Milo**, một giọt màu nhỏ hình mềm, có 2 mắt và tay ngắn. Thiết kế original; không sao chép mascot có sẵn.

Bộ biểu cảm: chào, tò mò, vui nhẹ, hướng dẫn, đang lắng nghe, nghỉ ngơi. Milo xuất hiện ở home, gợi ý đầu tiên và kết thúc câu chuyện; không che canvas, không liên tục thúc giục.

Lời thoại trung tính: “Con muốn thử màu nào?”, “Mình cất tranh ở đây nhé!”. Không nói “Con bỏ rơi mình”, “Mình buồn vì con không chơi”.

### 3.5. Thiết kế tranh tô và đồ chơi

* Kích thước thiết kế chuẩn 1024×1024; dùng tọa độ local độc lập thiết bị.
* Viền ngoài chuẩn khoảng 10–14 đơn vị; viền trong 6–10 đơn vị, điều chỉnh theo silhouette.
* 3 cấp chi tiết: simple 4–8 vùng lớn; medium 8–18; detailed 18–35 vùng.
* Cấp chi tiết không nhân số lượng mẫu. Một con mèo có 3 mức vẫn tính là 1 mẫu nội dung.
* Các chi tiết mặt quan trọng nằm ở lớp outline cố định phía trên màu, không bị fill che mất.
* Vùng tô khép kín, không khe hở; silhouette và mask khớp; không có vùng cực nhỏ khiến trẻ bực.
* Đồ chơi nhiều bộ phận phải có overlap tại khớp, pivot đúng và vùng dự phòng để không lộ khoảng trắng khi chuyển động.
* Đồ vật phải dễ nhận diện trên cả tranh trắng lẫn phiên bản tô đậm.
* Thumbnails thể hiện chính nội dung và mức độ, không quảng cáo một tranh khác.

### 3.6. Motion design

|Tương tác|Mục tiêu|
|-|-|
|Nhấn nút|Scale 0.97 rồi trở lại trong khoảng 100–160ms|
|Chọn màu|Vòng chọn và tick; không chỉ đổi hue|
|Đổ màu|Phản hồi ngay, hiệu ứng lan nhẹ 120–220ms nếu không ảnh hưởng input|
|Chuyển màn|180–280ms, có thể bỏ qua khi giảm chuyển động|
|Hoạt hóa tác phẩm|0.8–1.5 giây; được chạm bỏ qua|
|Kết thúc câu chuyện|1–2 giây, ít hạt trang trí, không khóa thao tác|
|Idle nhân vật|Nhẹ, không tất cả cùng nhảy liên tục|

Reduced motion loại parallax, rung màn hình, hạt và bounce mạnh; vẫn giữ phản hồi tĩnh rõ ràng.

### 3.7. Cổng chất lượng hình ảnh

Trước khi nhân rộng nội dung, tạo style board thực và 6 màn hình chuẩn: Home, Theme Library, Coloring Studio, Toy Reveal, World, Parent Area.

Chụp screenshot từ app chạy thật tại điện thoại dọc, điện thoại ngang và tablet. Tự đánh giá thang 1–5 cho: nhất quán, silhouette, thứ bậc, màu, khoảng cách, cảm giác đồ chơi, khả năng đọc, trạng thái tương tác. Mục tiêu nội bộ ≥4/5 từng tiêu chí; không coi điểm tự chấm là bằng chứng người dùng thích.

Sửa các lỗi như thẻ quá nhỏ, nền rối, button mặc định lạc phong cách, viền nhòe, vật thể nổi không có bóng, icon khó hiểu trước khi thêm chủ đề.

<a id="s04"></a>

## 4\. Luồng sử dụng và từng màn hình

### 4.1. Luồng lần đầu

Cold start → màn giới thiệu ngắn có thể bỏ qua → chọn mèo/hoa/xe → tô ngay → chọn “Chơi” → xem tác phẩm hoạt động → thêm vào vườn → tự lưu.

Tạo hồ sơ local mặc định bằng avatar, không hỏi tên/ngày sinh/email. Chọn chế độ mặc định Creative Explorer; phụ huynh có thể đổi sau. Không chặn lần tô đầu bằng paywall, permission hoặc tutorial dài.

Mục tiêu thử nghiệm: phần lớn trẻ trong nhóm thử có thể tạo một tác phẩm chơi được trong khoảng 60–90 giây với hướng dẫn tối thiểu. Đây là mục tiêu cần đo.

### 4.2. Hệ thống điều hướng

Ba đích chính: **Tô màu / Thế giới / Bộ sưu tập**. Câu chuyện truy cập từ home và thế giới; Phòng màu và Tạo nhân vật nằm trong Tô màu; Sách của bé nằm trong Bộ sưu tập. Khu phụ huynh ở góc, có cổng bảo vệ.

Back luôn có nghĩa rõ ràng; đang tô thì tự lưu và quay lại, không hỏi lưu liên tục. Xóa hoặc ghi đè cần flow riêng. Điều hướng không tự reset cảnh.

### 4.3. Danh mục màn hình

|ID|Màn hình|Bố cục và hành vi bắt buộc|
|-|-|-|
|UI-01|Splash|Logo nhỏ và nền nhẹ; không intro dài; tải dữ liệu tối thiểu, xử lý lỗi khôi phục|
|UI-02|Lần chơi đầu|Ba thẻ lớn mèo/hoa/xe, một lời nhắc; nút chơi ngay|
|UI-03|Home|Hero minh họa có cảnh gần đây; CTA “Tô màu”; hai thẻ “Thế giới” và “Bộ sưu tập”; gợi ý nhẹ|
|UI-04|Chọn chủ đề|Thẻ có minh họa gốc, nhãn ngắn; lọc theo biểu tượng; không dồn 14 chủ đề vào một màn|
|UI-05|Chọn tranh|Grid thumbnail lớn; chỉ báo đã tô, yêu thích, mức chi tiết; mở bản cũ hoặc bản mới|
|UI-06|Studio tô màu|Giấy ở trung tâm, palette, tool tray, undo/redo, “Chơi”; không banner|
|UI-07|Tác phẩm sống dậy|Nền sân khấu nhẹ; tương tác ngay; “Đưa vào thế giới”, “Tô tiếp”; có skip|
|UI-08|Chọn thế giới|Bảy bối cảnh minh họa; hiển thị bản gần đây và tạo cảnh mới|
|UI-09|Chơi trong thế giới|Cảnh chiếm ưu thế; chuyển Chơi/Trang trí rõ; khay đồ chơi thu gọn; auto-save|
|UI-10|Chọn câu chuyện|Thẻ 3–5 bước, preview hình vật thể; cho phép dùng tác phẩm sẵn có|
|UI-11|Trong câu chuyện|Một gợi ý tại một thời điểm; progress bằng hình; bỏ qua/thoát vẫn lưu|
|UI-12|Phòng màu|Khay phối, chọn chất liệu, thử trên giấy; reset rõ; không quiz bắt buộc|
|UI-13|Tạo nhân vật|Preview lớn, nhóm bộ phận, màu; nút chơi thử và lưu|
|UI-14|Bộ sưu tập|Tranh, nhân vật, cảnh, sách; tìm bằng chủ đề/avatar; thao tác phục hồi xóa|
|UI-15|Sách của bé|Bìa, trang, khay nội dung; sắp xếp trang; preview lật trang|
|UI-16|Nghỉ và lưu|Xác nhận “Tranh đã được cất”; kết thúc nhẹ; phụ huynh có thể gia hạn|
|UI-17|Cổng phụ huynh|Giữ nút để vào bước xác thực; mã PIN khi đã thiết lập; không lộ parent area khi back|
|UI-18|Khu phụ huynh|Hồ sơ, âm thanh, mức kỹ năng, thời lượng, ngôn ngữ, lưu/xuất, mua hàng, hỗ trợ|
|UI-19|Xuất và sao lưu|Preview, lựa chọn định dạng, dung lượng, tiến trình, cancel, thông báo kết quả thật|
|UI-20|Nội dung mở rộng|Chỉ ở parent area; nội dung/giá/trạng thái thật; khôi phục mua; không CTA thương mại ở canvas|

### 4.4. Studio theo kích thước màn hình

* Điện thoại dọc: thanh top mỏng; canvas ở giữa; 1 hàng tool; palette phía dưới có thể cuộn có chỉ dấu.
* Điện thoại ngang/tablet: canvas lớn; tool rail bên trái; palette bên phải hoặc dưới tùy không gian.
* Tôn trọng system bars, cutout và vùng gesture; không đặt nút xóa sát mép.
* Canvas letterbox nếu cần, không kéo méo tranh. Đổi hướng giữ camera, công cụ, tác phẩm và undo.
* Nút “Chơi” dùng icon chuyển động + nhãn, dễ thấy nhưng không che nội dung.
* Trẻ nhỏ: chỉ hiện fill, cọ, tẩy, undo, palette, chơi. Công cụ nâng cao mở theo chế độ.
* Gesture một ngón để tô; hai ngón để pan/zoom. Chế độ khóa camera cho trẻ nhỏ; nút Fit có mặt khi zoom.

### 4.5. Trạng thái đặc biệt

Mọi màn hình có trạng thái loading, empty, error, normal phù hợp. Empty state là minh họa + một hành động rõ. Không spinner vô hạn.

* Không có Internet: chơi bình thường; chỉ chức năng cần mạng trong parent area thông báo rõ.
* Mất asset: không crash; hiện nội dung khác, log lỗi nội bộ và có đường quay về.
* Lưu lỗi/ổ đầy: giữ tác phẩm trong RAM; thông báo cho phụ huynh; cho thử lại hoặc xuất nếu có nơi lưu; không giả “đã lưu”.
* Parent gate hủy: về đúng màn trước đó, không bật share/purchase.
* Nhấn nhanh nhiều lần: chống tạo trùng tác phẩm/câu chuyện/purchase flow.
* Process death: phục hồi bản commit gần nhất và hiện tranh bình thường; không trở lại onboarding.

<a id="s05"></a>

## 5\. Bộ công cụ tô màu

### 5.1. Công cụ bắt buộc

|ID|Công cụ|Hành vi|
|-|-|-|
|COL-01|Chạm đổ màu|Hit-test region; thay màu nền của vùng, giữ lớp nét bên trên|
|COL-02|Bút sáp|Nét texture nhẹ, độ dày ổn định theo tọa độ tranh|
|COL-03|Bút dạ|Nét mịn, opacity cao, cap/join tròn|
|COL-04|Màu nước|Alpha/layering có kiểm soát; không giả lập chất lỏng tốn tài nguyên|
|COL-05|Cọ cầu vồng|Dải màu deterministic theo độ dài nét; lưu seed|
|COL-06|Cọ họa tiết|Chấm, sọc, sao, tim, caro; clip trong vùng khi bật trợ giúp|
|COL-07|Sticker|Tối thiểu 36 sticker gốc, đặt/scale/rotate/xóa trong mode riêng|
|COL-08|Tẩy|Xóa nét và sticker theo mode; không xóa outline hoặc dữ liệu template|
|COL-09|Undo/redo|Ít nhất 50 thao tác gần nhất; một stroke là một thao tác|
|COL-10|Vẽ tự do|Giấy trắng; giới hạn trong trang; không tự hứa rig nhân vật từ nét bất kỳ|
|COL-11|Phóng to/thu nhỏ|1×–4×; fit; giữ đúng hit-test và độ dày world-space|
|COL-12|Bản mới/tô lại|Copy-on-write phiên bản, không phá tác phẩm đang dùng|

Bản free cũng có công cụ cốt lõi; không bán undo, save, tẩy hoặc hỗ trợ chặn tràn.

### 5.2. Quy tắc vùng tô

Ưu tiên vector region được chuẩn bị và kiểm chứng, không phụ thuộc flood fill trên ảnh raster có khe hở.

* Tap lấy region trên cùng tại tọa độ inverse-transformed.
* Khi tô giới hạn vùng, vùng được khóa ở pointer-down cho cả stroke. Di chuyển qua vùng khác không tự đổi vùng.
* Có touch tolerance vừa phải cho vùng nhỏ, nhưng không nhảy sang vùng bất kỳ; dùng vùng gần nhất trong ngưỡng rõ.
* Bật/tắt trợ giúp không đổi các nét đã có.
* Hủy gesture do hệ thống: kết thúc stroke hợp lệ hoặc rollback stroke chưa commit; không giữ pointer “kẹt”.
* Eraser chỉ sửa paint layer, không sửa vector mask.
* “Đặt lại vùng” xóa fill và stroke của vùng đã chọn sau xác nhận; “Đặt lại tranh” khác với tẩy.
* Hoàn tất do bé chủ động bấm; không yêu cầu đủ phần trăm vùng tô.
* Palette và outline được render cùng color space thống nhất; tránh thay hue khi xuất.

### 5.3. Layering

Thứ tự: paper → region base fills → brush strokes → pattern overlays → sticker layer → fixed outline/details → UI overlay.

Sticker nằm ngoài silhouette có thể xuất trong tranh; khi tạo đồ chơi, chỉ phần được hỗ trợ trong toy space được giữ theo chế độ giải thích rõ. Mặc định studio tranh có rig giới hạn sticker vào silhouette để tác phẩm và đồ chơi khớp; trang tự do không bị giới hạn này.

Các đường kẻ của template không được rasterize mất độ nét sớm. Precompiled paths hoặc mask phù hợp renderer; không parse SVG/XML trong mỗi frame.

### 5.4. Undo, preview và khả năng phục hồi

Stroke chứa điểm, áp lực nếu có, kích thước, màu, opacity, brushId, textureSeed, regionId và transform local. Fill/sticker là command riêng.

Dùng checkpoint theo số lệnh + incremental command log, không lưu một bitmap toàn trang cho mỗi thao tác. Giữ lịch sử undo gần nhất theo ngân sách; chuyện lưu tác phẩm không phụ thuộc undo còn hay không.

Ngay sau thao tác kết thúc, lên lịch persist nối tiếp; UI phản hồi ngay. Khi app pause hoặc rời canvas, flush có thời hạn và hiển thị trạng thái thực. Lưu lâu không chặn main thread.





<a id="s06"></a>

## 6\. Biến tác phẩm thành đồ chơi

### 6.1. Cơ chế

Mỗi template có bộ phận, pivot, anchor, mask, thứ tự lớp và animation preset. Không gửi tranh lên AI để tạo chuyển động.

Pipeline: lấy ArtworkRevision bất biến đã lưu → render paint trong template coordinates → composite từng phần theo mask → gắn rig 2D/sprite → áp dụng preset → tạo ToyInstance tham chiếu revision → cache theo templateVersion + artworkRevision + rendererVersion.

* Giữ nguyên màu, nét, opacity, seed và vùng trắng của bé.
* Texture của bộ phận chuyển động phải chứa nét vẽ tương ứng của bé; không thay bằng hình tô sẵn.
* Có overlap tại khớp; kiểm tra ở cực trị animation để không hở màu trắng.
* Tạo toy và thêm vào scene phải được lưu nhất quán. Preview chưa lưu không được báo “đã cất”.
* Xóa cache không được làm mất tác phẩm.
* Trang free draw không có rig semantic: biến thành tấm tranh/sticker có thể đặt, kéo và nảy nhẹ. Không hứa tự suy ra chân/tay từ nét bất kỳ.
* Tô lại tạo revision mới; hỏi người dùng có dùng bản màu mới trong cảnh này không. Không âm thầm đổi mọi scene và sách cũ.

### 6.2. Các preset tương tác

|Preset|Đối tượng|Hành vi|
|-|-|-|
|pet\_walk|Mèo, chó, thỏ, thú|Chạm biểu cảm, vuốt vẫy, chọn điểm để đi|
|bird\_fly|Chim, bướm|Bay đường ngắn rồi về anchor|
|flower\_bloom|Hoa|Tưới/chạm để nở và đung đưa|
|nature\_react|Cây, mây, mặt trời|Lá rung, mưa nhẹ, ánh sáng|
|family\_wave|Nhân vật người|Vẫy tay, đi, ngồi|
|machine\_work|Máy xúc, cần cẩu|Di chuyển tay máy/bánh xe, mang vật|
|robot\_dance|Robot|Điệu nhảy ngắn theo mẫu|
|vehicle\_ride|Phương tiện|Di chuyển theo waypoint, nhận hành khách|
|ocean\_swim|Sinh vật biển|Bơi và bong bóng|
|dino\_play|Khủng long|Đi, chào, ngó nghiêng|
|space\_explore|Tên lửa, hành tinh|Bay, xoay nhẹ, quỹ đạo đơn giản|
|food\_serve|Đồ ăn|Đặt bàn, trao phần bằng animation|
|home\_decor|Nhà, nội thất|Mở cửa, bật đèn, ngồi/chứa vật|
|fantasy\_magic|Đồ cổ tích|Bay, hiệu ứng phép nhẹ|
|celebration|Thiệp, cờ, bánh|Mở thiệp, đung đưa, thổi nến bằng chạm|

Một preset dùng nhiều template nhưng từng mẫu có silhouette, pivot, âm thanh/tham số riêng. Lắc toàn bộ sprite không đủ thay thế tất cả tương tác chuyên biệt.

<a id="s07"></a>

## 7\. Thế giới tương tác của bé

### 7.1. Bảy bối cảnh FULL

|ID|Bối cảnh|Điểm tương tác|
|-|-|-|
|garden|Khu vườn|Đất trồng, cây, hồ, lối đi, bàn dã ngoại|
|town|Khu phố|Nhà, đường, công viên, khu xây dựng, trường, quầy|
|ocean|Đại dương|Tầng nước, đáy biển, rạn san hô, bến tàu|
|dinosaur|Thung lũng khủng long|Bãi cỏ, hồ, cầu, hang an toàn|
|space|Vũ trụ|Bệ phóng, hành tinh, quỹ đạo, trạm|
|fantasy|Xứ sở tưởng tượng|Lâu đài, đồi, cây phép, mây, cổng|
|celebration|Sân tiệc|Bàn, sân khấu, chỗ treo trang trí, góc gia đình|

Mỗi bối cảnh có ≥5 anchor và 3 lớp nền. LAUNCH dùng garden/town; VS dùng garden. Cho ít nhất 3 scene mỗi bối cảnh, giới hạn tổng mặc định 30 scene/profile; trước khi hết dung lượng có quản lý và sao lưu. Không tự xóa scene cũ.

Mọi bối cảnh nhận tác phẩm từ các chủ đề khác nhau. Nếu thiếu tương tác chuyên biệt, vật vẫn đặt và phản hồi chạm được; không biến mất hoặc khóa vì khác chủ đề.

### 7.2. Chơi và Trang trí

* **Chơi:** chạm phản ứng, chọn đường, cho lên xe, tưới hoa. Không vô tình thay bố cục vĩnh viễn.
* **Trang trí:** kéo, scale 0.6–1.6, lật khi hợp lệ, đưa trước/sau, cất về bộ sưu tập. Vật được chọn có outline/handle lớn.
* Undo/redo bố trí ≥20 thao tác. Cất khỏi scene không xóa artwork.
* Camera pan/zoom và drag vật có ownership gesture rõ ràng.
* Hit-test ưu tiên front-most; có cách chọn vật bị che.
* Chỉ đưa thao tác xóa vĩnh viễn thư viện vào parent area.

### 7.3. Tương tác xuyên chủ đề MUST

1. Nhân vật/động vật lên passenger anchor của xe và được chở.
2. Máy xúc/cần cẩu mang vật liệu tới vị trí xây vườn.
3. Hoa/cây đặt đất, tưới kích hoạt animation.
4. Đồ ăn đặt bàn; nhân vật ngồi cạnh.
5. Mây/mặt trời đổi mood cảnh, giữ màu tranh.
6. Nhà mở cửa/bật đèn.
7. Cá bơi trong nước; ngoài nước có bong bóng tưởng tượng để vẫn chơi.
8. Tên lửa chở robot/nhân vật, quay về không mất instance.
9. Sao chép vật qua scene bằng reference tới artwork revision.
10. Mỗi vật chỉ gắn một parent attachment; detach trả về tọa độ hợp lệ.

Capability tags: rideable, passenger, plantable, waterable, seatable, table\_item, flying, aquatic, carryable. Không hard-code tên từng tranh vào logic tương tác.

### 7.4. State và simulation

Toy states: Idle, Selected, Dragging, Interacting, Moving, Attached, Paused. Scene modes: Play, Decorate, Story, Rest.

Dùng elapsed time, không đếm frame để tính vận tốc. Background/đổi màn pause animation và sound; resume không nhảy thời gian gây teleport. Waypoint đơn giản đủ dùng, không cần vật lý phức tạp.

Ngân sách: bình thường 20–30 toys, tối đa 40/scene. Máy yếu giảm particles/idle animation trước. Day/night do người dùng chọn, không tạo sự kiện mất nếu bỏ lỡ giờ thật.

<a id="s08"></a>

## 8\. Câu chuyện, sáng tạo và giữ chân

### 8.1. Vòng chơi

Chọn ý tưởng → tô hoặc dùng tác phẩm cũ → chơi → ghép cảnh → lưu vào thế giới/sách → kết thúc tự nhiên.

Một mini-story gợi ý 5–12 phút là giả định sản phẩm, không phải hướng dẫn sức khỏe. Không tối ưu bằng cách kéo dài phiên chơi vô tận.

### 8.2. Story engine

* Mỗi story 3–5 bước, một chỉ dẫn bằng hình/câu ngắn tại một thời điểm.
* Bước định nghĩa capability/template, scene target, completion event, fallback và reward nhìn thấy trước.
* Cho dùng tác phẩm cũ, đổi lựa chọn, bỏ qua, thoát rồi tiếp tục.
* Không yêu cầu màu đúng hoặc tô lại để grind.
* Không story nào bắt mua: template premium có lựa chọn free tương đương.
* Reward là cảnh, trang sách hoặc sticker rõ trước; không gacha/loot box.
* Xong không tự mở story kế tiếp; cho lưu, xem, về home hoặc nghỉ.
* Progress không dùng để chẩn đoán hoặc chấm trí thông minh/phát triển.

### 8.3. Gợi ý và bộ sưu tập

Thứ tự gợi ý local: việc dở → chủ đề yêu thích → chủ đề khác để khám phá. Không upload hồ sơ hành vi. Luôn có “Đổi ý tưởng” và “Tự do sáng tạo”.

30 gợi ý xáo trộn deterministic với seed local; không hết hạn, không phạt đổi đồng hồ hay nghỉ nhiều ngày. Nhân vật không đói, giận, mất đồ vì bé không quay lại.

Album hiển thị tác phẩm thật; favorite, continue, duplicate, so sánh hai bản do phụ huynh chọn. Không xếp hạng trẻ.

Parent area gợi ý hoạt động ngoài màn hình: in tranh, tìm đồ có màu đã chọn, kể chuyện cùng người thân.

<a id="s09"></a>

## 9\. Kho nội dung: 14 chủ đề, 168 mẫu tranh

Mỗi nhóm có 12 ID ổn định. Tạo đủ 168 manifest entry và 168 thiết kế phân biệt bằng silhouette/nội dung. Mirror, đổi palette hay thay độ chi tiết không được tính thành mẫu mới.

Trong store build, 6 mẫu đầu mỗi nhóm là base free; 6 mẫu sau là expansion. Offline FULL mở tất cả. Tên EN/VI đầy đủ ở locale catalog; slug không hiển thị cho bé.

### 9.1. animals — Animals / Động vật

1. animals.cat — Mèo
2. animals.dog — Chó
3. animals.rabbit — Thỏ
4. animals.elephant — Voi
5. animals.giraffe — Hươu cao cổ
6. animals.bird — Chim nhỏ
7. animals.lion — Sư tử
8. animals.panda — Gấu trúc
9. animals.monkey — Khỉ
10. animals.sheep — Cừu
11. animals.horse — Ngựa
12. animals.butterfly — Bướm

Pet/bird presets, silhouette đặc trưng, biểu cảm thân thiện.

### 9.2. flowers — Flowers / Hoa

1. flowers.sunflower — Hướng dương
2. flowers.rose — Hoa hồng
3. flowers.lotus — Hoa sen
4. flowers.tulip — Tulip
5. flowers.daisy — Cúc
6. flowers.hibiscus — Dâm bụt
7. flowers.lily — Hoa ly
8. flowers.orchid — Phong lan
9. flowers.lavender — Oải hương
10. flowers.daffodil — Thủy tiên
11. flowers.marigold — Vạn thọ
12. flowers.hydrangea — Cẩm tú cầu

Bloom/water presets, khác cánh/lá/silhouette. Không thêm kiến thức thực vật chưa kiểm chứng.

### 9.3. nature — Nature / Thiên nhiên

1. nature.tree — Cây tán tròn
2. nature.sun — Mặt trời
3. nature.cloud — Mây
4. nature.rainbow — Cầu vồng
5. nature.mountain — Núi
6. nature.pond — Hồ nhỏ
7. nature.waterfall — Thác nước
8. nature.mushroom — Nấm trang trí
9. nature.leaf — Chiếc lá
10. nature.moon — Mặt trăng
11. nature.rain — Mây mưa
12. nature.island — Hòn đảo

Nature presets, pond có anchor nước/bờ. Nấm là đồ trang trí, không hướng dẫn hái/ăn.

### 9.4. family — Family / Gia đình

1. family.child — Nhân vật trẻ nhỏ
2. family.caregiver — Người chăm sóc
3. family.mother — Mẹ
4. family.father — Bố
5. family.grandparent — Ông/bà, có lựa chọn phù hợp
6. family.sibling — Anh/chị/em
7. family.reading — Nhân vật đọc sách
8. family.cooking — Nhân vật chuẩn bị bữa ăn
9. family.gardening — Nhân vật làm vườn
10. family.picnic — Thảm và giỏ dã ngoại
11. family.petcare — Nhân vật chăm thú cưng
12. family.celebrate — Nhân vật cầm quà

Có nhiều màu da/tóc/trang phục; cách gọi do phụ huynh chọn, mặc định “người thân”. Không bắt gia đình có đủ bố/mẹ. Activity characters có hình khác, không chỉ đổi nhãn.

### 9.5. machines — Machines / Máy móc

1. machines.excavator — Máy xúc
2. machines.crane — Cần cẩu
3. machines.tractor — Máy kéo
4. machines.robot — Robot thân thiện
5. machines.bulldozer — Xe ủi
6. machines.cement\_mixer — Xe trộn bê tông
7. machines.road\_roller — Xe lu
8. machines.forklift — Xe nâng
9. machines.harvester — Máy gặt
10. machines.windmill — Cối xay gió
11. machines.conveyor — Băng chuyền
12. machines.submarine\_robot — Robot thám hiểm biển

Chức năng giúp đỡ/xây dựng; không hướng dẫn vận hành máy thật.

### 9.6. vehicles — Vehicles / Phương tiện

1. vehicles.car — Ô tô
2. vehicles.train — Tàu hỏa
3. vehicles.airplane — Máy bay
4. vehicles.bicycle — Xe đạp
5. vehicles.bus — Xe buýt
6. vehicles.boat — Thuyền
7. vehicles.tram — Xe điện
8. vehicles.hot\_air\_balloon — Khinh khí cầu
9. vehicles.helicopter — Trực thăng
10. vehicles.sailboat — Thuyền buồm
11. vehicles.scooter — Xe trượt chân
12. vehicles.camper — Xe cắm trại

Car/train/bus/boat có passenger anchor. Không bắt tô lại hành khách để lên xe.

### 9.7. ocean — Ocean / Đại dương

1. ocean.clownfish — Cá hề
2. ocean.turtle — Rùa biển
3. ocean.dolphin — Cá heo
4. ocean.octopus — Bạch tuộc
5. ocean.seahorse — Cá ngựa
6. ocean.coral — San hô
7. ocean.whale — Cá voi
8. ocean.starfish — Sao biển
9. ocean.crab — Cua
10. ocean.jellyfish — Sứa
11. ocean.seashell — Vỏ sò
12. ocean.submarine — Tàu ngầm

Swim/home\_decor presets, không săn mồi hay chớp sáng mạnh.

### 9.8. dinosaurs — Dinosaurs / Khủng long

1. dinosaurs.triceratops — Triceratops
2. dinosaurs.stegosaurus — Stegosaurus
3. dinosaurs.brachiosaurus — Brachiosaurus
4. dinosaurs.tyrannosaurus — Tyrannosaurus thân thiện
5. dinosaurs.ankylosaurus — Ankylosaurus
6. dinosaurs.parasaurolophus — Parasaurolophus
7. dinosaurs.diplodocus — Diplodocus
8. dinosaurs.spinosaurus — Spinosaurus thân thiện
9. dinosaurs.iguanodon — Iguanodon
10. dinosaurs.pachycephalosaurus — Pachycephalosaurus
11. dinosaurs.egg — Trứng tưởng tượng trang trí
12. dinosaurs.fossil — Hóa thạch đồ chơi

Kiểm chứng tên khi thêm kiến thức; không gọi mọi bò sát cổ đại là khủng long.

### 9.9. space — Space / Vũ trụ

1. space.rocket — Tên lửa
2. space.planet — Hành tinh tưởng tượng
3. space.astronaut — Phi hành gia
4. space.satellite — Vệ tinh
5. space.moon\_rover — Xe thám hiểm
6. space.star — Ngôi sao trang trí
7. space.station — Trạm không gian
8. space.comet — Sao chổi
9. space.ringed\_planet — Hành tinh có vành
10. space.telescope — Kính thiên văn
11. space.alien\_friend — Bạn ngoài hành tinh tưởng tượng
12. space.galaxy — Thiên hà trang trí

Tách tưởng tượng khỏi kiến thức khoa học; không tự bịa khoảng cách/kích thước.

### 9.10. food — Food / Đồ ăn

1. food.apple — Táo
2. food.banana — Chuối
3. food.carrot — Cà rốt
4. food.strawberry — Dâu tây
5. food.bread — Bánh mì
6. food.cake — Bánh kem
7. food.watermelon — Dưa hấu
8. food.broccoli — Bông cải
9. food.soup — Bát súp
10. food.sandwich — Sandwich
11. food.dumpling — Bánh bao
12. food.fruit\_bowl — Tô trái cây

Không đánh giá cân nặng hoặc hướng dẫn nấu thật bằng nhiệt/dao.

### 9.11. home\_city — Home \& City / Nhà và thành phố

1. home\_city.house — Ngôi nhà
2. home\_city.school — Trường học
3. home\_city.park — Công viên
4. home\_city.bed — Giường
5. home\_city.table — Bàn
6. home\_city.chair — Ghế
7. home\_city.bridge — Cầu
8. home\_city.library — Thư viện
9. home\_city.shop — Cửa hàng
10. home\_city.lamp — Đèn
11. home\_city.playground — Sân chơi
12. home\_city.fountain — Đài phun nước

House có door/light anchor; nội thất có capability ngồi/đặt đồ hợp lý.

### 9.12. fantasy — Fantasy / Tưởng tượng

1. fantasy.dragon — Rồng nhỏ
2. fantasy.unicorn — Kỳ lân
3. fantasy.castle — Lâu đài
4. fantasy.fairy\_house — Nhà cổ tích
5. fantasy.magic\_tree — Cây phép
6. fantasy.friendly\_monster — Quái vật đáng yêu
7. fantasy.flying\_cat — Mèo có cánh
8. fantasy.cloud\_house — Nhà trên mây
9. fantasy.magic\_carpet — Thảm bay
10. fantasy.crystal — Pha lê
11. fantasy.wand — Đũa phép đồ chơi
12. fantasy.story\_gate — Cổng truyện

Tất cả original; không nhái silhouette nhân vật nổi tiếng.

### 9.13. jobs — Jobs / Nghề nghiệp

1. jobs.doctor — Bác sĩ
2. jobs.chef — Đầu bếp
3. jobs.gardener — Người làm vườn
4. jobs.firefighter — Lính cứu hỏa
5. jobs.teacher — Giáo viên
6. jobs.builder — Thợ xây
7. jobs.artist — Họa sĩ
8. jobs.astronaut — Phi hành gia đang sửa trạm
9. jobs.farmer — Nông dân
10. jobs.veterinarian — Bác sĩ thú y
11. jobs.scientist — Nhà khoa học với dụng cụ đồ chơi
12. jobs.musician — Nhạc công

Silhouette/đạo cụ khác family/space, không duplicate. Không gắn nghề với giới tính; không cảnh đau đớn/cấp cứu gây sợ.

### 9.14. celebrations — Celebrations / Lễ hội và dịp vui

1. celebrations.birthday\_cake — Bánh sinh nhật có nến
2. celebrations.balloons — Chùm bóng
3. celebrations.gift — Hộp quà
4. celebrations.bunting — Dây cờ
5. celebrations.greeting\_card — Thiệp
6. celebrations.paper\_lantern — Đèn giấy
7. celebrations.spring\_blossom — Cành hoa mùa xuân
8. celebrations.lantern\_festival — Lồng đèn lễ hội
9. celebrations.winter\_tree — Cây mùa đông trang trí
10. celebrations.festival\_lights — Dây đèn
11. celebrations.kite\_festival — Diều lễ hội
12. celebrations.family\_banner — Băng rôn ngày gia đình

Nội dung văn hóa phải được người hiểu địa phương xem lại. Không coi một đồ trang trí là đại diện mọi nền văn hóa.

<a id="s10"></a>

## 10\. Danh mục 28 câu chuyện

Mỗi hàng được triển khai thành 3–5 bước có event và save state. Dùng lại tác phẩm cũ được tính hoàn thành bình thường.

|ID|Câu chuyện|Scene|Nội dung chính và kết quả|
|-|-|-|-|
|ST-01|Dã ngoại cùng thỏ|garden|animals.rabbit lên vehicles.car → đến family.picnic → đặt đồ ăn|
|ST-02|Bạn mới trong vườn|garden|animals.cat + animals.dog + một cây/hoa → chạm chào|
|ST-03|Vườn hoa cầu vồng|garden|Chọn ba flowers → trồng → tưới từng cây|
|ST-04|Bó hoa tặng người thân|celebration|Chọn ba hoa → xếp bó → trao family.caregiver|
|ST-05|Một ngày có mây và nắng|garden|nature.tree + sun + cloud → chọn mood → lưu cảnh|
|ST-06|Cây cầu qua hồ nhỏ|garden|nature.pond + home\_city.bridge → đặt cầu → đưa nhân vật qua|
|ST-07|Cùng nhau đọc sách|town|family.reading + home\_city.chair → ngồi → chọn bìa sách|
|ST-08|Bữa ăn gia đình|town|home\_city.table + food + hai family → sắp bàn → ngồi cùng|
|ST-09|Máy xúc xây vườn|garden|machines.excavator chuyển ba khối đất → trồng hoa|
|ST-10|Robot giúp xếp đồ|town|machines.robot đưa ba vật carryable đến anchor|
|ST-11|Chuyến xe buýt vui|town|vehicles.bus đón hai passenger → tới công viên|
|ST-12|Tàu hỏa qua khu phố|town|vehicles.train + home\_city.house → hai điểm dừng → chạy vòng|
|ST-13|Khu vườn dưới biển|ocean|ocean.coral + clownfish + turtle → ghép rạn → chơi swim|
|ST-14|Tàu ngầm khám phá|ocean|ocean.submarine → ba waypoint → quan sát ba vật biển|
|ST-15|Công viên khủng long|dinosaur|Hai dinosaurs + nature.tree → dựng cảnh → đi tới hồ|
|ST-16|Theo dấu chân nhỏ|dinosaur|dinosaurs.triceratops theo ba dấu → tìm nature.leaf|
|ST-17|Chuyến bay tên lửa|space|space.astronaut lên rocket → tới planet → quay về|
|ST-18|Hành tinh mới|space|space.planet + star + machines.robot → trang trí → lưu|
|ST-19|Quầy trái cây|town|food.apple + banana + strawberry → xếp quầy → trao giỏ|
|ST-20|Bữa tiệc nhỏ|celebration|food.cake + hai món → bày bàn → chào khách|
|ST-21|Căn phòng bạn nhỏ|town|home\_city.bed + chair + family.child → sắp đồ → đặt bạn|
|ST-22|Khu phố có công viên|town|home\_city.house + park + vehicles.car → nối đường → chơi|
|ST-23|Bạn rồng tìm nơi chơi|fantasy|fantasy.dragon + castle → đặt nơi chơi → bay ba điểm|
|ST-24|Ngôi nhà cổ tích|fantasy|fantasy.fairy\_house + hai flowers + đèn prop → trang trí|
|ST-25|Người làm vườn|garden|jobs.gardener + machines.tractor → chở chậu → trồng hai hoa|
|ST-26|Đầu bếp chuẩn bị bàn|town|jobs.chef + home\_city.table + ba food → bày và chào khách|
|ST-27|Sinh nhật sắc màu|celebration|birthday\_cake + balloons + gift → trang trí → chạm nến → mở quà|
|ST-28|Thiệp cho gia đình|celebration|greeting\_card + family.caregiver → trang trí → trao → lưu trang|

Các tên rút gọn trong bảng dùng cùng prefix với item trước đó khi rõ, nhưng JSON sản xuất luôn dùng full ID.

Props bổ sung bắt buộc, không tính vào 168, dùng ID ổn định: prop.soil\_block, prop.watering\_can, prop.plant\_pot, prop.bouquet\_wrap, prop.footprint, prop.stall, prop.basket, prop.lamp, prop.path, prop.book, prop.waypoint, prop.gift\_toy, prop.picnic\_mat, prop.bridge, prop.exploration\_window, prop.leaf, prop.chair, prop.table, prop.house, prop.park, prop.food\_plate. Tất cả thuộc base, có asset thật và capability/anchor phù hợp.

Free fallback cần khai báo: ST-01 family.picnic→prop.picnic\_mat + prop.basket; ST-06 home\_city.bridge→prop.bridge; ST-07 family.reading→family.caregiver + prop.book; ST-14 ocean.submarine→vehicles.boat + prop.exploration\_window; ST-16 nature.leaf→prop.leaf. Story nào chọn premium khác phải có capability alternative free. Validator kiểm tra toàn bộ 28 story có đường free hoàn thành, không dựa vào hard-code im lặng.

Ở mốc LAUNCH, home\_city và food chưa thuộc 72 mẫu: resolver dùng prop.house/park/chair/table/bridge/food\_plate tương ứng cho ST-01–12. Props là thành phần chơi sẵn, không giả là mẫu tranh tô đã hoàn thành. FULL ưu tiên template thật khi có quyền; vẫn giữ props để save cũ và đường free tiếp tục hoạt động.

<a id="s11"></a>

## 11\. Danh mục 30 gợi ý sáng tạo

|ID|Gợi ý|
|-|-|
|CR-01|Tô một con vật bằng ba màu con thích|
|CR-02|Tạo mèo có các đốm màu khác nhau|
|CR-03|Làm chiếc xe cầu vồng|
|CR-04|Trồng khu vườn với màu ấm|
|CR-05|Thử một bông hoa với màu lạnh|
|CR-06|Tạo bầu trời theo trí tưởng tượng|
|CR-07|Tô hai bạn bằng họa tiết khác nhau|
|CR-08|Trang trí ngôi nhà cho mèo|
|CR-09|Làm robot với chiếc mũ ngộ nghĩnh|
|CR-10|Tạo đàn cá nhiều màu|
|CR-11|Tô khủng long bằng bút sáp|
|CR-12|Thử cọ màu nước trên chiếc lá|
|CR-13|Làm hành tinh có sọc|
|CR-14|Xếp bàn tiệc cho ba người bạn|
|CR-15|Tạo bó hoa tặng người thân|
|CR-16|Trang trí phòng bằng hai màu chính|
|CR-17|Tạo tàu chở động vật|
|CR-18|Thử chấm tròn trên cánh bướm|
|CR-19|Làm khu phố có nhiều cây|
|CR-20|Tạo nhân vật có cánh|
|CR-21|Tô lại tranh cũ theo palette mới|
|CR-22|Kể chuyện có mèo, xe và hoa|
|CR-23|Làm trang sách về gia đình|
|CR-24|Chọn ba tác phẩm cho cảnh mới|
|CR-25|Tạo bức tranh có nhiều khoảng trắng|
|CR-26|Phối màu mới trong phòng màu|
|CR-27|Trang trí thiệp bằng hình tự vẽ|
|CR-28|Làm cảnh trời tối dịu nhẹ|
|CR-29|Thay phiên tô cùng người thân|
|CR-30|Chọn tác phẩm muốn in ra giấy|

Mỗi gợi ý có minh họa, text EN/VI và “Thử/Đổi ý tưởng”. Không timer, đúng/sai hoặc chặn nếu bé dùng màu khác. Màu ấm/lạnh có palette gợi ý dễ hiểu, không kiểm tra kiến thức. Resolver chỉ đề xuất gợi ý có nội dung đang truy cập được: nếu CR-18 cần bướm chưa có quyền thì chọn gợi ý khác hoặc biến thể chấm trên cánh chim với câu chữ tương ứng; không đưa bé vào màn khóa.

<a id="s12"></a>

## 12\. Tạo nhân vật, thử màu và sách của bé

### 12.1. Character Builder

FULL có ≥44 part original: 6 thân, 8 tai, 8 đuôi, 6 cánh, 8 mũ, 8 trang trí. Option “không có” không tính vào 44.

Chọn thân → bộ phận → màu/họa tiết → thử chuyển động → lưu. Mỗi part có socket type, pivot, bounds, z-order và region. Giới hạn scale để không ra ngoài khung. Lưu recipe + revision, không flatten rồi mất cấu trúc.

Ít nhất 6 preset hành vi theo base. Kiểm tra tự động socket combinations và render contact sheets để phát hiện xuyên/hở. Không tự upload nhân vật.

### 12.2. Phòng thử màu

Hai hoặc ba giọt màu, khay trộn, vùng thử trên giấy; thử sáp/dạ/nước. Lưu tối đa 24 custom swatch/profile.

Dùng mô hình trộn màu minh họa mỹ thuật có LUT/palette được art-direct. Không gọi trung bình RGB là mô phỏng pigment chính xác. Cặp minh họa RYB: đỏ+vàng→cam, vàng+xanh lam→xanh lá, đỏ+xanh lam→tím. Phần còn lại định nghĩa nhất quán và kiểm thử.

Chế độ học tên màu có audio riêng, không bắt dùng trong free painting. Reset phòng màu không reset tác phẩm khác.

### 12.3. Sách của bé

* Sách hoàn chỉnh 4–12 trang + bìa; draft ít trang hơn vẫn lưu.
* Layout: 1 tranh, 2 tranh, hoặc scene + sticker.
* Artwork/scene/character dùng immutable revision.
* Kéo thả trong safe area, sắp trang, duplicate, remove có undo.
* Tiêu đề/nhãn preset; parent có thể nhập local, không bắt trẻ gõ.
* Preview lật trang, reduced motion, không autoplay vô hạn.
* Xuất PDF/PNG qua parent area, preview trước chia sẻ.
* Không thu âm/chụp ảnh trẻ trong FULL mặc định.

### 12.4. Chơi cùng gia đình

Một thiết bị, thay phiên. “Mỗi người một màu” có lời nhắc đổi lượt tùy chọn, không thi đấu theo thời gian. Nhiều profile riêng, không multiplayer online.

<a id="s13"></a>

## 13\. Âm thanh, chuyển động và khả năng tiếp cận

* SFX local ngắn: chạm màu, cọ, sách, hoa nở, tiếng động vật/xe dịu.
* Giới hạn voice đồng thời; không phát âm trên mỗi điểm stroke.
* ≥3 loop nhạc gốc/có quyền dùng, khoảng 45–90 giây trở lên, nối mượt, nhỏ.
* Tách Music/Effects/Voice và mute toàn bộ.
* Audio focus đúng; pause khi background; không phát chồng khi resume.
* Thổi nến bằng chạm/kéo, không cần mic.
* Lời đọc EN/VI đóng gói local và có quyền dùng. System TTS chỉ là fallback tùy chọn; không phụ thuộc tải thêm giọng để chơi.
* Tắt tiếng vẫn hiểu luồng nhờ hình/chữ.
* Icon chức năng có accessibility label. Canvas cung cấp semantic action cho công cụ/màu/vùng trong fill mode; ghi rõ giới hạn free drawing.
* Không tuyên bố đầy đủ accessibility nếu chưa thử TalkBack.
* Tôn trọng font scaling, không crop dấu tiếng Việt.
* Selected state có tick/outline, không chỉ màu.
* Reduced motion bỏ parallax/bounce mạnh/particles. Không strobe.
* Haptic nhẹ có toggle; không rung kéo dài.





<a id="s14"></a>

## 14\. Phụ huynh, quyền riêng tư và thời lượng

### 14.1. Parent area

MUST có: đổi profile, mức kỹ năng, ngôn ngữ, âm thanh, reduced motion, thời lượng, thống kê local, xuất/sao lưu, khôi phục, xóa dữ liệu, nội dung mở rộng, thông tin quyền riêng tư và giấy phép.

* Profile dùng avatar/pseudonym; không cần tên thật, ngày sinh, email.
* Tạo tối đa 5 profile local; không trộn tranh/cảnh/setting giữa các profile.
* Đổi profile từ parent area hoặc bộ chọn avatar đã được parent bật.
* Parent gate trước mua hàng, mở URL, chia sẻ, export ra ứng dụng khác, import và xóa vĩnh viễn.
* Cổng đầu có thao tác giữ + hướng dẫn dành người lớn; khi phụ huynh thiết lập PIN thì dùng PIN cho thao tác nhạy cảm.
* Ưu tiên device credential cho xác thực phụ huynh nếu phù hợp. PIN local có giới hạn thử, không lưu plaintext; tránh hứa đây là xác minh tuổi/phụ huynh theo luật.
* Gate là chống thao tác vô ý, không thay thế verifiable parental consent nếu sau này thu dữ liệu cần consent.
* Parent session hết hiệu lực khi app background hoặc sau 2 phút không tương tác; share/purchase/delete yêu cầu xác thực mới nếu session không còn.
* Recovery PIN cần thiết kế rõ, không có “mã bí mật” hard-code. Nếu không có device credential hỗ trợ recovery, giải thích giới hạn và việc reset dữ liệu qua hệ thống; không tự reset hay âm thầm xóa.
* Parent authentication state không restore sau process death; đổi profile cũng làm hết phiên xác thực.
* Xóa profile nêu rõ phạm vi; có lựa chọn backup trước và xác nhận riêng.

### 14.2. Thời lượng

Các lựa chọn parent: không giới hạn / 5 / 10 / 15 / 20 phút hoặc tùy chỉnh hợp lý. Đây là tùy chọn sản phẩm, không phải khuyến nghị y khoa.

* Chỉ tính foreground play time bằng monotonic clock; pause khi app background và khi parent area mở.
* Cảnh báo nhẹ trước khi hết khoảng 1 phút, không làm gián đoạn stroke.
* Khi hết: kết thúc stroke hiện tại, flush dữ liệu, chuyển UI-16; cho tối đa khoảng 10 giây để hoàn tất thao tác lưu.
* Nếu save lỗi, không ghi “đã lưu”. Có đường xử lý cho parent.
* Gia hạn qua gate; không pop-up mua thêm phút.
* Khởi động lại không tự đặt lại budget buổi đang chạy. Lưu consumed duration và session checkpoint.
* Date change/clock change không dùng để phạt hoặc xóa nội dung. Ghi rõ đây không phải giải pháp parental control cấp hệ điều hành.

### 14.3. Quyền và SDK

Mặc định FULL offline: không camera, microphone, contacts, location, advertising ID, push permission; không third-party ad SDK; không SDK analytics gửi dữ liệu tự động.

* SAF/system picker cho import/export; chia sẻ content URI với quyền đọc tạm thời.
* Không xin quyền truy cập toàn bộ bộ nhớ để tô/lưu trong app.
* Không background job gửi dữ liệu hoặc download khi trẻ đang chơi.
* URL chỉ mở sau parent gate và chỉ tới destination cố định, allowlist.
* Không webview mở nội dung web bất kỳ cho trẻ.
* Không embedding service-account secret hay khóa riêng trong APK.
* Cloud Auto Backup không được tự upload dữ liệu trẻ trái lời hứa offline; cấu hình rules cho các Android version tương ứng, kiểm tra cloud và device transfer riêng.
* Đặt artwork/scene/raw event ở noBackup storage khi phù hợp; database/settings nhạy cảm có exclude rules rõ.
* Dùng dependency inventory và kiểm tra manifest merge để phát hiện permission/SDK ngoài dự tính.
* Nếu bật store/billing, chính sách quyền riêng tư phải phản ánh dữ liệu/thành phần thật; “không backend riêng” không đồng nghĩa “không có bên thứ ba xử lý”.

Google Play Families/COPPA và quy định thị trường có thể thay đổi. Trước phát hành phải kiểm tra nguồn chính thức hiện hành theo mục 25; không tuyên bố chứng nhận hoặc tuân thủ đã được xác thực chỉ dựa vào tài liệu này.

<a id="s15"></a>

## 15\. Kiến trúc Android và offline

### 15.1. Stack mặc định

|Thành phần|Chọn mặc định|
|-|-|
|Ngôn ngữ|Kotlin|
|Giao diện|Jetpack Compose, custom design system|
|Vẽ và thế giới 2D|Custom Android View/Canvas nhúng AndroidView|
|State|ViewModel, StateFlow, coroutines; unidirectional data flow|
|Metadata|Room với migrations|
|Preferences|DataStore|
|File tác phẩm|Versioned command documents + immutable snapshots + cache|
|Vector nội dung|SVG nguồn được biên dịch/chuẩn hóa thành paths + masks + rig data|
|Âm thanh|Android audio APIs phù hợp SFX và loop local|
|Xuất|PNG, Android PDF APIs, video encoder Android|
|Build|Gradle Kotlin DSL, version catalog, wrapper được commit|
|Test|Unit, instrumentation, screenshot và macrobenchmark phù hợp|

Đây là lựa chọn thiết kế cho bài toán Android-first, không phải yêu cầu dùng phiên bản thư viện cố định đã cũ. Claude phải kiểm tra compatibility Kotlin/AGP/Gradle/JDK/Compose hiện hành từ tài liệu chính thức, chọn stable, pin version thực trong libs.versions.toml. Không dùng version “+”, snapshot hoặc artifact không tồn tại.

Đề xuất minSdk 26 cho dự án mới; quyết định cuối ghi ADR sau kiểm tra thị trường mục tiêu. compileSdk/targetSdk đáp ứng Google Play tại thời điểm phát hành, không hard-code theo suy đoán trong tài liệu này.

Không cần WebView, Unity/3D engine, tài khoản Firebase hoặc AI runtime cho phần cốt lõi. Nếu repository đã dùng stack khác hợp lý, đánh giá reuse bằng prototype trước khi rewrite; giữ cùng tính năng và tiêu chí.

### 15.2. Module đề xuất

|Module|Trách nhiệm|
|-|-|
|app|Entry, navigation, build variants, DI wiring|
|core:model|Data contracts, IDs, capability types|
|core:design|Tokens, typography, buttons, cards, transitions|
|core:content|Catalog parsing, validation, locale/asset lookup|
|core:drawing|Commands, paths, hit-test, brushes, renderer|
|core:world|Scene graph, anchors, transforms, interactions, clock|
|core:data|Room, documents, migrations, repositories, atomic writes|
|core:audio|SFX, voice, music, focus|
|core:export|PNG/PDF/MP4, cancellation, file staging|
|core:parent|Gate, session budget, privacy settings|
|feature:home|Home và onboarding|
|feature:studio|Library, painting, toy reveal, color lab|
|feature:world|World picker, play/decorate|
|feature:stories|Story runtime và catalog|
|feature:collection|Gallery, builder, books|
|feature:parents|Parent area, backup, settings, store|
|billing|Interface và Play adapter, chỉ store flavor|
|content-tools|Validators, asset compilation, contact sheets|

Có thể gộp module khi tránh build overhead, nhưng giữ ranh giới trách nhiệm và khả năng test. Không tạo một file Activity dài chứa toàn bộ app.

### 15.3. Renderer và input

* Compose quản lý controls/navigation, không recompose cả cây theo từng điểm bút.
* Custom View nhận immutable render state + stream input; bộ vẽ giữ cache cục bộ có ownership rõ.
* Main thread xử lý input và submit frame, không parse asset, serialize file hoặc encode video.
* Dùng incremental paint surfaces/tiles và dirty regions; không rerender toàn bộ lịch sử mỗi điểm.
* Precompute region bounds/spatial index; chỉ hit-test candidate paths.
* Tọa độ: screen → inverse viewport → template/scene → part local. Dùng cùng transform chain cho render và hit-test.
* Kích thước bút được lưu theo template units, không raw screen px.
* Khi chuyển từ một sang hai pointer, hủy/commit nét đang mở theo policy nhất quán rồi pan/zoom; không vẽ một đường chéo dài.
* Scene graph có parent-child transforms, scale/flip/z-order, attachment constraints, collision/hit shapes.
* Clock được inject để test và render export deterministic.
* Worker thread tạo snapshot/thumbnail, kết quả có revision token; bỏ kết quả cũ nếu document đã đổi.
* Không để job của profile trước ghi vào profile mới.
* Texture cache có LRU và tính ngân sách bytes, không chỉ số lượng entry.
* Dispose audio/frame callbacks/bitmap references đúng lifecycle.

### 15.4. Build variants và offline

* **offlineFull:** mặc định cho bàn giao; mở đủ 168 mẫu/28 stories/7 worlds; không billing, không network SDK, không cần INTERNET permission nếu không thành phần nào cần. Parent help nằm local.
* **store:** có base free và expansion; cùng gameplay; billing adapter thật, chỉ hiện mua khi có cấu hình/product details hợp lệ.
* Debug có thể chứa fake billing để test, nhưng dependency và flag bị loại khỏi release.
* Mua mới/khôi phục quyền cần Google Play và mạng. Nội dung đã có quyền cached vẫn chơi offline.
* Bộ nội dung mặc định đóng gói install-time và phải chơi được sau cài đặt khi tắt mạng. Dùng app updates để bổ sung nội dung ở bản đầu.
* Nếu về sau dùng optional download packs, công khai trạng thái “chưa tải”, cho parent tải/xóa pack; không gọi nội dung chưa tải là offline available.
* Không cần backend cho game state. Đồng bộ đa thiết bị/cloud backup là LATER, không giả lập bằng tài khoản đăng nhập rỗng.

<a id="s16"></a>

## 16\. Mô hình dữ liệu và hợp đồng nội dung

### 16.1. Đối tượng chính

|Entity|Trường/chính sách cốt lõi|
|-|-|
|ChildProfile|id local UUID, avatarId, optional localAlias, skillMode, locale, createdAt|
|ParentSettings|âm thanh, gate policy, giới hạn, export prefs; tách khỏi child content|
|TemplateDefinition|stable id, topic, contentVersion, bounds, difficulty, region/rig/outline refs, capabilities, access tier|
|ArtworkDocument|id, profileId, kind, sourceId/version, currentRevisionId, created/updated, favorite|
|ArtworkRevision|immutable id, parentRevisionId, dimensions, command checkpoint/log refs, digest, thumbnail ref|
|PaintCommand|sequence, type, local points/region, color, opacity, brush, seed, transform|
|ToyDefinition|template/recipe source, rig schema, supported actions|
|ToyInstance|id, artworkRevisionId, sceneId, transform, zOrder, parentAttachment, optional state|
|SceneDocument|id, profileId, worldId/version, objects, environment, revision, story association|
|StoryDefinition|id, text keys, scene, steps, event predicates, free fallbacks|
|StoryProgress|profile, story, attemptId, step, selected revision refs, completed events|
|CreativePrompt|id, locale keys, suggested capability/palette, no deadline|
|CharacterRecipe|id, version, body/parts, sockets, paint refs, preset|
|StoryBook|id, profile, title, pages ordered, snapshot refs, cover|
|CollectionIndex|profile, type, item ref, tags/favorites, lastUsed|
|SessionBudget|active session id, consumed monotonic duration checkpoints, parent limit|
|Entitlement|store product, verification status/time, cached grant; never trusted from imported backup|
|ContentPack|id/version, manifest checksum, file index, access mapping|
|ExportJob|id, input immutable refs, type, status/progress, staging path, cancellation|

Room dùng foreign keys/indexes phù hợp, migrations thật và tests. Timestamp không làm primary key; không dùng tên hiển thị làm identity. Không sửa/xóa template ID khi đổi nhãn dịch.

### 16.2. Ví dụ template metadata

Ví dụ hợp đồng, không phải các file asset đã được tạo. Claude phải tạo đủ tài nguyên thật và validator, không copy đường dẫn rồi để file rỗng.

\~\~\~json
{
  "schemaVersion": 1,
  "id": "animals.cat",
  "contentVersion": 1,
  "topicId": "animals",
  "labelKey": "template.animals.cat",
  "difficulty": "simple",
  "access": {
    "tier": "base",
    "packId": "base"
  },
  "designBounds": {
    "width": 1024,
    "height": 1024
  },
  "assets": {
    "outline": "templates/animals/cat/outline.svg",
    "regions": "templates/animals/cat/regions.json",
    "rig": "templates/animals/cat/rig.json",
    "thumbnail": "templates/animals/cat/thumbnail.webp"
  },
  "interactionPreset": "pet\_walk",
  "capabilities": \["passenger", "seatable", "carryable"],
  "defaultPaletteId": "bright\_basic",
  "audioSetId": "cat\_gentle",
  "compatibleWorlds": \["garden", "town", "ocean", "dinosaur", "space", "fantasy", "celebration"]
}
\~\~\~

Region file mô tả: regionId, pathRef/pathData chuẩn hóa, partId, drawOrder, bounds, fillable, lockedDetail, touchPriority. Rig file mô tả: partId, parentPartId, pivot local, maskRegionIds, outlineRef, bindTransform, animation channels, anchors.

Một region chỉ thuộc một paint ownership part. Chi tiết cố định và mảng overlap có quy tắc riêng để không nhân stroke/opacity. Rig không có cycle. Normalize SVG transforms vào hệ tọa độ thống nhất, không tin khả năng SVG renderer hỗ trợ mọi filter.

### 16.3. Ví dụ scene instance

\~\~\~json
{
  "schemaVersion": 1,
  "id": "scene-example-01",
  "profileId": "profile-example-01",
  "worldId": "garden",
  "worldVersion": 1,
  "revision": 3,
  "environment": {
    "timeOfDay": "day",
    "weather": "clear",
    "reducedMotion": false
  },
  "objects": \[
    {
      "id": "toy-example-01",
      "artworkRevisionId": "artrev-example-01",
      "transform": {
        "x": 0.45,
        "y": 0.68,
        "scale": 1.0,
        "rotationDegrees": 0,
        "flipX": false
      },
      "zOrder": 10,
      "attachment": null
    }
  ]
}
\~\~\~

Scene positions normalized vào logical scene bounds; scale relative asset baseline. Những ID example chỉ dùng tài liệu, không seed đồng loạt ID cố định trong production. reducedMotion runtime phải lấy settings hiện tại; giá trị scene này chỉ là gợi ý export, không ghi đè lựa chọn accessibility của người dùng.

### 16.4. Paint command contract

Command types: FillRegion, AddStroke, AddSticker, TransformSticker, RemoveSticker, ClearRegion, ClearPage, SetPaperStyle. Không lưu raw Android object vào JSON.

Stroke ghi points x/y/pressure/timeDelta, brushId/version, brushSizeUnits, colorARGB, opacity, seed, clipRegionId. Giới hạn sample rate và dùng interpolation có kiểm soát; không đổi shape khi reload.

Undo/redo phục hồi chính xác thứ tự và layer, bao gồm fill sau khi có stroke. Command schema có migration version. Chỉ renderer mới được diễn giải brush, business logic không phụ thuộc Android Canvas.

### 16.5. Content validation MUST

* Đúng 14 topic và 12 unique templates/topic cho FULL.
* Đúng 7 worlds, 28 stories, 30 prompts.
* ≥44 character parts, ≥36 stickers, scene props và audio sets đủ tham chiếu.
* Tất cả ID unique; asset path tồn tại, đúng case, không ../.
* Label key EN/VI đầy đủ; không fallback raw key trong release.
* SVG parse được; fillable regions kín; bounds hợp lệ; không rig cycle.
* Mọi animation/anchor/capability reference hợp lệ.
* SHA-256 index được sinh từ bytes thật; không điền checksum giả.
* Mọi story có free resolution path trong store variant.
* Assets premium không khiến free story mất props; props story cốt lõi thuộc base.
* Mọi template có toy interaction; static decor vẫn có tap response.
* Validator fail build nếu thiếu nội dung thuộc mốc đang bàn giao.
* Không dùng 168 bản copy cùng file để qua check số lượng; contact sheet và review silhouette kiểm tra chất lượng riêng.

<a id="s17"></a>

## 17\. Lưu, khôi phục, sao lưu và xuất tác phẩm

### 17.1. Atomic persistence

Không dựa vào Activity.onDestroy để lưu.

* Mỗi document có writer queue nối tiếp; no lost-update khi autosave và manual save chạy cùng lúc.
* Ghi file tạm trong cùng filesystem → flush cần thiết → atomic rename → transaction metadata commit.
* Dùng journal để xử lý khoảng giữa file commit và Room commit; không giả định Room transaction bao phủ filesystem.
* Revision bất biến; metadata current pointer chỉ chuyển khi file đã hợp lệ.
* Startup recovery xử lý orphan temp, orphan revision và broken pointer; ưu tiên bản gần nhất có digest hợp lệ.
* Thumbnail/cache là dẫn xuất; lỗi thumbnail không làm hỏng artwork.
* Autosave sau stroke/operation đã hoàn thành, debounce gợi ý 300–800ms; flush khi đổi màn/profile/pause.
* Với process kill/power loss trước durable commit, có thể mất thao tác chưa commit cuối; công khai giới hạn trong QA, không hứa tuyệt đối không mất một pixel.
* Mục tiêu: không mất các revision đã báo lưu thành công; thao tác chưa commit có cửa sổ nhỏ được đo.
* Không garbage-collect revision còn được scene/book/toy tham chiếu.
* Trash local 7 ngày cho thao tác xóa do người dùng, parent có thể restore hoặc xóa vĩnh viễn; không tự gửi lên cloud.

### 17.2. Sao lưu

Định dạng .colora.zip: manifest schema + profiles/content refs + artwork/revisions/recipes/scenes/books + integrity index. Export snapshot nhất quán, không backup database đang ghi bằng copy mù.

Không chứa PIN, gate token, entitlement, purchase token, secrets, debug logs hoặc analytics raw. Artwork có thể chứa tên bé tự vẽ; parent preview và thông báo bản sao có nội dung cá nhân.

Import:

* Parent gate và SAF picker.
* Giới hạn số entry, compressed/uncompressed bytes và compression ratio.
* Chặn path traversal, symlink, absolute paths, extension/script lạ.
* Kiểm tra manifest/digest/schema trước commit.
* Đọc vào staging, validate toàn bộ, remap IDs khi trùng, commit transaction; lỗi thì rollback, không phá thư viện hiện có.
* Không tin quyền premium từ file; restore entitlement qua Play riêng.
* Missing content version: giữ nguyên snapshot tranh có sẵn, đánh dấu cần asset version phù hợp để sửa/rig; không bỏ tranh.
* Cho import as new profiles hoặc merge có preview; mặc định as new để tránh ghi đè.
* Không tự reset catalog bằng dữ liệu import.

### 17.3. Xuất PNG và bản in

* PNG artwork có nền giấy hoặc transparent theo lựa chọn parent; mặc định không chứa UI, watermark, tên hồ sơ hay metadata nhận dạng.
* Độ phân giải mặc định tối đa 2048px cạnh dài; tùy chọn thấp hơn khi bộ nhớ hạn chế.
* Scene snapshot giữ đúng transforms, màu, z-order và attached objects.
* Tranh nét trắng đen để in là template outline, tách khỏi bản màu.
* PDF khổ A4 và Letter, margin 10–15mm; giữ aspect ratio, không cắt chủ thể.
* Sách PDF có bìa và trang theo thứ tự; text Việt có font/glyph đầy đủ, không dùng raster screenshot UI làm PDF.
* Lưu qua system picker/MediaStore phù hợp; share FileProvider/content URI, không file://.
* Export chạy được offline, có cancel; không tự share ngay sau render.

### 17.4. Xuất video MP4

FULL phải có video ngắn 6–10 giây của scene hoặc toy; dọc 720×1280, ngang 1280×720, 24fps mục tiêu; máy yếu có option 15fps/540p thông báo trong parent area.

* Render deterministic từ immutable snapshot và timeline, không quay màn hình trẻ đang chạm.
* Android encoder/MediaCodec + surface pipeline phù hợp: shared renderer/offscreen frame → encoder input → mux MP4. Nếu cần bridge bitmap→texture, reuse buffers; không giữ toàn bộ frames trong RAM.
* Không giả định Canvas lock trực tiếp mọi codec surface được hỗ trợ; làm prototype encoder và test thiết bị.
* Video mặc định không mic; tùy chọn soundtrack/SFX đã có quyền và chỉ dùng trong app/export theo license.
* Có nhạc thì mux audio có duration/timestamp đúng; không có quyền export âm thanh thì silent export, nêu rõ lựa chọn.
* Handle cancel, insufficient storage, codec not supported; dọn temp, giữ artworks.
* Nếu thiết bị thật không encode được sau capability check, parent có thể chọn PNG/PDF; ghi rõ giới hạn thiết bị, không giả file MP4 bằng đổi extension.
* Không gọi export thành công trước khi file finalize và mở/đọc được.
* Không auto-post Facebook/TikTok/YouTube hoặc gửi dữ liệu ra ngoài.

<a id="s18"></a>

## 18\. Hiệu năng và tương thích

Các giá trị sau là ngân sách kỹ thuật cần đo trên thiết bị tham chiếu, không phải tuyên bố đã đạt.

|Chỉ số|Mục tiêu ban đầu|
|-|-|
|Cold start đến thao tác|≤3 giây trên máy tham chiếu tầm trung|
|Phản hồi nét bút|p95 ≤50ms từ input đến hiển thị, đo bằng công cụ phù hợp|
|Frame tương tác|Hướng tới 60fps; máy yếu fallback 30fps ổn định|
|Main thread work|Không decode/IO/encode dài; theo dõi jank frame thực|
|Bộ nhớ trong studio|Mục tiêu dưới 180MB PSS ở cấu hình tranh chuẩn|
|Bộ nhớ scene|Mục tiêu dưới 250MB PSS với 30 vật; điều chỉnh bằng đo thực|
|Lưu một thay đổi nhỏ|Mục tiêu commit ≤1 giây, không khóa UI|
|Dung lượng tải ban đầu|Mục tiêu ≤150MB compressed cho FULL, kiểm chứng theo split thật|
|Chạy bền|30 phút tô/đổi cảnh không crash, OOM, rò âm thanh|
|Crash-free|Mục tiêu nội bộ ≥99.5% sessions ở pilot có dữ liệu hợp lệ|

Thiết bị tham chiếu phải ghi model/SoC/RAM/Android/resolution. Tối thiểu thử 2GB RAM low-end, 4GB midrange, tablet. Emulator không thay thế đo hiệu năng phần cứng.

Nếu dung lượng vượt ngân sách, tối ưu vector/audio/compression và resource density; không giảm 168 nội dung bằng thumbnail giả. Báo số đo và trade-off.

* Dùng atlas khi phù hợp; tránh texture 4096 không cần thiết.
* Giới hạn live raster layers; 1024×1024 RGBA khoảng 4MiB/buffer, nhiều buffer rất dễ tăng RAM.
* Vector/source giữ độ nét; render cache theo scale cần dùng.
* GC churn phải được profile; không allocate Paint/Path/Matrix mới trên từng điểm/frame.
* Không benchmark/debug overlays trong release.
* Kiểm thử portrait/landscape, tablet, split-screen, font scale, insets, process recreation.
* Settings giảm hiệu ứng không làm đổi artwork hoặc tính năng nội dung.
* Hỗ trợ chế độ offline lạnh sau khi force stop và reboot.

<a id="s19"></a>

## 19\. Kinh doanh và mua nội dung

### 19.1. Quy tắc

* Tô/lưu/undo/hoạt hóa/thế giới cơ bản không thu phí thao tác.
* Không quảng cáo bên thứ ba ở khu trẻ chơi trong thiết kế mặc định.
* Không energy, trả tiền thêm phút, gacha hoặc lời thúc trẻ xin bố mẹ mua.
* Sản phẩm trả phí chỉ xuất hiện trong parent area, nội dung và giá rõ.
* Không auto-renew subscription ở bản đầu. Nếu sau này thêm, đó là thay đổi phạm vi cần review riêng.

### 19.2. Base và expansion

FULL offline mở toàn bộ để bàn giao chức năng. Store: 84 mẫu free (6 đầu ×14) + 84 mẫu expansion. LAUNCH store: 36 free +36 expansion thuộc 6 chủ đề; không quảng cáo đã có đủ 14 khi chưa tới FULL.

Tất cả 7 bối cảnh cơ sở và 28 stories phải chơi được bằng base/free substitutions. Bán nội dung nâng cao, không bán lối thoát khỏi lỗi save.

Đề xuất product IDs theo nhóm:

* pack\_nature\_plus: animals, flowers, nature, ocean, dinosaurs phần expansion.
* pack\_everyday\_plus: family, machines, vehicles, food, home\_city, jobs phần expansion.
* pack\_imagination\_plus: space, fantasy, celebrations phần expansion.

Map catalog tới product rõ và không overlap quyền ngoài dự tính. Không thêm “mua trọn đời tất cả nội dung tương lai” nếu không có cam kết kinh doanh riêng.

Giá là dữ liệu Play ProductDetails theo locale; không hard-code USD hoặc suy đoán giá cho user. Giá thử nghiệm do chủ sản phẩm quyết định trước public.

### 19.3. Billing state machine

States: Unavailable, LoadingProducts, Ready, PurchaseInProgress, Pending, Verifying, Granted, Canceled, Error, RestoreInProgress.

* Play purchase sheet chỉ mở sau gate.
* Pending không grant entitlement.
* Purchase phải được xác minh trước grant và acknowledge theo API/policy hiện hành.
* Entitlement non-consumable; không consume nhầm.
* Query purchases để restore; user-cancel là trạng thái bình thường, không spam.
* Callback trùng xử lý idempotent, token không log plaintext.
* Offline không revoke chỉ vì query lỗi. Khi có xác nhận hoàn tiền/thu hồi hợp lệ thì cập nhật quyền, vẫn giữ tác phẩm do trẻ tạo. Tác phẩm/scene/book đã tạo tiếp tục xem, chơi, sao lưu và xuất được; chỉ tạo mới từ template expansion chưa có quyền bị giới hạn. Không loại asset cần render tác phẩm cũ khi cập nhật pack.
* Không tin backup, local boolean hoặc debug flag là bằng chứng thanh toán.
* Content đã mua phải dùng được offline theo cached grant; restore sau reinstall cần mạng và cùng tài khoản phù hợp.

### 19.4. Giới hạn của “không backend”

Google khuyến nghị xác minh mua hàng và logic nhạy cảm trên backend bảo mật. Không được tuyên bố client-only chống giả mạo/hoàn tiền đầy đủ.

Mặc định tài liệu chọn:

1. FULL gameplay và build offline hoàn toàn không backend.
2. Store billing code/adapter và kiểm thử được chuẩn bị.
3. Nếu chưa có hạ tầng xác minh/cấu hình thương mại thật, không bật bán hàng production; vẫn bàn giao offline FULL.
4. Khi chủ sản phẩm quyết định thương mại hóa, ưu tiên một dịch vụ xác minh tối thiểu tách khỏi gameplay, không dùng để lưu hồ sơ trẻ. Đây là quyết định bổ sung, không tự triển khai dịch vụ mất phí.
5. Nếu chủ sản phẩm chủ động chấp nhận client-only, ghi rõ hạn chế xác minh và không hứa an toàn tương đương server.

Các cấu hình Play Console, tài khoản, product activation, khóa ký và review là phụ thuộc bên ngoài. Ghi blocked nếu chưa có; không dùng fake success để đánh dấu đã hoàn tất production billing.





<a id="s20"></a>

## 20\. Quốc tế hóa và hướng tới 10 triệu lượt tải

### 20.1. Ngôn ngữ và văn hóa

FULL phải có EN/VI hoàn chỉnh cho giao diện, catalog, 28 stories, 30 prompts, parent help và lỗi. Không dịch slug, không ghép chuỗi tùy tiện.

* Locale dùng language resources và content locale bundles.
* Không hard-code chữ vào ảnh; xuất riêng lớp chữ nếu cần cho marketing.
* Chọn locale theo hệ thống khi được hỗ trợ, fallback EN; parent đổi được.
* Locale change không sửa ID hoặc mất scene/story progress.
* Hỗ trợ plural, số, ngày và giá theo locale khi liên quan.
* Kiểm thử pseudolocalization +40% và hướng RTL để chuẩn bị; chưa có bản dịch Arabic thì không quảng cáo hỗ trợ Arabic.
* Mở rộng ES/PT-BR/ID/HI/FR/DE và ngôn ngữ khác theo dữ liệu thị trường, không tự coi máy dịch là đã được kiểm duyệt.
* Gia đình, trang phục, màu da, nghề nghiệp và lễ hội đa dạng, không khuôn mẫu giới.
* Nội dung kiến thức phải được kiểm chứng; không hứa cải thiện IQ hoặc điều trị khó khăn phát triển.

### 20.2. Vị thế cạnh tranh

Mốc tham khảo Google Play quan sát ngày lập tài liệu:

* Coloring Games: Color \& Paint — RV AppStudios: 100M+ lượt tải hiển thị.
* Coloring Book – Games for Kids — Bimi Boo: 10M+ lượt tải hiển thị.
* Crayola Create and Play có hoạt động nghệ thuật, chăm thú cưng và trò chơi với tác phẩm.

Đây là mốc tải tích lũy của đối thủ, không phải người dùng hiện tại, doanh thu, hay bằng chứng ứng dụng mới chắc chắn đạt cùng kết quả. Mẫu “tranh sống dậy” không được tuyên bố độc quyền/chưa ai có.

Định vị cần truyền đạt: **tác phẩm của trẻ được giữ nguyên, dùng để tạo thế giới và kể chuyện, phần cốt lõi offline, phụ huynh kiểm soát**.

### 20.3. Growth loop do phụ huynh chủ động

Trẻ sáng tạo → phụ huynh xem tác phẩm/câu chuyện → tùy chọn xuất/in → tự chia sẻ nếu muốn → người khác khám phá app.

Không auto-share, không kèm tên thật, không ép đánh giá 5 sao. Lời mời review chỉ trong parent area vào thời điểm phù hợp với policy hiện hành, không đổi phần thưởng lấy review.

Marketing deliverables sau khi có app thật:

* Icon app original và một bộ nhận diện.
* 6–8 ảnh screenshot thực, không mock chức năng chưa có.
* Video 15–30 giây: tô mèo tím → lên xe cầu vồng → đi qua khu vườn của bé.
* Store listing EN/VI, sau đó bản địa hóa các thị trường đã chọn.
* Bộ tranh in miễn phí có quyền sử dụng và dẫn tới app.
* Trang hướng dẫn phụ huynh và giáo viên, nói rõ offline/không quảng cáo nếu build phát hành thật đúng như vậy.

Không tự đăng nội dung ra ngoài theo tài liệu này. Chuẩn bị asset/text; owner quyết định công bố.

### 20.4. Quy mô và mốc kiểm chứng

10.000.000 lượt tải / 36 tháng ≈277.778 lượt/tháng trung bình. Đây là phép tính quy mô, không dự báo tăng trưởng tuyến tính hoặc deadline cam kết.

Trình tự:

1. VS: quan sát 10–20 gia đình tự nguyện, có phụ huynh; sửa khả năng hiểu và cảm giác thú vị.
2. LAUNCH: pilot nhỏ 100–300 gia đình nếu có nguồn tuyển, theo dõi sử dụng nhiều tuần; đây là kế hoạch, chưa phải kết quả.
3. Chỉ mở rộng quảng bá sau khi tác phẩm/lưu/retention có bằng chứng tốt và doanh thu/chi phí có mô hình.
4. Mở rộng ngôn ngữ, thư viện, nguồn phân phối và content cadence theo phản hồi.

Không đặt tỷ lệ retention giả như “chuẩn thị trường”. Thiết lập baseline thật rồi chọn mục tiêu. Đo nhu cầu quay lại, không lấy thời lượng càng dài càng tốt.

### 20.5. Đo lường tôn trọng offline

Mặc định: counters/thống kê chỉ nằm trên máy; chỉ số ở đây là hợp đồng để pilot và parent export tự nguyện, không phải yêu cầu nhúng SDK theo dõi.

Sự kiện nội bộ gợi ý:

* first\_canvas\_opened
* first\_artwork\_saved
* first\_toy\_created
* first\_toy\_placed
* story\_started / story\_completed
* artwork\_reused
* scene\_resumed
* export\_completed
* session\_ended\_with\_save
* save\_recovery\_triggered

Không ghi raw strokes, giọng nói, tên tự nhập, vị trí hoặc IDs quảng cáo vào event analytics. Export thống kê qua parent preview chủ động; chỉ dữ liệu cần thiết.

D7/D30 định nghĩa rõ cohort “cài đặt/thiết bị tham gia pilot quay lại đúng ngày tương ứng”; profile không đồng nghĩa một trẻ thật. Với app offline không có analytics được phép, không thể tự suy ra retention toàn cầu từ Google Play install count.

Nếu sau này thêm telemetry online, đó là thay đổi riêng về dữ liệu/consent/SDK cần thiết kế và kiểm tra quy định; không gọi persistent pseudonymous IDs là dữ liệu hoàn toàn vô danh.

Chỉ số quyết định:

* Trẻ có tự chọn tranh và hoàn thành thao tác đầu?
* Có nhận ra đồ chơi là tranh của mình?
* Có dùng lại tác phẩm cũ trong scene/story?
* Quay lại vì muốn tiếp tục sáng tạo hay vì bị nhắc/thúc?
* Phụ huynh có tin tưởng, muốn giới thiệu và sẵn sàng trả cho nội dung?
* Lỗi save/crash/khó chạm đang gây bỏ chơi ở đâu?

<a id="s21"></a>

## 21\. Quy trình sản xuất tài nguyên

### 21.1. Asset manifest và giấy phép

Mỗi asset có: id, file path, type, source/creator, license, commercialUseAllowed, exportUseAllowed, attributionNeeded, size/dimensions, contentVersion, checksum.

Không lấy tranh trên Internet rồi giả định miễn phí. Không dùng mascot, nhân vật phim/game có bản quyền khi chưa được cấp phép. Font, SFX, nhạc và voice cũng phải có license.

Tài nguyên do AI hỗ trợ vẫn cần review hình ảnh, vùng tô, quyền sử dụng theo công cụ và không giống nhân vật có bảo hộ. Không gọi generative AI ở runtime.

### 21.2. Quy trình một template

1. Sketch silhouette đúng đối tượng và style.
2. Chia bộ phận theo motion thực cần có.
3. Thiết kế nét và vùng tô lớn/nhỏ theo độ khó.
4. Tạo vector source + named groups + outlines + closed regions.
5. Chuẩn hóa transforms và fill rules.
6. Đặt pivot, anchor, masks, overlap và hit shapes.
7. Render thumbnail và bản nét in.
8. Gắn preset và thông số riêng.
9. Tô bằng ba palette và vài nét ngẫu nhiên cố định; so sánh toy giữ đúng nét.
10. Chạy validator, kiểm tra contact sheet và animation extreme poses.
11. Ghi catalog locale, audio mapping, license và checksum.
12. Review trên điện thoại thật trước đưa vào pack.

Chỉ gọi một template “xong” khi tô, lưu, reload, hoạt hóa, đặt scene và export đều hoạt động.

### 21.3. Prompt mỹ thuật tham khảo cho công cụ tạo ảnh

Prompt định hướng concept, không thay thế việc tạo vector/region/rig:

> Create an original premium interactive storybook illustration for a children's coloring-and-play app, ages 3–8. Friendly rounded silhouettes, expressive but simple faces, consistent dark navy outlines, soft cream background, cheerful coral/turquoise/yellow accents, subtle paper and crayon texture, clear large shapes, gentle dimensional shading, uncluttered composition. Subject: \[specific subject]. Keep the character recognizable and distinct from other subjects. No text, no logos, no copyrighted characters, no photorealism, no UI, no excessive gradients, no busy background. Deliver concept suitable for manual layered vector preparation.

Prompt UI concept:

> Design a polished mobile children's coloring studio for Colora World. Large central white coloring paper, original cute animal outline, generous touch targets, tactile crayon tools, clearly selected color swatch, calm cream background, navy readable labels, playful but restrained coral and turquoise accents. Landscape and portrait adaptations should preserve the canvas and tool accessibility. Use original icons and illustrations, no emoji placeholders, no ads, no crowded dashboard, no misleading controls.

Không cắt screenshot concept thành app. Implement responsive UI bằng component thật và icon vector thật. Nếu không có image tool, vẽ vector original thủ công bằng path có kiểm chứng; không hạ chuẩn thành 168 hình oval giống nhau.

### 21.4. 36 sticker cơ sở

|Nhóm|6 sticker khác nhau|
|-|-|
|Hình vui|Sao, tim, tròn, tam giác, tia sáng, xoắn ốc|
|Thiên nhiên|Lá, mây, giọt nước, cầu vồng, nấm nhỏ, mặt trời|
|Trang trí|Nơ, ruy băng, chấm bi, cờ nhỏ, huy hiệu, viền hoa|
|Thú cưng|Dấu chân, xương đồ chơi, quả bóng, chuông, bát, chiếc lông|
|Khám phá|Tên lửa nhỏ, hành tinh, kính lúp, la bàn đồ chơi, cờ khám phá, bản đồ|
|Dịp vui|Quà, bóng bay, nến, confetti tĩnh, mũ tiệc, phong bì|

Sticker có design rõ, phù hợp giới hạn màu/layer; pattern không được tính như sticker thứ hai bằng đổi màu.

### 21.5. Nội dung âm thanh và thoại

Tạo inventory tối thiểu:

* UI: 8–12 SFX khác chức năng, âm lượng nhất quán.
* Toy interactions: đủ 15 preset, thêm đặc trưng cho các động vật phổ biến.
* Nhạc: ≥3 loop.
* Voice: lời chào, chọn màu, thử chơi, thêm scene, tự lưu, nghỉ, câu hướng dẫn stories/prompts theo ngôn ngữ. Có mapping line ID và clip thật.
* Không bắt mỗi item phải có một voice clip riêng nếu lời hướng dẫn chung đủ; nhưng label/readout đã quảng cáo phải có nội dung.
* Có build report thiếu clip/key, duration quá dài, clipping và license thiếu.

<a id="s22"></a>

## 22\. Lộ trình triển khai và bàn giao

### 22.1. M0 — Khảo sát và nền tảng

* Audit repository hoặc tạo project mới.
* Ghi quyết định stack/version/package, build flavors, data storage, render prototype.
* Tạo design tokens, component gallery, style board.
* Tạo kế hoạch requirements với mã ID và trạng thái.
* Kiểm chứng build debug và test skeleton; không gọi đây là app hoàn chỉnh.

### 22.2. M1 — Vertical slice đẹp và chạy xuyên suốt

* 12 mẫu: 4 đầu của animals/flowers/vehicles.
* Home, chọn tranh, studio, toy reveal, garden, parent settings cơ bản.
* Fill + crayon + eraser + undo + autosave.
* Tranh thành toy giữ đúng màu; add/reload scene.
* Story ST-01 dùng thỏ, xe, picnic prop base và food prop tạm cho VS; tại LAUNCH thay bằng catalog refs đầy đủ.
* Screenshot + capture thực của một hành trình.
* Sửa mỹ thuật và latency trước khi nhân nội dung.

VS props là tài nguyên scene thật; không được để chúng thay thế catalog FULL cuối cùng.

### 22.3. M2 — LAUNCH 72 mẫu

* Hoàn thành 6 chủ đề đầu và 2 worlds.
* 12 stories ST-01–12 với free fallback đủ (home/food props base ngoài 72 khi cần).
* Tất cả công cụ tô, gallery, profile, timer, parent gate, backup, PNG/PDF.
* EN/VI hoạt động đầy đủ cho scope này.
* Persistence tests, process death, offline, low-end QA.
* Không dừng ở đây nếu nhiệm vụ yêu cầu FULL.

### 22.4. M3 — FULL gameplay và content

* Đủ 168 mẫu/14 chủ đề, 7 worlds, 28 stories, 30 prompts.
* Các props thay thế khi catalog phù hợp có sẵn vẫn giữ stable ID để save cũ không gãy.
* Character Builder ≥44 parts, Color Lab, 36 stickers.
* StoryBook, cross-theme interactions, local suggestions.
* Video MP4 và audio export có license.
* Hoàn thiện hình ảnh/âm thanh cho tất cả màn hình và nội dung, không để “coming soon”.

### 22.5. M4 — Polish, QA, thương mại và bàn giao

* Review screenshot từng form factor, sửa khả năng đọc/gesture.
* Chạy automated và manual QA theo mục 23.
* Store billing adapter + tests và ghi rõ external blockers; offlineFull phải bàn giao chạy thật.
* Kiểm tra permission, backup rules, licenses, privacy text và source attribution.
* Chuẩn bị store assets từ app thật; không tự public.
* Build APK debug cài được, AAB/release theo signing có sẵn; không tạo khóa production rồi giấu người dùng.
* Cập nhật README, kỹ thuật, QA report, content report, known issues.

### 22.6. Bộ bàn giao trong repository

|File/thư mục|Nội dung|
|-|-|
|README.md|Setup, build/run, flavors, thiết bị hỗ trợ, hướng dẫn cài|
|docs/MASTER\_SPEC.md|Bản đặc tả này|
|docs/DECISIONS.md|ADR và các trade-off đã chọn|
|docs/IMPLEMENTATION\_STATUS.md|Mã yêu cầu, done/partial/blocked, evidence|
|docs/CONTENT\_PIPELINE.md|Cách thêm tranh/world/story/voice|
|docs/ASSET\_LICENSES.md|Nguồn và quyền từng nhóm asset|
|docs/DATA\_AND\_PRIVACY.md|Dữ liệu, SDK, permission, backup và xóa|
|docs/QA\_REPORT.md|Test thực chạy, thiết bị, lệnh, kết quả, lỗi|
|docs/RELEASE\_CHECKLIST.md|Signing, Play Console, billing, policy, localization|
|docs/KNOWN\_ISSUES.md|Giới hạn thật và mức ảnh hưởng|
|content/ hoặc assets/|Tài nguyên production + manifest + locales|
|content-tools/|Validators và contact sheet generators|
|screenshots/|Ảnh thực từ các màn hình/form factor|
|build artifacts|APK cài được; AAB/release khi đủ signing|

Bàn giao không chứa service-account key, production keystore hoặc mật khẩu trong git.

### 22.7. Prompt làm việc qua nhiều phiên

Mỗi lần kết thúc phiên, ghi:

* commit/trạng thái working tree;
* mốc đang làm;
* tài nguyên/code đã hoàn thành;
* test thật đã chạy;
* lỗi chưa giải quyết;
* 3–5 bước kế tiếp cụ thể;
* phần bị chặn bởi tài khoản/quyền hoặc công cụ.

Phiên tiếp theo đọc checkpoint và tiếp tục, không dựng lại project hoặc tự giảm scope.

<a id="s23"></a>

## 23\. Kiểm thử, nghiệm thu và Definition of Done

### 23.1. Ma trận yêu cầu trọng yếu

|ID|Yêu cầu|Bằng chứng nghiệm thu|
|-|-|-|
|REQ-01|Mỹ thuật nhất quán và đẹp|Style board + screenshots thật; review tiêu chí mục 3|
|REQ-02|Tô chính xác, không leak|Fixtures vùng kín, biên, zoom, gesture|
|REQ-03|Nét vẽ giữ nguyên trong toy|Golden images palette/nét test và video toy|
|REQ-04|Lưu đáng tin cậy|Kill/restart, IO error, migration tests|
|REQ-05|Thế giới cross-theme|Test xe chở mèo, máy xúc trồng hoa, rocket chở robot|
|REQ-06|Nội dung FULL|Validator 14/168/7/28/30, contact sheets|
|REQ-07|Builder và Color Lab|Recipe roundtrip, socket tests, trộn màu xác định|
|REQ-08|Sách và export|PDF đủ trang, PNG đúng màu, MP4 mở và seek được|
|REQ-09|Phụ huynh và timer|Gate mọi exit point, time/pause/resume, cancel|
|REQ-10|Offline|Cold start/reboot/force stop khi airplane mode|
|REQ-11|Accessibility|Labels, font scale, reduced motion, TalkBack test thật|
|REQ-12|Quốc tế hóa|Không raw key; EN/VI; dấu Việt; locale switch|
|REQ-13|Hiệu năng|Báo cáo máy thật, memory/jank/input measurements|
|REQ-14|Mua hàng thật|Play test hoặc external blocker, fake không có release|
|REQ-15|Không thu dữ liệu ngoài ý định|Manifest/SDK/network/backup review|
|REQ-16|Bàn giao có thể tiếp tục|Build từ README, content tools, report và known issues|

### 23.2. Unit/content tests

* Region hit-test: inside, outside, boundary, overlap, transforms, zoom.
* Clip: stroke không rò sang region khác khi trợ giúp bật.
* Seeded brush render giống sau serialization/reload.
* Fill/eraser/sticker/undo/redo replay đúng.
* Artwork revisions immutable; scene/book reference không tự đổi.
* Scene attachment không cycle, không nhiều parent, detach giữ vị trí hợp lệ.
* Story event idempotence; resume đúng step; old artwork được chấp nhận.
* Free-path resolver cho đủ 28 stories.
* Content counts, refs, versions, hashes, locales, licenses.
* Backup validation: zip slip, quá dung lượng, thiếu asset, corrupted hash, duplicate IDs, transaction rollback.
* DB migrations từ từng schema đã phát hành; không destructive migration để “sửa nhanh”.
* Timer active duration, background, process recreation, parent override.
* Entitlement không restore từ backup; pending không grant; callbacks trùng không duplicate.

Không viết test chỉ lặp lại constant để tăng số lượng. Ưu tiên lỗi gây mất tranh, sai màu, khóa nội dung và thao tác nhầm.

### 23.3. Instrumentation và end-to-end

**E2E-A — Trải nghiệm đầu:** cài sạch → không đăng nhập → chọn mèo → tô ba màu → undo/redo → chơi → đặt garden → force stop → mở lại, đúng màu/vị trí.

**E2E-B — Cross-theme:** tạo xe và thỏ → lên xe → đi waypoint → gỡ xuống → lưu/reload → attachment đúng.

**E2E-C — Story:** ST-01 dùng tác phẩm cũ → hoàn thành → tạo trang sách → tiếp tục sau app restart.

**E2E-D — Free catalog:** store chưa mua → hoàn thành ST-06/ST-07/ST-14/ST-16 bằng fallback → không paywall bắt buộc.

**E2E-E — Phụ huynh:** trẻ chạm share/mua/link/delete → gate → cancel không thoát ngoài; session timeout/background cần xác thực lại.

**E2E-F — Offline:** airplane mode từ cold start → tô/scene/story/book/export đều dùng được; store restore báo cần mạng ở parent area.

**E2E-G — Backup:** export profile → import vào app sạch → đủ tranh/scenes/books; không nhập quyền mua/PIN.

**E2E-H — Export:** PNG trong suốt/giấy; PDF A4/Letter; MP4 dọc/ngang; tên file rõ, mở được, không UI cá nhân.

**E2E-I — Profile:** hai profile tạo cùng template → gallery/scene/timer tách; đổi khi autosave đang chạy không ghi nhầm.

**E2E-J — Accessibility:** 200% font ở parent area; reduced motion; mute; TalkBack chọn tool/màu và fill vùng hỗ trợ.

### 23.4. Tình huống lỗi cần thử

* Xoay màn hình giữa stroke và giữa story.
* Multi-touch, tap nhanh, pointer cancel, stylus nếu có.
* Home/back liên tiếp, cuộc gọi/audio focus, lock screen.
* Mất process khi đang ghi file, lúc commit metadata, lúc tạo thumbnail.
* Ổ gần đầy, write permission/provider bị từ chối, export cancel.
* Cache bị xóa, file revision hỏng, thiếu asset version cũ.
* Import file lớn/lỗi, duplicate IDs, migration interrupted.
* Product details loading lỗi, purchase canceled/pending/duplicate, restore offline.
* Máy không có Google Play Services chạy offlineFull.
* Thu hồi quyền mua đã xác minh: giữ artwork người dùng, không crash mở scene cũ.
* Đổi locale/font scale trong lúc có draft.
* Không có audio output hoặc thiếu system TTS voice: chơi được.

### 23.5. Kiểm thử hình ảnh

* Contact sheet đủ 168 tranh: outline, sample paint và toy.
* So sánh palette/texture trước và sau rig/export với tolerance có giải thích.
* Animation extreme pose: tay/chân/đuôi không hở hoặc cắt nét.
* Screenshot Home/Studio/World/Gallery/Book/Parents trên 3 layout classes.
* Không clipping chữ, button dưới system gesture, trạng thái selected mất, thumbnail méo.
* Không tính screenshot mockup là ảnh chạy thật.
* Tài nguyên có checklist art review; kiểm đếm tự động không thay thế review thẩm mỹ.

### 23.6. Build commands dự kiến

Điều chỉnh đúng tên module/flavor thực trong README, ví dụ:

\~\~\~bash
./gradlew :app:assembleOfflineFullDebug
./gradlew :app:testOfflineFullDebugUnitTest
./gradlew :app:lintOfflineFullDebug
./gradlew :app:connectedOfflineFullDebugAndroidTest
\~\~\~

Có content validation task tích hợp build, ví dụ validateContent, và command tạo contact sheets. Đây là command dự kiến cho implementation, không phải các test đã chạy trong lúc viết đặc tả.

Nếu không có emulator/SDK, vẫn hoàn thành phần code/artifact được phép, ghi rõ check chưa chạy và lý do; không khẳng định APK đã chạy.

### 23.7. Definition of Done — FULL

* \[ ] Android project build được từ hướng dẫn bằng phiên bản đã pin.
* \[ ] offlineFull cài và chơi end-to-end không mạng.
* \[ ] UI đẹp/nhất quán, assets original, không placeholder cuối.
* \[ ] 168 mẫu thật, 14 nhóm, 7 worlds, 28 stories, 30 prompts.
* \[ ] ≥44 parts, ≥36 stickers, props/audio/fonts có license.
* \[ ] Công cụ tô, undo, fill clipping, save/reload đúng.
* \[ ] Toy giữ nguyên nét và màu; cross-theme hoạt động.
* \[ ] Worlds, stories, builder, color lab, collection, books hoàn chỉnh.
* \[ ] Parent gates, profiles, timer và reduced motion hoạt động.
* \[ ] PNG/PDF/MP4 export thật; thiết bị không hỗ trợ có fallback trung thực.
* \[ ] Backup/import an toàn và không phá dữ liệu cũ.
* \[ ] EN/VI đầy đủ, accessibility đã kiểm tra và giới hạn ghi rõ.
* \[ ] QA/performance report có bằng chứng; không bịa số đo.
* \[ ] Không secret, ad SDK, tracker hoặc permission không được yêu cầu.
* \[ ] Store adapter không giả; external blockers được liệt kê riêng.
* \[ ] README, status, licenses, release checklist, known issues đầy đủ.
* \[ ] Chưa public nếu người dùng chưa yêu cầu.
* \[ ] Báo cáo tách “FULL offline hoàn tất” khỏi “production store/billing sẵn sàng”.

<a id="s24"></a>

## 24\. Rủi ro, quyết định và những việc không được làm

### 24.1. Rủi ro sản phẩm và giải pháp

|Rủi ro|Biện pháp|
|-|-|
|Đẹp trong mockup, khó dùng trên máy|Kiểm thử tương tác và screenshot từ app thật sớm|
|Quá nhiều nội dung nhưng không khác biệt|Khóa art style, silhouette review, mỗi nhóm có cách chơi|
|Trẻ chỉ tô một lần|Tác phẩm dùng lại trong scene/story/book; thử nghiệm quay lại thật|
|Mất tranh gây mất niềm tin|Atomic revisions, recovery, backup; ưu tiên QA dữ liệu|
|Animations làm tranh khác bản gốc|Texture từ paint layer và golden tests|
|Native 2D scene khó tối ưu|Prototype 30 toys sớm, cache có ngân sách, ít physics|
|168 mẫu quá nặng sản xuất|Theo milestone, content pipeline/validator; không giả số lượng|
|Không backend nhưng muốn billing an toàn|Tách game offline với quyết định xác minh thương mại|
|Offline nhưng muốn đo retention toàn cầu|Pilot/parent export tự nguyện; không giả analytics có sẵn|
|Trẻ lớn lên hoặc đổi sở thích|Độ khó tăng, builder/books, nội dung mở rộng theo phản hồi|
|Thị trường có app miễn phí mạnh|Giá trị riêng + base đủ tốt + thử willingness-to-pay|
|Chia sẻ làm lộ thông tin trong tranh|Parent preview; không tự gửi; không giả tranh luôn vô danh|

### 24.2. Không thuộc FULL mặc định

LATER: AR camera, sinh tranh bằng AI trực tiếp, tự nhận diện nét vẽ thành nhân vật có xương, upload cộng đồng, chat, multiplayer online, cloud sync, thuê bao, live operations backend, trường học quản lý tài khoản trẻ.

Không xây các hệ thống này trước khi vòng tô → chơi → lưu → quay lại đạt chất lượng.

### 24.3. Các điều cấm triển khai

* Không dùng cả app như một danh sách ảnh tĩnh rồi báo “thế giới tương tác”.
* Không auto-color lại theo mẫu làm mất nét của trẻ.
* Không buộc tô kín/đúng màu để mở chơi.
* Không dùng 168 ảnh đổi màu từ cùng một hình để đạt số lượng.
* Không hiển thị khóa/popup mua dày đặc trước mặt trẻ.
* Không tạo nút export/purchase/share chỉ hiện toast thành công.
* Không thêm quyền mic/camera vì có thổi nến hoặc mascot.
* Không auto-delete artwork khi cache đầy hoặc quyền mua thay đổi.
* Không ghi dữ liệu vào thư mục tạm rồi coi là lưu bền.
* Không thu dữ liệu trẻ vì “SDK mặc định”.
* Không hứa 10 triệu tải, doanh thu, retention hoặc tác dụng giáo dục đã được chứng minh.
* Không hứa billings/backup/cloud an toàn tuyệt đối khi chưa kiểm tra.
* Không dừng ở milestone VS/LAUNCH nếu yêu cầu là FULL.

<a id="s25"></a>

## 25\. Nguồn tham khảo và prompt khởi động

### 25.1. Nguồn chính thức

Các nguồn được dùng để định hướng/kiểm tra trong quá trình lập tài liệu ngày 17/11/2026. Claude phải kiểm tra lại thông tin có thể thay đổi trước build/release. Phần thiết kế, số lượng nội dung và ngân sách là đề xuất của tài liệu này.

1. [RV AppStudios — Coloring Games: Color \& Paint trên Google Play](https://play.google.com/store/apps/details?id=com.rvappstudios.kids.coloring.book.color.painting): mốc tải và giá trị miễn phí tham khảo.
2. [Bimi Boo — Coloring Book trên Google Play](https://play.google.com/store/apps/details?id=com.bimiboo.coloring): mốc tải và nội dung tô màu tham khảo.
3. [Crayola Create and Play trên Google Play](https://play.google.com/store/apps/details?id=com.crayolallc.crayola_create_and_play): cho thấy chơi với tác phẩm/thú cưng đã có trên thị trường.
4. [Google Play Families Policies](https://support.google.com/googleplay/android-developer/answer/9893335?hl=en): yêu cầu với ứng dụng hướng đến trẻ, SDK và quảng cáo.
5. [FTC — COPPA Six-Step Compliance Plan](https://www.ftc.gov/business-guidance/resources/childrens-online-privacy-protection-rule-six-step-compliance-plan-your-business): hướng dẫn tham khảo cho dữ liệu trẻ; parent gate không tự thay thế yêu cầu consent.
6. [Android — Graphics in Compose](https://developer.android.com/develop/ui/compose/graphics/draw/overview): graphics và rendering.
7. [Android — Using Views in Compose](https://developer.android.com/develop/ui/compose/migrate/interoperability-apis/views-in-compose): AndroidView cho custom drawing surface.
8. [Android — Auto Backup](https://developer.android.com/identity/data/autobackup): quy tắc backup/exclusion và khác biệt phiên bản.
9. [Android — Play Billing security](https://developer.android.com/google/play/billing/security): khuyến nghị xác minh giao dịch và hạn chế client-side.
10. [Android — Integrate Play Billing](https://developer.android.com/google/play/billing/integrate): luồng API và trạng thái giao dịch cần kiểm tra ở thời điểm triển khai.



### 


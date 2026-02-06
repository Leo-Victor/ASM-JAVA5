package com.poly.ASM.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.poly.ASM.dao.ProductDAO;
import com.poly.ASM.model.Product;
import org.springframework.beans.factory.annotation.Autowired;
// [QUAN TRỌNG] Đã sửa lại các dòng import đúng của Spring Framework
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class GeminiService {

    @Autowired
    ProductDAO productDAO;

    // API Key của bạn
    private static final String GEMINI_API_KEY = "AIzaSyChBrLllJ8sTIMkOmA7KG0Q3r_n9hqEOUE";

    // Dùng bản Gemini 2.5 Flash (Mới nhất, nhanh và thông minh)
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + GEMINI_API_KEY;

    public String getChatResponse(String userMessage) {
        try {
            // 1. Lấy dữ liệu sản phẩm mới nhất từ Database
            List<Product> products = productDAO.findAll();

            // Cấu hình định dạng tiền tệ Việt Nam (Ví dụ: 10.000.000)
            // Việc này giúp AI nhận diện con số chính xác hơn là để số thô (10000000)
            NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

            // Tạo danh sách sản phẩm dưới dạng văn bản để gửi cho AI
            String productContext = products.stream()
                    .map(p -> {
                        String categoryName = (p.getCategory() != null) ? p.getCategory().getName() : "Khác";
                        String priceStr = currencyFormat.format(p.getPrice()); // Format giá tiền

                        // Cấu trúc: - Tên sản phẩm (Giá: ... VNĐ, Loại: ...)
                        return String.format("- %s (Giá: %s VNĐ, Loại: %s)",
                                p.getName(), priceStr, categoryName);
                    })
                    .collect(Collectors.joining("\n"));

            // 2. Tạo Kịch bản (Prompt) chi tiết cho AI
            // Thêm hướng dẫn so sánh giá để AI không bị "ngáo" khi tìm hàng giá rẻ
            String systemPrompt = "Bạn là trợ lý ảo bán hàng chuyên nghiệp của Tech Store. " +
                    "Dưới đây là danh sách sản phẩm hiện có trong kho (Giá đã niêm yết):\n" + productContext + "\n\n" +
                    "QUY TẮC TRẢ LỜI QUAN TRỌNG:\n" +
                    "1. So sánh giá cẩn thận: Hãy hiểu rằng '6.500.000' nhỏ hơn '10.000.000'.\n" +
                    "2. Khi khách hỏi 'dưới X tiền', hãy tìm tất cả sản phẩm có giá thấp hơn mức đó.\n" +
                    "3. Nếu tìm thấy sản phẩm phù hợp, hãy liệt kê tên và giá của nó ra.\n" +
                    "4. Trả lời ngắn gọn, thân thiện, sử dụng emoji vui vẻ.\n" +
                    "5. Nếu không có sản phẩm nào phù hợp yêu cầu, hãy gợi ý sản phẩm gần giống nhất.\n\n" +
                    "Câu hỏi của khách hàng: " + userMessage;

            // 3. Cấu hình Header gửi đi
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 4. Tạo JSON Body bằng thư viện Jackson (An toàn tuyệt đối với ký tự đặc biệt)
            ObjectMapper mapper = new ObjectMapper();

            // Tạo cấu trúc JSON theo chuẩn Google Gemini
            ObjectNode rootNode = mapper.createObjectNode();
            ArrayNode contentsArray = rootNode.putArray("contents");
            ObjectNode contentNode = contentsArray.addObject();
            ArrayNode partsArray = contentNode.putArray("parts");
            ObjectNode textPart = partsArray.addObject();

            // Đưa kịch bản vào JSON
            textPart.put("text", systemPrompt);

            // Chuyển đối tượng Java thành chuỗi JSON
            String requestJson = mapper.writeValueAsString(rootNode);

            // 5. Gọi API Google Gemini
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
            RestTemplate restTemplate = new RestTemplate();

            String response = restTemplate.postForObject(API_URL, entity, String.class);

            // 6. Phân tích kết quả trả về từ Google
            JsonNode responseNode = mapper.readTree(response);
            return responseNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

        } catch (HttpClientErrorException e) {
            // In lỗi chi tiết ra cửa sổ Run để dễ sửa (nếu có)
            System.err.println("=== LỖI API GEMINI (HTTP " + e.getStatusCode() + ") ===");
            System.err.println("Chi tiết lỗi: " + e.getResponseBodyAsString());
            System.err.println("======================================================");

            return "Hệ thống AI đang gặp sự cố kết nối. Vui lòng chuyển sang tab 'Gặp nhân viên' để được hỗ trợ ngay ạ.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Xin lỗi, hiện tại hệ thống AI đang bận một chút. Bạn thử lại sau nhé! 😅";
        }
    }
}
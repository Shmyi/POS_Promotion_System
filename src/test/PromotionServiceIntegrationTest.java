package test;

import model.CartItem;
import model.Receipt;
import service.PromotionService;

import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 整合測試：測試 PromotionService 是否正確套用所有優惠活動
 * 執行時會印出每個測試結果。
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PromotionServiceIntegrationTest {

    private static PromotionService service;
    private static Date testDate;

    @BeforeAll
    static void setup() throws Exception {
        service = new PromotionService();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // 把日期改在活動期間內（10 月底所有活動都有效）
        testDate = sdf.parse("2025-10-25"); 
        System.out.println("=== 測試開始，日期：" + sdf.format(testDate) + " ===");
    }

    @Test
    @Order(1)
    void testCosmeticPromotion() {
        System.out.println("\n[測試1] 化妝品滿三千送三百");
        List<CartItem> cart = List.of(
                new CartItem("COSM001", 1),
                new CartItem("COSM002", 1)
        );
        Receipt r = service.calculateReceipt(cart, testDate, false);
        System.out.println("促銷折扣金額：" + r.getTotalPromotionDiscount());

        Assertions.assertTrue(
                r.getTotalPromotionDiscount().compareTo(new BigDecimal("300")) >= 0,
                "應觸發化妝品滿三千送三百"
        );
        System.out.println("成功觸發化妝品優惠（滿三千送三百）");
    }

    @Test
    @Order(2)
    void testPerfumePromotion() {
        System.out.println("\n[測試2] 香水節滿五千送五百");
        List<CartItem> cart = List.of(
                new CartItem("PERF001", 1),
                new CartItem("PERF002", 1)
        );
        Receipt r = service.calculateReceipt(cart, testDate, false);
        System.out.println("促銷折扣金額：" + r.getTotalPromotionDiscount());

        Assertions.assertTrue(
                r.getTotalPromotionDiscount().compareTo(new BigDecimal("500")) >= 0,
                "應觸發香水節滿五千送五百"
        );
        System.out.println("成功觸發香水節優惠（滿五千送五百）");
    }

    @Test
    @Order(3)
    void testLiquorPromotion_CompanyMember() {
        System.out.println("\n[測試3] 酒類滿八千送八百（公司會員95折）");
        List<CartItem> cart = List.of(
                new CartItem("WINE001", 1),
                new CartItem("LIQUOR001", 1)
        );
        Receipt r = service.calculateReceipt(cart, testDate, true);
        System.out.println("促銷折扣金額：" + r.getTotalPromotionDiscount());
        System.out.println("總折扣金額：" + r.getTotalDiscountAmount());

        Assertions.assertTrue(
                r.getTotalPromotionDiscount().compareTo(new BigDecimal("800")) >= 0,
                "應觸發酒類滿八千送八百"
        );
        Assertions.assertTrue(
                r.getTotalDiscountAmount().compareTo(new BigDecimal("800")) > 0,
                "公司會員應享95折"
        );
        System.out.println("成功觸發酒類優惠（滿八千送八百 + 公司會員95折）");
    }

    @Test
    @Order(4)
    void testAllStorePromotion() {
        System.out.println("\n[測試4] 全館滿萬送千");
        List<CartItem> cart = List.of(
                new CartItem("ELEC002", 2),
                new CartItem("FOOD001", 5),
                new CartItem("BOOK001", 3)
        );
        Receipt r = service.calculateReceipt(cart, testDate, false);
        System.out.println("促銷折扣金額：" + r.getTotalPromotionDiscount());

        Assertions.assertTrue(
                r.getTotalPromotionDiscount().compareTo(new BigDecimal("1000")) >= 0,
                "應觸發全館滿萬送千"
        );
        System.out.println("成功觸發全館滿萬送千");
    }

    @Test
    @Order(5)
    void testElectronicsPromotion() {
        System.out.println("\n[測試5] 3C電子滿五千折五百");
        List<CartItem> cart = List.of(new CartItem("ELEC001", 1));
        Receipt r = service.calculateReceipt(cart, testDate, false);
        System.out.println("促銷折扣金額：" + r.getTotalPromotionDiscount());

        Assertions.assertTrue(
                r.getTotalPromotionDiscount().compareTo(new BigDecimal("500")) >= 0,
                "應觸發3C電子滿五千折五百"
        );
        System.out.println("成功觸發3C電子優惠（滿五千折五百）");
    }

    @AfterAll
    static void finish() {
        System.out.println("\n=== 所有優惠測試完成 ===");
    }
}

// test/FullSystemTest.java
package test;

import model.CartItem;
import model.Line;
import model.Receipt;
import service.PromotionService;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class FullTest {
    public static void main(String[] args) throws Exception {
        System.out.println("=== POS 全系統測試開始 ===");
        Date transactionDate = new SimpleDateFormat("yyyy-MM-dd").parse("2025-10-30");

        // === 測試 1: 公司會員（應觸發所有折扣）===
        runScenario(true, transactionDate);

        // === 測試 2: 一般顧客（不享會員折扣，只測活動）===
        runScenario(false, transactionDate);
    }

    private static void runScenario(boolean isCompanyMember, Date transactionDate) throws Exception {
        System.out.println("\n==============================================================");
        System.out.println("測試場景: " + (isCompanyMember ? "公司會員（含95折）" : "一般顧客（無會員折扣）"));
        System.out.println("==============================================================");

        PromotionService service = new PromotionService();

        // 購買組合 — 涵蓋所有活動類別
        List<CartItem> cartItems = Arrays.asList(
                new CartItem("WINE001", 2),  // 酒類（09）
                new CartItem("COSM001", 2),  // 化妝品（01）
                new CartItem("PERF001", 2),  // 香水（02）
                new CartItem("ELEC001", 1)   // 3C電子（05）
        );

        System.out.println("輸入測試商品:");
        for (CartItem item : cartItems) {
            System.out.printf("  - %s x %d%n", item.getItemCode(), item.getQuantity());
        }

        Receipt receipt = service.calculateReceipt(cartItems, transactionDate, isCompanyMember);
        printDetailedReceipt(receipt, isCompanyMember);
        validateTestResults(receipt, isCompanyMember);
    }

    private static void printDetailedReceipt(Receipt receipt, boolean isCompanyMember) {
        System.out.println("\n============================ 收據 ============================");
        System.out.printf("會員身份: %s%n", isCompanyMember ? "公司會員（酒類95折）" : "一般顧客");
        System.out.println("--------------------------------------------------------------");
        System.out.printf("%-10s %-25s %-8s %-8s %-10s %-10s %-10s%n",
                "品號", "品名", "分類", "數量", "原價", "會員價", "最終價");

        for (Line line : receipt.getLines()) {
            System.out.printf("%-10s %-25s %-8s %-8d %-10.0f %-10.0f %-10.0f%n",
                    line.getItemCode(),
                    shorten(line.getItemName(), 22),
                    line.getCategory(),
                    line.getQuantity(),
                    line.getOriginalAmount().doubleValue(),
                    line.getOriginalAmount().subtract(line.getMemberDiscount()).doubleValue(),
                    line.getFinalAmount().doubleValue());
        }

        System.out.println("--------------------------------------------------------------");
        System.out.printf("%-50s %10.0f%n", "原價總計:", receipt.getTotalOriginalAmount().doubleValue());
        System.out.printf("%-50s %10.0f%n", "會員折扣後總計:", receipt.getTotalMemberAmount().doubleValue());
        System.out.printf("%-50s %10.0f%n", "促銷折扣:", receipt.getTotalPromotionDiscount().doubleValue());
        System.out.printf("%-50s %10.0f%n", "總折扣金額:", receipt.getTotalDiscountAmount().doubleValue());
        System.out.printf("%-50s %10.0f%n", "最終應付金額:", receipt.getFinalAmount().doubleValue());
        System.out.println("==============================================================");

        if (!receipt.getActivityDiscounts().isEmpty()) {
            System.out.println("套用的促銷活動:");
            for (Map.Entry<String, BigDecimal> entry : receipt.getActivityDiscounts().entrySet()) {
                System.out.printf("  - %s：-%.0f 元%n", entry.getKey(), entry.getValue().doubleValue());
            }
        }

        // 折扣明細
        System.out.println("\n折扣明細:");
        boolean hasDiscount = false;
        for (Line line : receipt.getLines()) {
            BigDecimal totalDiscount = line.getMemberDiscount().add(line.getPromotionDiscount());
            if (totalDiscount.compareTo(BigDecimal.ZERO) > 0) {
                hasDiscount = true;
                System.out.printf("  %-20s | 會員折扣: %-6.0f | 促銷折扣: %-6.0f | 總折扣: %-6.0f%n",
                        shorten(line.getItemName(), 20),
                        line.getMemberDiscount().doubleValue(),
                        line.getPromotionDiscount().doubleValue(),
                        totalDiscount.doubleValue());
            }
        }
        if (!hasDiscount) System.out.println("  無折扣應用");
    }

    private static void validateTestResults(Receipt receipt, boolean isCompanyMember) {
        System.out.println("\n===================== 驗證結果 =====================");
        boolean allPassed = true;

        // 驗證1: 酒類會員折扣
        boolean wineTest = checkWineDiscount(receipt, isCompanyMember);
        System.out.println((wineTest ? "✅" : "❌") + " 酒類會員折扣測試");
        allPassed &= wineTest;

        // 驗證2: 非酒類不應有會員折扣
        boolean nonWineTest = checkNonWineDiscount(receipt);
        System.out.println((nonWineTest ? "✅" : "❌") + " 非酒類會員折扣測試");
        allPassed &= nonWineTest;

        // 驗證3: 活動促銷金額有正值
        boolean promoTest = receipt.getTotalPromotionDiscount().compareTo(BigDecimal.ZERO) > 0;
        System.out.println((promoTest ? "✅" : "❌") + " 活動促銷觸發測試");
        allPassed &= promoTest;

        // 驗證4: 金額計算一致
        boolean amountTest = checkAmountCalculation(receipt);
        System.out.println((amountTest ? "✅" : "❌") + " 金額計算一致性測試");
        allPassed &= amountTest;

        System.out.println("====================================================");
        System.out.println(allPassed ? "✅ 所有測試通過！" : "❌ 部分測試失敗！");
        System.out.println("====================================================");
    }

    private static boolean checkWineDiscount(Receipt receipt, boolean isCompanyMember) {
        boolean ok = true;
        for (Line line : receipt.getLines()) {
            if ("酒類".equals(line.getCategory()) || "09".equals(line.getCategoryCode())) {
                if (isCompanyMember && line.getMemberDiscount().compareTo(BigDecimal.ZERO) == 0) {
                    System.out.println("  ⚠️ 酒類商品未套用會員折扣: " + line.getItemName());
                    ok = false;
                }
                if (!isCompanyMember && line.getMemberDiscount().compareTo(BigDecimal.ZERO) > 0) {
                    System.out.println("  ❌ 非會員卻有酒類折扣: " + line.getItemName());
                    ok = false;
                }
            }
        }
        return ok;
    }

    private static boolean checkNonWineDiscount(Receipt receipt) {
        for (Line line : receipt.getLines()) {
            if (!"酒類".equals(line.getCategory()) && !"09".equals(line.getCategoryCode())) {
                if (line.getMemberDiscount().compareTo(BigDecimal.ZERO) > 0) {
                    System.out.println("  ❌ 非酒類商品錯誤套用會員折扣: " + line.getItemName());
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean checkAmountCalculation(Receipt receipt) {
        BigDecimal total = BigDecimal.ZERO;
        for (Line line : receipt.getLines()) {
            total = total.add(line.getFinalAmount());
        }
        return total.compareTo(receipt.getFinalAmount()) == 0;
    }

    private static String shorten(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength - 3) + "..." : text;
    }
}

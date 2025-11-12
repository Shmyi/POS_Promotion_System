package Main;

import model.CartItem;
import model.Line;
import model.Receipt;
import service.PromotionService;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import util.TextAlignUtil;

/**
 * 主程式：POS 促銷計算系統
 * ------------------------------------------------------------
 * 功能說明：
 *  1. 讓使用者輸入購買商品與數量
 *  2. 判斷會員身份（公司會員享酒類95折）
 *  3. 透過 PromotionService 進行資料庫查詢與促銷攤提折扣計算
 *  4. 印出完整收據與折扣明細
 * ------------------------------------------------------------
 * 作者：yi chen
 * 建立日期：2025-11-10
 */
public class App {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 取得當下系統時間
        Date transactionDate = new Date();
        SimpleDateFormat sdfDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.TAIWAN);
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.TAIWAN);
        
        // 問使用者會員身份
        boolean isCompanyMember = askMembership(scanner);

        System.out.println("\n=== POS 促銷計算系統 ===");
        System.out.println("交易日期: " + sdfDateTime.format(transactionDate));
        System.out.println("會員身份: " + (isCompanyMember ? "公司會員（酒類95折）" : "一般顧客"));
        System.out.println();
        
        // 輸入購買商品清單
        List<CartItem> cartItems = readUserInput(scanner);

        if (cartItems.isEmpty()) {
            System.out.println("未輸入任何商品，程式結束。");
            return;
        }

        try {
        	// 呼叫促銷服務
            PromotionService service = new PromotionService();
            Receipt receipt = service.calculateReceipt(cartItems, transactionDate, isCompanyMember);
            
            // 輸出收據
            printReceipt(receipt, sdfDate.format(transactionDate), isCompanyMember);

        } catch (Exception e) {
            System.err.println("計算過程中發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }   
    }
    
    /**
     * 詢問使用者會員身份
     */
    private static boolean askMembership(Scanner scanner) {
        while (true) {
            System.out.print("請選擇會員身份 (1: 一般顧客, 2: 公司會員): ");
            String input = scanner.nextLine().trim();
            if ("1".equals(input)) return false;
            if ("2".equals(input)) return true;
            System.out.println("輸入錯誤，請重新選擇。");
        }
    }

    /**
     * 讀取使用者輸入的購買商品與數量
     */
    private static List<CartItem> readUserInput(Scanner scanner) {
        List<CartItem> cartItems = new ArrayList<>();

        System.out.println("請輸入購買商品（格式：品號 數量），輸入 end 結束：");
        System.out.println("例如：WINE001 2");
        System.out.println();

        while (true) {
            System.out.print("請輸入品號與數量: ");
            String input = scanner.nextLine().trim();

            if ("end".equalsIgnoreCase(input)) break;
            if (input.isEmpty()) continue;
            
            // \\s：代表任何空白字元
            String[] parts = input.split("\\s+");
            if (parts.length == 2) {
                try {
                    String itemCode = parts[0];
                    int quantity = Integer.parseInt(parts[1]);

                    if (quantity <= 0) {
                        System.out.println("數量必須大於 0，請重新輸入。");
                        continue;
                    }

                    cartItems.add(new CartItem(itemCode, quantity));
                    System.out.println("已加入: " + itemCode + " x " + quantity);

                } catch (NumberFormatException e) {
                    System.out.println("數量格式錯誤，請重新輸入。");
                }
            } else {
                System.out.println("輸入格式錯誤，請依「品號 數量」格式輸入。");
            }
        }

        System.out.println("輸入完成，共 " + cartItems.size() + " 種商品。\n");
        return cartItems;
    }
    
    /**
     * 印出完整銷售收據
     */
    private static void printReceipt(Receipt receipt, String dateString, boolean isCompanyMember) {
        System.out.println("================================================================================");
        System.out.println("                              完整銷售收據");
        System.out.println("================================================================================");
        System.out.println("交易日期: " + dateString + " 下午");
        System.out.println("地點: 桃園機場免稅店");
        System.out.println("會員身份: " + (isCompanyMember ? " 公司會員 " : " 一般顧客 "));
        System.out.println("--------------------------------------------------------------------------------");

        printRow("品號", "品名", "分類", "數量", "原價", "會員價", "最終價");
        System.out.println("--------------------------------------------------------------------------------");

        for (Line line : receipt.getLines()) {
            printRow(
                    line.getItemCode(),
                    TextAlignUtil.shorten(line.getItemName(), 25),
                    line.getCategoryName(),
                    String.valueOf(line.getQuantity()),
                    String.format("%,.0f", line.getOriginalAmount().doubleValue()),
                    String.format("%,.0f", line.getOriginalAmount().subtract(line.getMemberDiscount()).doubleValue()),
                    String.format("%,.0f", line.getFinalAmount().doubleValue())
            );
        }

        System.out.println("--------------------------------------------------------------------------------");
        System.out.println(TextAlignUtil.padRightAlign("原價總計:", String.format("%,12.0f", receipt.getTotalOriginalAmount().doubleValue())));
        System.out.println(TextAlignUtil.padRightAlign("會員折扣後總計:", String.format("%,12.0f", receipt.getTotalMemberAmount().doubleValue())));
        System.out.println(TextAlignUtil.padRightAlign("促銷折扣:", String.format("%,12.0f", receipt.getTotalPromotionDiscount().doubleValue())));
        System.out.println(TextAlignUtil.padRightAlign("總折扣金額:", String.format("%,12.0f", receipt.getTotalDiscountAmount().doubleValue())));
        System.out.println("================================================================================");
        System.out.println(TextAlignUtil.padRightAlign("最終應付金額:", String.format("%,12.0f", receipt.getFinalAmount().doubleValue())));
        System.out.println("================================================================================");
        System.out.println("折扣明細:");
        for (Line line : receipt.getLines()) {
            if (line.getTotalDiscount().compareTo(BigDecimal.ZERO) > 0) {
                System.out.printf("  %-25s 會員折扣: %,.0f 元 | 促銷折扣: %,.0f 元 | 總折扣: %,.0f 元%n",
                		TextAlignUtil.shorten(line.getItemName(), 20),
                        line.getMemberDiscount().doubleValue(),
                        line.getPromotionDiscount().doubleValue(),
                        line.getTotalDiscount().doubleValue());
            }
        }
        System.out.println("================================================================================");
        System.out.println("感謝您的惠顧，祝您購物愉快！");
        System.out.println("================================================================================");
    }

    /**
     * 對齊輸出表格欄位
     */
    private static void printRow(String col1, String col2, String col3, String col4,
	            String col5, String col6, String col7) {
	System.out.printf("%s\t%s%s%s%s%s%s%n",
	TextAlignUtil.padRight(col1, 10),
	TextAlignUtil.padRight(col2, 30),
	TextAlignUtil.padRight(col3, 10),
	TextAlignUtil.padRight(col4, 8),
	TextAlignUtil.padRight(col5, 12),
	TextAlignUtil.padRight(col6, 12),
	TextAlignUtil.padRight(col7, 12));
	}

}

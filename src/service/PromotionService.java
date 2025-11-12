package service;

import model.*;
import repo.ActivityRepository;
import repo.ItemRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 負責整體促銷邏輯的主要服務類。
 * 包含：
 * 1. 計算會員折扣
 * 2. 套用促銷活動
 * 3. 彙整最終收據
 */
public class PromotionService {
	
    // 負責查詢商品資料
    private ItemRepository itemRepository;

    // 負責查詢促銷活動
    private ActivityRepository activityRepository;

    // 公司會員酒類95折
    private static final BigDecimal MEMBER_DISCOUNT_RATE = new BigDecimal("0.95");
    
    /**constructor 初始化資料存取物件 */
    public PromotionService() {
        this.itemRepository = new ItemRepository();
        this.activityRepository = new ActivityRepository();
    }
    
    /**
     * 主流程：計算整筆交易的收據金額
     */
    public Receipt calculateReceipt(List<CartItem> cartItems, Date transactionDate, boolean isCompanyMember) {
    	
    	// 從資料庫補齊商品資訊
        completeItemInfo(cartItems);
        
        // 將購物車轉換為明細 (Line)
        List<Line> lines = convertToLines(cartItems);
        
        // 建立收據物件
        Receipt receipt = new Receipt();
        receipt.setLines(lines);
        
        // 計算初始金額（未打折前）
        receipt.calculateTotals();
        
        // 若為公司會員則套用會員折扣
        applyMemberDiscount(receipt, isCompanyMember);
        
        // 查詢目前日期內有效的促銷活動
        List<Activity> validActivities = activityRepository.findValidActivities(transactionDate);
        
        // 套用滿額活動折扣
        applyPromotionActivities(receipt, validActivities);
        
        // 重新計算最終金額
        receipt.calculateTotals();
        
        return receipt;
    }
    
    /** 
     * 
     * 將購物車內容轉換為 Line 物件
     *  
     */
    private List<Line> convertToLines(List<CartItem> cartItems) {
        List<Line> lines = new ArrayList<>();
        
        for (CartItem cartItem : cartItems) {
            if (cartItem.getItem() != null) {
                Line line = new Line(
                    cartItem.getItemCode(),
                    cartItem.getItem().getItemName(),
                    cartItem.getItem().getCategory01(),
                    cartItem.getItem().getCategory01Name(),
                    cartItem.getQuantity(),
                    cartItem.getItem().getUnitPrice()
                );
                lines.add(line);
            }
        }
        
        return lines;
    }
    
    /** 從資料庫補齊商品資料（批次查詢） */
    private void completeItemInfo(List<CartItem> cartItems) {
    	
    	// 從購物車清單中取出所有商品的編號，收集成一個新的字串清單
        List<String> itemCodes = cartItems.stream()
                .map(CartItem::getItemCode)
                .collect(Collectors.toList());
        
        // 查詢商品詳細資料
        List<Item> items = itemRepository.findItemsByCodes(itemCodes);
        
        // 商品清單轉成 Map 方便快速查找
        Map<String, Item> itemMap = items.stream()
                .collect(Collectors.toMap(Item::getItemCode, item -> item));
        
        // 將商品資訊補回購物車物件
        for (CartItem cartItem : cartItems) {
            Item item = itemMap.get(cartItem.getItemCode());
            if (item != null) {
                cartItem.setItem(item);
            } else {
                System.err.println("警告: 未找到商品 " + cartItem.getItemCode());
            }
        }
    }
    
    /** 套用會員折扣（僅限酒類） */
    private void applyMemberDiscount(Receipt receipt, boolean isCompanyMember) {
        if (!isCompanyMember) return; // 非會員不折扣

        for (Line line : receipt.getLines()) {
            if ("09".equals(line.getCategoryCode())) { // 判斷分類代碼是否為 "09"（酒類）
                line.applyMemberDiscount(MEMBER_DISCOUNT_RATE);
                System.out.println("[會員折扣] " + line.getItemName() + " → 套用95折");
            }
        }
        receipt.calculateTotals();
    }

    /** 套用促銷活動邏輯 */
    private void applyPromotionActivities(Receipt receipt, List<Activity> activities) {
        for (Activity activity : activities) {
        	// 先計算可參與活動的金額總和
        	BigDecimal eligibleTotal = calculateEligibleTotal(findEligibleLines(receipt.getLines(), activity));
        	
        	// 若總金額達到門檻才套用折扣
        	if (eligibleTotal.compareTo(activity.getMeetCriteriaAmtG1()) >= 0) {
                List<Line> eligibleLines = findEligibleLines(receipt.getLines(), activity);
                
                if (!eligibleLines.isEmpty()) {
                    eligibleTotal = calculateEligibleTotal(eligibleLines);
                    
                    if (eligibleTotal.compareTo(BigDecimal.ZERO) > 0) {
                    	// 按比例分攤折扣金額
                        distributeDiscount(eligibleLines, activity.getAwardAmtG1(), eligibleTotal);
                        
                        // 將此活動名稱與折扣記錄到收據中
                        receipt.addActivityDiscount(activity.getActivityName(), activity.getAwardAmtG1());
                    }
                }
            }
        }
    }
    
    /** 找出符合活動條件的商品明細 */
    private List<Line> findEligibleLines(List<Line> lines, Activity activity) {
        return lines.stream()
                .filter(line -> isCategoryMatch(line.getCategoryCode(), activity.getItemDiscountGroup()))
                .collect(Collectors.toList());
    }
    
    /** 檢查活動分類與商品分類是否匹配 */
    private boolean isCategoryMatch(String itemCategoryCode, String activityCategoryGroup) {
        if (itemCategoryCode == null || activityCategoryGroup == null) return false;

        if (activityCategoryGroup.equalsIgnoreCase("ALL")) return true; // 全館活動

        String[] categories = activityCategoryGroup.split(",");
        for (String category : categories) {
            if (category.trim().equalsIgnoreCase(itemCategoryCode)) { // 代碼比對
                return true;
            }
        }
        return false;
    }
    
    /** 計算可參與促銷的商品總金額 */
    private BigDecimal calculateEligibleTotal(List<Line> eligibleLines) {
        return eligibleLines.stream()
                .map(Line::getFinalAmount)  // 使用最終金額（以應用折扣） .map(line -> line.getFinalAmount())
                // 從 0 開始，逐筆相加所有 finalAmount
                // reduce(初始值, 運算方式)
                // 這裡等同於：
                // BigDecimal total = 0;
                // for (Line line : eligibleLines) total = total.add(line.getFinalAmount());
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /** 將活動折扣按比例分攤到每一個符合商品 */
    private void distributeDiscount(List<Line> eligibleLines, BigDecimal totalDiscount, BigDecimal eligibleTotal) {
        BigDecimal distributed = BigDecimal.ZERO;
        
        for (int i = 0; i < eligibleLines.size(); i++) {
            Line line = eligibleLines.get(i);
            BigDecimal ratio = line.getFinalAmount().divide(eligibleTotal, 6, RoundingMode.HALF_UP);
            BigDecimal lineDiscount;
            
            if (i == eligibleLines.size() - 1) {
            	// 最後一筆用剩下的金額，避免四捨五入誤差
                lineDiscount = totalDiscount.subtract(distributed);
            } else {
                lineDiscount = totalDiscount.multiply(ratio).setScale(0, RoundingMode.HALF_UP);
                distributed = distributed.add(lineDiscount);
            }
            
            // 套用促銷折扣
            line.applyPromotionDiscount(lineDiscount);
        }
    }
}
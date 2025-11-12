package model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收據類別。
 * 
 * 用於儲存一筆交易的所有明細資料、各項總金額與折扣。
 * 主要由 {@code PromotionService} 負責填入資料。
 * 
 */
public class Receipt {
	
    /** 銷售明細列表（每個 Line 代表一項商品） */
    private List<Line> lines;  
    
    /** 原價總計（所有商品的原價加總） */
    private BigDecimal totalOriginalAmount;
    
    /** 會員折扣後總計（原價扣除會員折扣後的總額） */
    private BigDecimal totalMemberAmount;
    
    /** 促銷折扣總計 */
    private BigDecimal totalPromotionDiscount;
    
    /** 總折扣金額（會員折扣 + 促銷折扣） */
    private BigDecimal totalDiscountAmount;
    
    /** 最終應付金額（原價 - 所有折扣） */
    private BigDecimal finalAmount;
    
    /** 活動折扣清單（記錄每個活動名稱與折扣金額） */
    private Map<String, BigDecimal> activityDiscounts;
    
    /** no-args constructor */
    public Receipt() {
        this.lines = new ArrayList<>();
        this.totalOriginalAmount = BigDecimal.ZERO;
        this.totalMemberAmount = BigDecimal.ZERO;
        this.totalPromotionDiscount = BigDecimal.ZERO;
        this.totalDiscountAmount = BigDecimal.ZERO;
        this.finalAmount = BigDecimal.ZERO;
        this.activityDiscounts = new HashMap<>();
    }
    
    // Getters and Setters
    public List<Line> getLines() { return lines; }
    public void setLines(List<Line> lines) { this.lines = lines; }
    
    public BigDecimal getTotalOriginalAmount() { return totalOriginalAmount; }
    public void setTotalOriginalAmount(BigDecimal totalOriginalAmount) { this.totalOriginalAmount = totalOriginalAmount; }
    
    public BigDecimal getTotalMemberAmount() { return totalMemberAmount; }
    public void setTotalMemberAmount(BigDecimal totalMemberAmount) { this.totalMemberAmount = totalMemberAmount; }
    
    public BigDecimal getTotalPromotionDiscount() { return totalPromotionDiscount; }
    public void setTotalPromotionDiscount(BigDecimal totalPromotionDiscount) { this.totalPromotionDiscount = totalPromotionDiscount; }
    
    public BigDecimal getTotalDiscountAmount() { return totalDiscountAmount; }
    public void setTotalDiscountAmount(BigDecimal totalDiscountAmount) { this.totalDiscountAmount = totalDiscountAmount; }
    
    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
    
    public Map<String, BigDecimal> getActivityDiscounts() { return activityDiscounts; }
    public void setActivityDiscounts(Map<String, BigDecimal> activityDiscounts) { this.activityDiscounts = activityDiscounts; }
    

    /**
     * 計算所有金額總和。
     * 使用 Stream API 依序計算：
     * 
	 * stream()：將 List 轉成資料流以便操作
	 * map()：取出每個 Line 的指定欄位（例如金額）
	 * reduce()：將所有值加總成一個結果
	 *   
     * 1. 原價總計
     * 2. 會員價總計（扣會員折扣）
     * 3. 促銷折扣總計
     * 4. 總折扣金額（會員＋促銷）
     * 5. 最終應付金額
     */
    public void calculateTotals() {
    	// 原價總計：取出每筆 Line 的原價並加總
        this.totalOriginalAmount = lines.stream()          
                .map(Line::getOriginalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 會員折扣後總計：原價 - 會員折扣，再加總
        this.totalMemberAmount = lines.stream()				
                .map(line -> line.getOriginalAmount().subtract(line.getMemberDiscount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 促銷折扣總計：取出每筆促銷折扣金額並加總
        this.totalPromotionDiscount = lines.stream()
                .map(Line::getPromotionDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 總折扣金額：取出每筆會員+促銷折扣的合計，加總
        this.totalDiscountAmount = lines.stream()
                .map(Line::getTotalDiscount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 最終應付金額：取出每筆最終金額並加總
        this.finalAmount = lines.stream()
                .map(Line::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * 新增一筆促銷活動折扣記錄。
     *
     * @param activityName 活動名稱
     * @param discount 活動折扣金額
     */
    public void addActivityDiscount(String activityName, BigDecimal discount) {
        this.activityDiscounts.put(activityName, discount);
    }
    
    /**
     * 新增一筆明細（Line）並重新計算總金額。
     *
     * @param line 新增的商品明細
     */
    public void addLine(Line line) {
        this.lines.add(line);
        calculateTotals();   //每當新增一筆明細後，就立即重新計算整張收據的金額總和
    }
    
    /**
     * 依商品代碼查找對應的銷售明細。
    *
	*   stream()：將明細清單轉為資料流
	*   filter()：篩選出 itemCode 相同的項目
	*   findFirst()：取第一筆符合的結果
	*   orElse(null)：若找不到則回傳 null
    *
    * @param itemCode 商品代碼
    * @return 對應的 Line 物件，若找不到則回傳 null
    */ 
    public Line findLineByItemCode(String itemCode) {
        return lines.stream()
                .filter(line -> itemCode.equals(line.getItemCode()))
                .findFirst()
                .orElse(null);
    }
}
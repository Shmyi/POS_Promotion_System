package model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 銷售明細類別，用於表示收據上的單一商品。
 * 
 * 儲存商品代碼、名稱、分類、數量、單價與金額相關資訊，
 * 並可套用會員折扣及促銷折扣後計算最終金額。
 * 
 */
public class Line {
	
    // 商品基本資料
    private String itemCode;          // 商品代碼
    private String itemName;          // 商品名稱
    private String categoryCode;      // 商品大類 (category01)
    private String categoryName;      // 類別名稱
    private int quantity;             // 購買數量
    private BigDecimal unitPrice;     // 單價

    // 金額欄位
    private BigDecimal originalAmount;        // 原價小計
    private BigDecimal manualDiscount;        // 手動折扣
    private BigDecimal memberDiscount;        // 會員折扣
    private BigDecimal promotionDiscount;     // 促銷折扣
    private BigDecimal totalDiscount;         // 總折扣（手動+會員+促銷）
    private BigDecimal finalAmount;           // 最終金額（原價 - 總折扣）
    
    /** no-args constructor */
    public Line() {
        this.quantity = 0;
        this.unitPrice = BigDecimal.ZERO;
        this.originalAmount = BigDecimal.ZERO;
        this.manualDiscount = BigDecimal.ZERO;
        this.memberDiscount = BigDecimal.ZERO;
        this.promotionDiscount = BigDecimal.ZERO;
        this.finalAmount = BigDecimal.ZERO;
    }
    
    /**
     * Constructor：建立一筆完整銷售明細。
     */
    public Line(String itemCode, String itemName, String categoryCode, String categoryName,
            int quantity, BigDecimal unitPrice) {
    this.itemCode = itemCode;
    this.itemName = itemName;
    this.categoryCode = categoryCode;
    this.categoryName = categoryName;
    this.quantity = quantity;
    this.unitPrice = unitPrice;

    this.originalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
    this.manualDiscount = BigDecimal.ZERO;
    this.memberDiscount = BigDecimal.ZERO;
    this.promotionDiscount = BigDecimal.ZERO;
    this.totalDiscount = BigDecimal.ZERO;
    this.finalAmount = this.originalAmount;
}
    
    /**
     * 1 單項手動折扣
     */
    public void applyManualDiscount(BigDecimal discount) {
        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) return;
        this.manualDiscount = discount.setScale(0, RoundingMode.HALF_UP);
        recalcTotals();
    }
    
    /**
     * 2 會員折扣（例如 95 折）
     */
    public void applyMemberDiscount(BigDecimal discountRate) {
        if (discountRate == null || discountRate.compareTo(BigDecimal.ZERO) <= 0) return;

        BigDecimal base = this.originalAmount.subtract(this.manualDiscount); //手動折扣後金額
        BigDecimal discount = base.multiply(BigDecimal.ONE.subtract(discountRate))
                .setScale(0, RoundingMode.HALF_UP);

        this.memberDiscount = discount;
        recalcTotals();
    }

    /**
     * 3 促銷折扣（滿額活動）
     */
    public void applyPromotionDiscount(BigDecimal discount) {
        if (discount == null || discount.compareTo(BigDecimal.ZERO) <= 0) return;
        this.promotionDiscount = this.promotionDiscount.add(discount.setScale(0, RoundingMode.HALF_UP));
        recalcTotals();
    }
    
    /**
     * 重新計算總折扣與最終金額。
     */
    private void recalcTotals() {
        // 重新計算一次總折扣與最終金額
        this.totalDiscount = manualDiscount.add(memberDiscount).add(promotionDiscount);
        this.finalAmount = this.originalAmount.subtract(this.totalDiscount);

        // 確保不出現負金額
        if (this.finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            this.finalAmount = BigDecimal.ZERO;
        }
    }
        
//    /** 計算原價小計（單價 × 數量） */
//    private void calculateOriginalAmount() {
//        if (unitPrice != null && quantity > 0) {
//            this.originalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
//            this.finalAmount = this.originalAmount; 
//        }
//    }
    
    // Getters and Setters
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getOriginalAmount() { return originalAmount; }
    public void setOriginalAmount(BigDecimal originalAmount) { this.originalAmount = originalAmount; }

    public BigDecimal getManualDiscount() { return manualDiscount; }
    public void setManualDiscount(BigDecimal manualDiscount) { this.manualDiscount = manualDiscount; }

    public BigDecimal getMemberDiscount() { return memberDiscount; }
    public void setMemberDiscount(BigDecimal memberDiscount) { this.memberDiscount = memberDiscount; }

    public BigDecimal getPromotionDiscount() { return promotionDiscount; }
    public void setPromotionDiscount(BigDecimal promotionDiscount) { this.promotionDiscount = promotionDiscount; }

    public BigDecimal getTotalDiscount() { return totalDiscount; }
    public void setTotalDiscount(BigDecimal totalDiscount) { this.totalDiscount = totalDiscount; }

    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
    
//    /**
//     * 
//     * 套用會員折扣，依據指定的折扣比率計算折扣金額，並更新最終應付金額。
//     * 公式：折扣金額 = 原價 × (1 - 折扣率)
//     *
//     * @param discountRate 折扣比率（例如 0.95 代表 95 折）
//     */
//    public void applyMemberDiscount(BigDecimal discountRate) {
//        if (originalAmount.compareTo(BigDecimal.ZERO) > 0) {
//            this.memberDiscount = originalAmount.multiply(BigDecimal.ONE.subtract(discountRate))
//                    .setScale(0, java.math.RoundingMode.HALF_UP);   //結果取到整數位（小數點 0 位） 四捨五入 (HALF_UP)
//            updateFinalAmount();
//        }
//    }
//    
//    /**
//     * 套用促銷折扣。
//     * @param discount 折扣金額
//     */
//    public void applyPromotionDiscount(BigDecimal discount) {
//        this.promotionDiscount = this.promotionDiscount.add(discount);
//        updateFinalAmount();
//    }
    
    /**
     * toSring 回傳字串
     */
    @Override
    public String toString() {
        return String.format(
                "Line[item=%s, 原價=%,.0f, 手動折扣=%,.0f, 會員折扣=%,.0f, 促銷折扣=%,.0f, 總折扣=%,.0f, 最終金額=%,.0f]",
                itemName,
                originalAmount,
                manualDiscount,
                memberDiscount,
                promotionDiscount,
                totalDiscount,
                finalAmount
        );
    }
}
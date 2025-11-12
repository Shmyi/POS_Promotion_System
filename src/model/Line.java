package model;

import java.math.BigDecimal;

/**
 * 銷售明細類別，用於表示收據上的單一商品。
 * 
 * 儲存商品代碼、名稱、分類、數量、單價與金額相關資訊，
 * 並可套用會員折扣及促銷折扣後計算最終金額。
 * 
 */
public class Line {
	
    /** 商品代碼 */
    private String itemCode;

    /** 商品名稱 */
    private String itemName;

    /** 商品分類名稱（例如 酒類、化妝品） */
    private String category;

    /** 購買數量 */
    private int quantity;

    /** 單價 */
    private BigDecimal unitPrice;

    /** 原價小計（單價 × 數量） */
    private BigDecimal originalAmount;

    /** 會員折扣金額 */
    private BigDecimal memberDiscount;

    /** 促銷折扣金額 */
    private BigDecimal promotionDiscount;

    /** 最終應付金額（原價 - 折扣） */
    //BigDecimal 是物件（類別），不是基本型別（primitive type）
    private BigDecimal finalAmount;

    /** 商品分類代碼（例如 01、02、09） */
    private String categoryCode;
    
    /** no-args constructor */
    public Line() {
        this.quantity = 0;
        this.unitPrice = BigDecimal.ZERO;
        this.originalAmount = BigDecimal.ZERO;
        this.memberDiscount = BigDecimal.ZERO;
        this.promotionDiscount = BigDecimal.ZERO;
        this.finalAmount = BigDecimal.ZERO;
    }
    
    /**
     * Constructor：建立一筆完整銷售明細。
     * @param itemCode 商品代碼
     * @param itemName 商品名稱
     * @param categoryCode 商品分類代碼
     * @param category 商品分類名稱
     * @param quantity 數量
     * @param unitPrice 單價
     */
    public Line(String itemCode, String itemName, String categoryCode, String category,  int quantity, BigDecimal unitPrice) {
        this();
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.categoryCode = categoryCode;
        this.category = category;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        calculateOriginalAmount();
    }
    
    
    /** 計算原價小計（單價 × 數量） */
    private void calculateOriginalAmount() {
        if (unitPrice != null && quantity > 0) {
            this.originalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
            this.finalAmount = this.originalAmount; 
        }
    }
    
    // Getters and Setters
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity; 
        calculateOriginalAmount();
    }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { 
        this.unitPrice = unitPrice; 
        calculateOriginalAmount();
    }
    
    public BigDecimal getOriginalAmount() { return originalAmount; }
    public void setOriginalAmount(BigDecimal originalAmount) { this.originalAmount = originalAmount; }
    
    public BigDecimal getMemberDiscount() { return memberDiscount; }
    public void setMemberDiscount(BigDecimal memberDiscount) { 
        this.memberDiscount = memberDiscount; 
        updateFinalAmount();
    }
    
    public BigDecimal getPromotionDiscount() { return promotionDiscount; }
    //更新促銷折扣時重新計算最終應付金額
    public void setPromotionDiscount(BigDecimal promotionDiscount) { 
        this.promotionDiscount = promotionDiscount; 
        updateFinalAmount();
    }
    
    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
    
    /** 取得總折扣金額（會員折扣 + 促銷折扣） */
    public BigDecimal getTotalDiscount() {
        return memberDiscount.add(promotionDiscount);
    }
    
    /** 重新計算最終金額（原價 - 折扣） */
    private void updateFinalAmount() {
        this.finalAmount = originalAmount.subtract(memberDiscount).subtract(promotionDiscount);  
    }
    
    /**
     * 
     * 套用會員折扣，依據指定的折扣比率計算折扣金額，並更新最終應付金額。
     * 公式：折扣金額 = 原價 × (1 - 折扣率)
     *
     * @param discountRate 折扣比率（例如 0.95 代表 95 折）
     */
    public void applyMemberDiscount(BigDecimal discountRate) {
        if (originalAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.memberDiscount = originalAmount.multiply(BigDecimal.ONE.subtract(discountRate))
                    .setScale(0, java.math.RoundingMode.HALF_UP);   //結果取到整數位（小數點 0 位） 四捨五入 (HALF_UP)
            updateFinalAmount();
        }
    }
    
    /**
     * 套用促銷折扣。
     * @param discount 折扣金額
     */
    public void applyPromotionDiscount(BigDecimal discount) {
        this.promotionDiscount = this.promotionDiscount.add(discount);
        updateFinalAmount();
    }
    
    /**
     * toSring 回傳字串
     */
    @Override
    public String toString() {
        return "Line{" +
                "itemCode='" + itemCode + '\'' +
                ", itemName='" + itemName + '\'' +
                ", category='" + category + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", originalAmount=" + originalAmount +
                ", memberDiscount=" + memberDiscount +
                ", promotionDiscount=" + promotionDiscount +
                ", finalAmount=" + finalAmount +
                '}';
    }
}
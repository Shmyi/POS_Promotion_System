package model;

import java.math.BigDecimal;

/**
 * 表示購物車中的單一商品項目。
 * 
 * 此類別負責儲存商品資訊、購買數量以及所有金額計算結果，包括：
 *   原價小計（單價 × 數量）
 *   會員折扣後價格
 *   促銷後最終金額
 *   會員與促銷的折扣明細
 *
 * 此類別主要由 {@code PromotionService} 用於套用各種折扣，
 * 並由 {@code Receipt} 用於統計銷售總額與折扣。</p>
 *
 */
public class CartItem {
	
	/** 商品代碼，用於識別及查詢商品資料 */
    private String itemCode;
    
    /** 商品物件，包含商品名稱、分類、單價等資訊 */
    private Item item;
    
    /** 購買數量 */
    private int quantity;
    
    /** 原價小計（單價 × 數量） */
    private BigDecimal originalPrice;     
    
    /** 會員折扣後的小計 */
    private BigDecimal memberPrice;       
    
    /** 最終應付金額（含促銷折扣） */
    private BigDecimal finalPrice;     
    
    /** 總折扣金額（會員折扣 + 促銷折扣） */
    private BigDecimal discountAmount;     
    
    /** 會員折扣金額 */
    private BigDecimal memberDiscount;   
    
    /** 促銷折扣金額 */
    private BigDecimal promotionDiscount;  
    
    private BigDecimal manualDiscount = BigDecimal.ZERO; // 手動輸入折扣金額
    
    /** No-args Constructor */
    public CartItem() {}
    
    /**
     * 建構購物項目，指定品號與數量。
     * 所有金額欄位初始為 0。
     *
     * @param itemCode 商品代號
     * @param quantity 購買數量
     */
    public CartItem(String itemCode, int quantity) {
        this.itemCode = itemCode;
        this.quantity = quantity;
        this.originalPrice = BigDecimal.ZERO;
        this.memberPrice = BigDecimal.ZERO;
        this.finalPrice = BigDecimal.ZERO;
        this.discountAmount = BigDecimal.ZERO;
        this.memberDiscount = BigDecimal.ZERO;
        this.promotionDiscount = BigDecimal.ZERO;
    }
    
    // Getters and Setters
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    
    public Item getItem() { return item; }
    
    /**
     * 設定商品物件，同時根據單價與數量自動更新原價小計。
     */
    public void setItem(Item item) { 
        this.item = item;
        if (item != null && item.getUnitPrice() != null) {
            this.originalPrice = item.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }
    
    // Getters and Setters
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity;
        if (item != null && item.getUnitPrice() != null) {
            this.originalPrice = item.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }
    
    public BigDecimal getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(BigDecimal originalPrice) { this.originalPrice = originalPrice; }
    
    public BigDecimal getMemberPrice() { return memberPrice; }
    public void setMemberPrice(BigDecimal memberPrice) { this.memberPrice = memberPrice; }
    
    public BigDecimal getFinalPrice() { return finalPrice; }
    public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
    
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    
    public BigDecimal getMemberDiscount() { return memberDiscount; }
    public void setMemberDiscount(BigDecimal memberDiscount) { this.memberDiscount = memberDiscount; }
    
    public BigDecimal getPromotionDiscount() { return promotionDiscount; }
    public void setPromotionDiscount(BigDecimal promotionDiscount) { this.promotionDiscount = promotionDiscount; }
    
    public BigDecimal getManualDiscount() { return manualDiscount; }
    public void setManualDiscount(BigDecimal manualDiscount) { this.manualDiscount = manualDiscount; }

    
    // Business Logic ---------------------------------
    /**
     * 套用會員折扣（ 95 折），更新會員價與折扣金額。
     *
     * @param discountRate 折扣率 0.95
     */
    public void applyMemberDiscount(BigDecimal discountRate) {
        if (originalPrice.compareTo(BigDecimal.ZERO) > 0) {
            this.memberDiscount = originalPrice.multiply(BigDecimal.ONE.subtract(discountRate))
                    .setScale(0, java.math.RoundingMode.HALF_UP);
            this.memberPrice = originalPrice.subtract(memberDiscount);
            this.discountAmount = memberDiscount;
            this.finalPrice = memberPrice;
        }
    }
    
    public void applyPromotionDiscount(BigDecimal discount) {
        this.promotionDiscount = this.promotionDiscount.add(discount);
        this.discountAmount = this.discountAmount.add(discount);
        this.finalPrice = this.memberPrice.subtract(discount);
    }
}
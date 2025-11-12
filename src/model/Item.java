package model;

import java.math.BigDecimal;

/**
 * 商品資料類別，用來儲存商品的基本資訊。
 * 包含商品代碼、名稱、分類及單價等欄位。
 * 使用 BigDecimal 來避免金額運算誤差。
 */
public class Item {
	
    /** 商品代碼 */
    private String itemCode;

    /** 商品名稱 */
    private String itemName;

    /** 商品主分類代碼 */
    private String category01;

    /** 商品主分類名稱 */
    private String category01Name;

    /** 商品單價 */
    private BigDecimal unitPrice;
    
    /** No-args Constructors */
    public Item() {}
    
    /**
     * Constructor：建立商品物件。
     * @param itemCode 商品代碼
     * @param itemName 商品名稱
     * @param category01 商品主分類代碼
     * @param category01Name 商品主分類名稱
     * @param unitPrice 商品單價
     */
    public Item(String itemCode, String itemName, String category01, String category01Name, BigDecimal unitPrice) {
        this.itemCode = itemCode;
        this.itemName = itemName;
        this.category01 = category01;
        this.category01Name = category01Name;
        this.unitPrice = unitPrice;
    }
    
    // Getters and Setters
    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }
    
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    
    public String getCategory01() { return category01; }
    public void setCategory01(String category01) { this.category01 = category01; }
    
    public String getCategory01Name() { return category01Name; }
    public void setCategory01Name(String category01Name) { this.category01Name = category01Name; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    /**
     * toString 回傳商品字串
     */
    @Override
    public String toString() {
        return "Item{" +
                "itemCode='" + itemCode + '\'' +
                ", itemName='" + itemName + '\'' +
                ", category01='" + category01 + '\'' +
                ", category01Name='" + category01Name + '\'' +
                ", unitPrice=" + unitPrice +
                '}';
    }
}
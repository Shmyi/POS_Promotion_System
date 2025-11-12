package model;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 促銷活動資料模型 (Activity)
 * ------------------------------------------------------------
 * 此類別對應資料庫表：crm_promo_rebate_h
 * 用於儲存與管理促銷活動的主檔資訊，包括：
 *  - 活動代碼與名稱
 *  - 活動起迄日期
 *  - 適用商品類別代碼
 *  - 滿額條件與折扣金額
 * ------------------------------------------------------------
 * 在系統中主要用途：
 *  1. 由 ActivityRepository 透過 JDBC 讀取資料庫紀錄後建立物件。
 *  2. 由 PromotionService 讀取活動資訊以判斷是否符合促銷條件。
 * ------------------------------------------------------------
 * 作者：yi chen
 * 建立日期：2025-11-10
 */
public class Activity {
    /** 活動代碼 */
    private String activityCode;

    /** 活動名稱*/
    private String activityName;

    /** 活動開始日期 */
    private Date startActivityDate;

    /** 活動結束日期 */
    private Date endActivityDate;

    /** 適用商品類別代碼 */
    private String itemDiscountGroup;

    /** 滿額條件金額 */
    private BigDecimal meetCriteriaAmtG1;

    /** 折扣或贈送金額 */
    private BigDecimal awardAmtG1;
    
    // No-arg Constructor
    public Activity() {}
    
    /**
     * Constructor
     * @param activityCode 活動代碼
     * @param activityName 活動名稱
     * @param startActivityDate 開始日期
     * @param endActivityDate 結束日期
     * @param itemDiscountGroup 適用商品類別代碼
     * @param meetCriteriaAmtG1 滿額條件金額
     * @param awardAmtG1 折扣金額
     */
    public Activity(String activityCode, String activityName, Date startActivityDate, 
                   Date endActivityDate, String itemDiscountGroup, 
                   BigDecimal meetCriteriaAmtG1, BigDecimal awardAmtG1) {
        this.activityCode = activityCode;
        this.activityName = activityName;
        this.startActivityDate = startActivityDate;
        this.endActivityDate = endActivityDate;
        this.itemDiscountGroup = itemDiscountGroup;
        this.meetCriteriaAmtG1 = meetCriteriaAmtG1;
        this.awardAmtG1 = awardAmtG1;
    }
    
    // Getters and Setters
    public String getActivityCode() { return activityCode; }
    public void setActivityCode(String activityCode) { this.activityCode = activityCode; }
    
    public String getActivityName() { return activityName; }
    public void setActivityName(String activityName) { this.activityName = activityName; }
    
    public Date getStartActivityDate() { return startActivityDate; }
    public void setStartActivityDate(Date startActivityDate) { this.startActivityDate = startActivityDate; }
    
    public Date getEndActivityDate() { return endActivityDate; }
    public void setEndActivityDate(Date endActivityDate) { this.endActivityDate = endActivityDate; }
    
    public String getItemDiscountGroup() { return itemDiscountGroup; }
    public void setItemDiscountGroup(String itemDiscountGroup) { this.itemDiscountGroup = itemDiscountGroup; }
    
    public BigDecimal getMeetCriteriaAmtG1() { return meetCriteriaAmtG1; }
    public void setMeetCriteriaAmtG1(BigDecimal meetCriteriaAmtG1) { this.meetCriteriaAmtG1 = meetCriteriaAmtG1; }
    
    public BigDecimal getAwardAmtG1() { return awardAmtG1; }
    public void setAwardAmtG1(BigDecimal awardAmtG1) { this.awardAmtG1 = awardAmtG1; }
    
    //toString 輸出字串
    @Override
    public String toString() {
        return "Activity{" +
                "activityCode='" + activityCode + '\'' +
                ", activityName='" + activityName + '\'' +
                ", startActivityDate=" + startActivityDate +
                ", endActivityDate=" + endActivityDate +
                ", itemDiscountGroup='" + itemDiscountGroup + '\'' +
                ", meetCriteriaAmtG1=" + meetCriteriaAmtG1 +
                ", awardAmtG1=" + awardAmtG1 +
                '}';
    }
}
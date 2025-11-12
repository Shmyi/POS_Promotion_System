package repo;

import config.DatabaseConfig;
import model.Activity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ActivityRepository 負責與資料庫互動，
 * 查詢目前有效的促銷活動（crm_promo_rebate_h 表）。
 * 
 * 使用 JDBC 連線資料庫，將查詢結果封裝成 {@link Activity} 物件。
 * 
 */
public class ActivityRepository {
    
    /**
     * 根據指定日期查詢「當天有效」的促銷活動。
     * <p>
     * SQL 條件：<br>
     *   <code>? BETWEEN start_activity_date AND end_activity_date</code><br>
     * 代表傳入的日期必須介於活動起訖日期之間。
     * </p>
     *
     * @param targetDate 查詢的目標日期（通常為交易日期）
     * @return 活動清單（List<Activity>），若無則回傳空集合
     */
    public List<Activity> findValidActivities(java.util.Date targetDate) {
    	
    	// 建立活動清單
        List<Activity> activities = new ArrayList<>();
        
        // SQL 查詢語句：找出指定日期內有效的活動
        String sql = "SELECT activity_code, activity_name, start_activity_date, " +
                    "end_activity_date, item_discount_group, meet_criteria_amt_g1, award_amt_g1 " +
                    "FROM crm_promo_rebate_h " +
                    "WHERE ? BETWEEN DATE(start_activity_date) AND DATE(end_activity_date)";
        
        // try-with-resources：自動關閉連線與 Statement
        try (Connection conn = DriverManager.getConnection(DatabaseConfig.URL, DatabaseConfig.USER, DatabaseConfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
        	// 將 java.util.Date 轉換成 java.sql.Date，並設定為 SQL 查詢中的第 1 個參數（?）
        	// getTime() 取得毫秒時間戳；new Date(...) 建立 SQL 專用日期物件
            pstmt.setDate(1, new Date(targetDate.getTime()));   
            // 執行查詢，取得結果
            ResultSet rs = pstmt.executeQuery();				
            
            // 將每一筆資料轉成 Activity 物件
            while (rs.next()) {
                Activity activity = new Activity(
                    rs.getString("activity_code"),
                    rs.getString("activity_name"),
                    rs.getDate("start_activity_date"),
                    rs.getDate("end_activity_date"),
                    rs.getString("item_discount_group"),
                    rs.getBigDecimal("meet_criteria_amt_g1"),
                    rs.getBigDecimal("award_amt_g1")
                );
                // 加入活動清單
                activities.add(activity);						// <== 將資料封裝成物件
            }
        } catch (SQLException e) {
            System.err.println("查询活动信息失败: " + e.getMessage());
            e.printStackTrace();
        }
        return activities;
    }
}
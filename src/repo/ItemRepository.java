// repository/ItemRepository.java
package repo;

import config.DatabaseConfig;
import model.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ItemRepository 負責與資料庫互動，提供商品資料查詢功能。
 * 包含：
 *  - findItemByCode()：查詢單一商品
 *  - findItemsByCodes()：批次查詢多筆商品
 *
 * 資料來源表：
 *  - im_item：商品主檔
 *  - im_item_category：商品分類表
 *  - im_item_price：商品價格表
 */
public class ItemRepository {
    
    /**
     * 查詢單一商品的詳細資料（包含分類名稱與最新價格）。
     * 使用 LEFT JOIN 連結商品分類與價格資料，
     * 並依價格表的 begin_date 遞減排序，僅取最新一筆價格。
     *
     * @param itemCode 商品代碼
     * @return 對應的 Item 物件；若查無資料則回傳 null
     */
    public Item findItemByCode(String itemCode) {
    	
    	// SQL 查詢：取得商品資訊、分類名稱與最新價格
        String sql = "SELECT i.item_code, i.item_c_name, i.category01, " +
                    "c.category_name as category01_name, ip.unit_price " +
                    "FROM im_item i " +
                    "LEFT JOIN im_item_category c ON i.category01 = c.category_code AND c.category_type = 'category01' " +
                    "LEFT JOIN im_item_price ip ON i.item_code = ip.item_code " +
                    "WHERE i.item_code = ? " +
                    "ORDER BY ip.begin_date DESC LIMIT 1";
        
        
        try (
        	// 建立資料庫連線
        	Connection conn = DriverManager.getConnection(DatabaseConfig.URL, DatabaseConfig.USER, DatabaseConfig.PASSWORD);
        	// 建立預處理查詢物件	
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            
        	// 設定查詢條件（商品代碼）
            pstmt.setString(1, itemCode);
            
            // 執行查詢
            ResultSet rs = pstmt.executeQuery();
            
            // 若查到資料，封裝成 Item 物件回傳
            if (rs.next()) {
                return new Item(
                    rs.getString("item_code"),
                    rs.getString("item_c_name"),
                    rs.getString("category01"),
                    rs.getString("category01_name"),
                    rs.getBigDecimal("unit_price")
                );
            }
        } catch (SQLException e) {
            System.err.println("查询商品信息失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 批次查詢多筆商品資料（依商品代碼清單）。
     *
     * 動態組合 SQL 的 IN 條件，並使用 FIELD() 函數保持輸入順序。
     *
     * @param itemCodes 商品代碼清單
     * @return 查詢結果的 Item 物件列表
     */
    public List<Item> findItemsByCodes(List<String> itemCodes) {
        List<Item> items = new ArrayList<>();
        if (itemCodes.isEmpty()) return items;
        
        //SQL 查詢語句
        StringBuilder sql = new StringBuilder(
            "SELECT i.item_code, i.item_c_name, i.category01, " +
            "c.category_name as category01_name, ip.unit_price " +
            "FROM im_item i " +
            "LEFT JOIN im_item_category c ON i.category01 = c.category_code AND c.category_type = 'category01' " +
            "LEFT JOIN im_item_price ip ON i.item_code = ip.item_code " +
            "WHERE i.item_code IN ("
        );
        /** 
         * WHERE i.item_code IN (?, ?, ?) 
         * ORDER BY FIELD(i.item_code, ?, ?, ?) 
         * */
	    // 根據 itemCodes 清單的長度，動態加入對應數量的「?」佔位符
	    // 例如傳入 3 個商品代碼 → 生成 "(?, ?, ?)"
        for (int i = 0; i < itemCodes.size(); i++) {
            sql.append("?");
            if (i < itemCodes.size() - 1) sql.append(",");
        }
        sql.append(") ORDER BY FIELD(i.item_code, ");  //補排序
        
        // FIELD() 確保查詢結果順序與輸入順序一致
        for (int i = 0; i < itemCodes.size(); i++) {
            sql.append("?");
            if (i < itemCodes.size() - 1) sql.append(",");
        }
        sql.append(")");
        
        
        try (Connection conn = DriverManager.getConnection(DatabaseConfig.URL, DatabaseConfig.USER, DatabaseConfig.PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
        	// 設定 IN 條件參數
            for (int i = 0; i < itemCodes.size(); i++) {
                pstmt.setString(i + 1, itemCodes.get(i));
            }
            // 設定 FIELD() 函數參數以保持順序
            for (int i = 0; i < itemCodes.size(); i++) {
                pstmt.setString(i + 1 + itemCodes.size(), itemCodes.get(i));
            }
            // 執行查詢
            ResultSet rs = pstmt.executeQuery();
            // 將每筆資料封裝成 Item 物件並加入清單
            while (rs.next()) {
                items.add(new Item(
                    rs.getString("item_code"),
                    rs.getString("item_c_name"),
                    rs.getString("category01"),
                    rs.getString("category01_name"),
                    rs.getBigDecimal("unit_price")
                ));
            }
        } catch (SQLException e) {
            System.err.println("批量查询商品信息失败: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }
}
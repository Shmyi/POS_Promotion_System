// test/DatabaseConnectionTest.java
package test;

import config.DatabaseConfig;
import java.sql.*;

public class DatabaseConnectionTest {
    public static void main(String[] args) {
        System.out.println("=== 数据库连接测试 ===");
        
        try (Connection conn = DriverManager.getConnection(
                DatabaseConfig.URL, DatabaseConfig.USER, DatabaseConfig.PASSWORD)) {
            
            System.out.println("✅ 数据库连接成功");
            
            // 测试基本查询
            testBasicQueries(conn);
            
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败: " + e.getMessage());
        }
    }
    
    private static void testBasicQueries(Connection conn) {
        String[] testQueries = {
            "SELECT COUNT(*) as count FROM im_item",
            "SELECT COUNT(*) as count FROM im_item_price", 
            "SELECT COUNT(*) as count FROM im_item_category",
            "SELECT COUNT(*) as count FROM crm_promo_rebate_h"
        };
        
        String[] tableNames = {"im_item", "im_item_price", "im_item_category", "crm_promo_rebate_h"};
        
        for (int i = 0; i < testQueries.length; i++) {
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(testQueries[i])) {
                
                if (rs.next()) {
                    System.out.printf("✅ %-20s: %d 条记录%n", tableNames[i], rs.getInt(1));
                }
            } catch (SQLException e) {
                System.out.printf("❌ %-20s: 查询失败 - %s%n", tableNames[i], e.getMessage());
            }
        }
    }
}
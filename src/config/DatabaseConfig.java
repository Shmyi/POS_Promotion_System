package config;

/**
 * 資料庫連線設定類別 (DatabaseConfig)
 * ------------------------------------------------------------
 * 此類別用來集中管理 MySQL 資料庫連線設定，
 * 包含：
 *  1. 連線網址 (URL)
 *  2. 使用者帳號與密碼
 *  3. JDBC Driver 載入機制
 *
 * 所有需要連線資料庫的類別（例如 Repository 類別）
 * 都應該透過 DatabaseConfig 取得連線資訊，
 * 避免重複定義與維護。
 * ------------------------------------------------------------
 * 作者：yi chen
 * 建立日期：2025-11-10
 */
public class DatabaseConfig {
    
    /**
     * 資料庫連線字串 (URL)
     * 
     * 結構說明：
     * jdbc:mysql://主機位置:連接埠/資料庫名稱?參數設定
     *
     * - useSSL=false：關閉 SSL 驗證，避免本地端出現警告。
     * - serverTimezone=UTC：指定伺服器時區，避免時區錯誤。
     * - allowPublicKeyRetrieval=true：允許公開金鑰抓取（必要於新版 MySQL 驗證）。
     */
    public static final String URL = "jdbc:mysql://localhost:3306/twg4700002" +
            "?useSSL=false" +
            "&serverTimezone=UTC" +
            "&allowPublicKeyRetrieval=true";

    /** 資料庫帳號 */
    public static final String USER = "possystem";
    
    /** 資料庫密碼 */
    public static final String PASSWORD = "possystem";

    /**
     * 靜態初始化區塊 (Static Initializer)
     * ------------------------------------------------------------
     * 當類別第一次被載入時執行，用於：
     *  - 明確載入 MySQL JDBC Driver
     *  - 驗證驅動程式是否存在
     * ------------------------------------------------------------
     * 若未正確載入驅動，JDBC 將無法建立資料庫連線。
     */
    static {
        try {
            // 明確載入 MySQL 8.x 驅動程式類別
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println(" MySQL JDBC Driver 加載成功");
        } catch (ClassNotFoundException e) {
            System.err.println(" MySQL JDBC Driver error ");
        }
    }
}

/**
 * 
 */
/**
 * 
 */
// module-info.java
module pos.promotion.system {
    requires java.sql;
    requires java.desktop;
    requires java.base;
    requires org.junit.jupiter.api;  // ✅ JUnit API
    requires org.junit.jupiter.engine; // ✅ JUnit 引擎
    
    opens test to org.junit.platform.commons; // ✅ 開放 test 給 JUnit 反射使用
    
    exports model;
    exports repo;
    exports service;
    exports config;
}
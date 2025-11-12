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
    
    exports model;
    exports repo;
    exports service;
    exports config;
}
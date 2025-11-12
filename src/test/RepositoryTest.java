// test/RepositoryTest.java
package test;

import model.Item;
import model.Activity;
import repo.ItemRepository;
import repo.ActivityRepository;
import java.util.*;

public class RepositoryTest {
    public static void main(String[] args) {
        System.out.println("=== Repository层测试 ===");
        
        // 测试商品Repository
        testItemRepository();
        
        // 测试活动Repository  
        testActivityRepository();
    }
    
    private static void testItemRepository() {
        System.out.println("\n--- 商品Repository测试 ---");
        ItemRepository itemRepo = new ItemRepository();
        
        // 测试单个商品查询
        String testItemCode = "T4S5050600281"; // 用你数据库中存在的品号
        Item item = itemRepo.findItemByCode(testItemCode);
        
        if (item != null) {
            System.out.println("✅ 单个商品查询成功:");
            System.out.println("  品号: " + item.getItemCode());
            System.out.println("  品名: " + item.getItemName());
            System.out.println("  分类: " + item.getCategory01Name());
            System.out.println("  价格: " + item.getUnitPrice());
        } else {
            System.out.println("❌ 商品 " + testItemCode + " 未找到，尝试其他品号...");
            // 尝试查找任意商品
            findAnyItem(itemRepo);
        }
        
        // 测试批量查询
        List<String> itemCodes = Arrays.asList("T4S5050600281", "T4US5008837", "Snock4000499");
        List<Item> items = itemRepo.findItemsByCodes(itemCodes);
        System.out.println("✅ 批量查询: 找到 " + items.size() + " 个商品");
    }
    
    private static void findAnyItem(ItemRepository itemRepo) {
        // 这里可以尝试一些常见的品号
        String[] commonCodes = {"T4S5050600281", "T4US5008837", "Snock4000499", "CnL0009571"};
        for (String code : commonCodes) {
            Item item = itemRepo.findItemByCode(code);
            if (item != null) {
                System.out.println("找到商品: " + code + " - " + item.getItemName());
                return;
            }
        }
        System.out.println("未找到任何测试商品，请检查数据库数据");
    }
    
    private static void testActivityRepository() {
        System.out.println("\n--- 活动Repository测试 ---");
        ActivityRepository activityRepo = new ActivityRepository();
        
        // 测试有效活动查询
        Date testDate = new Date(); // 当前日期
        List<Activity> activities = activityRepo.findValidActivities(testDate);
        
        System.out.println("找到 " + activities.size() + " 个有效活动");
        for (Activity activity : activities) {
            System.out.println("✅ 活动: " + activity.getActivityName() + 
                             " | 满额: " + activity.getMeetCriteriaAmtG1() +
                             " | 折扣: " + activity.getAwardAmtG1());
        }
        
        if (activities.isEmpty()) {
            System.out.println("⚠️ 没有找到有效活动，测试将使用模拟数据");
        }
    }
}
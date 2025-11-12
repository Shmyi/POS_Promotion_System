package service;

import model.*;
import repo.ActivityRepository;
import repo.ItemRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 負責整體促銷邏輯的主要服務類。
 * -------------------------------------------------------
 * 計算順序符合 POS 銷售規則：
 *  1. 單項手動折扣（小計前）
 *  2. 身份活動折扣（公司會員95折，小計前）
 *  3. 條件活動折扣（滿額／分類活動，小計後）
 * -------------------------------------------------------
 * 作者：Yi Chen
 */
public class PromotionService {

    private ItemRepository itemRepository;
    private ActivityRepository activityRepository;
    private static final BigDecimal MEMBER_DISCOUNT_RATE = new BigDecimal("0.95");

    public PromotionService() {
        this.itemRepository = new ItemRepository();
        this.activityRepository = new ActivityRepository();
    }

    /** 主流程：計算整筆交易的收據金額 */
    public Receipt calculateReceipt(List<CartItem> cartItems, Date transactionDate, boolean isCompanyMember) {
        completeItemInfo(cartItems);
        List<Line> lines = convertToLines(cartItems);
        applyManualLineDiscount(lines); // Step 1
        Receipt receipt = new Receipt();
        receipt.setLines(lines);
        applyMemberDiscount(receipt, isCompanyMember); // Step 2
        List<Activity> validActivities = activityRepository.findValidActivities(transactionDate);
        applyPromotionActivities(receipt, validActivities); // Step 3
        receipt.calculateTotals();
        return receipt;
    }

    /** 批次補齊商品資訊 */
    private void completeItemInfo(List<CartItem> cartItems) {
        List<String> itemCodes = cartItems.stream()
                .map(CartItem::getItemCode)
                .collect(Collectors.toList());
        List<Item> items = itemRepository.findItemsByCodes(itemCodes);
        Map<String, Item> itemMap = items.stream()
                .collect(Collectors.toMap(Item::getItemCode, item -> item));

        for (CartItem cartItem : cartItems) {
            Item item = itemMap.get(cartItem.getItemCode());
            if (item != null) {
                cartItem.setItem(item);
            } else {
                System.err.println("警告: 未找到商品 " + cartItem.getItemCode());
            }
        }
    }

    /** 將購物車轉換為 Line 物件 */
    private List<Line> convertToLines(List<CartItem> cartItems) {
        List<Line> lines = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            if (cartItem.getItem() != null) {
                Line line = new Line(
                        cartItem.getItemCode(),
                        cartItem.getItem().getItemName(),
                        cartItem.getItem().getCategory01(),
                        cartItem.getItem().getCategory01Name(),
                        cartItem.getQuantity(),
                        cartItem.getItem().getUnitPrice()
                );
                if (cartItem.getManualDiscount() != null && cartItem.getManualDiscount().compareTo(BigDecimal.ZERO) > 0) {
                    line.setManualDiscount(cartItem.getManualDiscount());
                }
                lines.add(line);
            }
        }
        return lines;
    }

    /** Step 1: 手動折扣（小計前） */
    private void applyManualLineDiscount(List<Line> lines) {
        for (Line line : lines) {
            if (line.getManualDiscount() != null && line.getManualDiscount().compareTo(BigDecimal.ZERO) > 0) {
                line.applyManualDiscount(line.getManualDiscount());
                System.out.printf("[手動折扣] %s 折讓 %.0f 元%n", line.getItemName(), line.getManualDiscount());
            }
        }
    }

    /** Step 2: 公司會員折扣（酒類95折） */
    private void applyMemberDiscount(Receipt receipt, boolean isCompanyMember) {
        if (!isCompanyMember) return;

        for (Line line : receipt.getLines()) {
            if ("09".equals(line.getCategoryCode())) {
                line.applyMemberDiscount(MEMBER_DISCOUNT_RATE);
                System.out.println("[會員折扣] " + line.getItemName() + " → 套用95折");
            }
        }
        receipt.calculateTotals();
    }

    /** Step 3: 套用促銷活動 */
    private void applyPromotionActivities(Receipt receipt, List<Activity> activities) {
        for (Activity activity : activities) {
            List<Line> eligibleLines = findEligibleLines(receipt.getLines(), activity);
            BigDecimal eligibleTotal = calculateEligibleTotal(eligibleLines);

            if (eligibleTotal.compareTo(activity.getMeetCriteriaAmtG1()) >= 0 && eligibleTotal.compareTo(BigDecimal.ZERO) > 0) {
                distributeDiscount(eligibleLines, activity.getAwardAmtG1(), eligibleTotal);
                receipt.addActivityDiscount(activity.getActivityName(), activity.getAwardAmtG1());
                System.out.printf("[促銷觸發] %s | 分類: %s | 總金額: %,.0f | 折扣: %,.0f%n",
                        activity.getActivityName(),
                        activity.getItemDiscountGroup(),
                        eligibleTotal,
                        activity.getAwardAmtG1());
            }
        }
    }

    /** 找出符合活動條件的商品 */
    private List<Line> findEligibleLines(List<Line> lines, Activity activity) {
        return lines.stream()
                .filter(line -> isCategoryMatch(line.getCategoryCode(), activity.getItemDiscountGroup()))
                .collect(Collectors.toList());
    }

    private boolean isCategoryMatch(String itemCategoryCode, String activityCategoryGroup) {
        if (itemCategoryCode == null || activityCategoryGroup == null) return false;
        if (activityCategoryGroup.equalsIgnoreCase("ALL")) return true;
        for (String category : activityCategoryGroup.split(",")) {
            if (category.trim().equalsIgnoreCase(itemCategoryCode)) return true;
        }
        return false;
    }

    /** 計算符合條件商品的金額 */
    private BigDecimal calculateEligibleTotal(List<Line> eligibleLines) {
        return eligibleLines.stream()
                .map(Line::getFinalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** 折扣分攤邏輯 */
    private void distributeDiscount(List<Line> eligibleLines, BigDecimal totalDiscount, BigDecimal eligibleTotal) {
        if (eligibleLines == null || eligibleLines.isEmpty() || eligibleTotal.compareTo(BigDecimal.ZERO) == 0) return;

        final int SCALE = 6;
        BigDecimal distributed = BigDecimal.ZERO;

        for (int i = 0; i < eligibleLines.size(); i++) {
            Line line = eligibleLines.get(i);
            BigDecimal ratio = line.getFinalAmount().divide(eligibleTotal, SCALE, RoundingMode.HALF_UP);
            BigDecimal lineDiscount = (i == eligibleLines.size() - 1)
                    ? totalDiscount.subtract(distributed)
                    : totalDiscount.multiply(ratio).setScale(0, RoundingMode.HALF_UP);
            distributed = distributed.add(lineDiscount);
            line.applyPromotionDiscount(lineDiscount);
        }
    }
}

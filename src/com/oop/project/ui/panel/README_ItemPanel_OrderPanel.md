# 📦 ItemPanel & OrderPanel - Complete Documentation

## 🎉 New Panels Overview

### ItemPanel.java ✅ COMPLETE
**Item Catalog Management với Advanced Features:**
- ✅ CRUD operations (Add, Edit, Delete)
- ✅ **Admin-only price editing** (FR-2.4)
- ✅ SKU validation với format XXXX-NNN
- ✅ Duplicate SKU prevention (FR-2.3)
- ✅ Category filtering (8 categories)
- ✅ Stock quantity tracking
- ✅ Color-coded stock levels (Red=0, Orange<10)
- ✅ Active/Inactive status
- ✅ Search by name or SKU
- ✅ Role-based UI (Admin vs Staff)

### OrderPanel.java ✅ COMPLETE
**Order Management với Complex Features:**
- ✅ View all orders với detailed info
- ✅ **Create new order dialog**
- ✅ Add multiple items to order
- ✅ Real-time subtotal/tax/total calculation
- ✅ Order status management (Pending, Paid, Cancelled)
- ✅ Customer selection via dropdown
- ✅ Status filtering
- ✅ View order details
- ✅ Cancel order với confirmation
- ✅ Update order status
- ✅ Color-coded status indicators

---

## 🚀 Quick Start

### Test ItemPanel:
```bash
javac ItemPanel.java
java ItemPanel

# Try these actions:
# 1. Add item with SKU: ELEC-001
# 2. Update price (Admin only)
# 3. Filter by category
# 4. Search for items
```

### Test OrderPanel:
```bash
javac OrderPanel.java
java OrderPanel

# Try these actions:
# 1. Click "Create Order"
# 2. Select customer
# 3. Add items with quantities
# 4. See real-time total calculation
# 5. Create order
```

---

## 📊 ItemPanel Features Deep Dive

### 1. SKU Validation
```java
Format: XXXX-NNN
Examples:
  ✅ ELEC-001  (Electronics)
  ✅ CLOT-001  (Clothing)
  ✅ FOOD-001  (Food & Beverage)
  ❌ ELEC-1    (Wrong format)
  ❌ elec-001  (Must be uppercase)
```

### 2. Admin-Only Price Editing
```java
// When creating ItemPanel:
ItemPanel panel = new ItemPanel("ADMIN");  // Can edit prices
ItemPanel panel = new ItemPanel("STAFF");  // Cannot edit prices

// Price update button only visible for Admin
btnUpdatePrice.setVisible(isAdmin());
```

### 3. Stock Level Indicators
```
Stock = 0    → Red background (Out of Stock)
Stock < 10   → Orange background (Low Stock)
Stock >= 10  → Normal (White background)
```

### 4. Category System
```java
Available Categories:
1. Electronics
2. Clothing
3. Food & Beverage
4. Home & Garden
5. Sports & Outdoors
6. Books & Media
7. Toys & Games
8. Health & Beauty
9. Other
```

### 5. Sample Items Included
```
ELEC-001 - Laptop Dell XPS 13 - $1,299.99 (15 in stock)
ELEC-002 - iPhone 15 Pro - $999.99 (8 in stock) ⚠️
CLOT-001 - T-Shirt Nike - $29.99 (50 in stock)
FOOD-001 - Organic Coffee - $15.50 (0 in stock) ❌
HOME-001 - LED Desk Lamp - $45.00 (25 in stock)
```

---

## 🛒 OrderPanel Features Deep Dive

### 1. Create Order Workflow
```
Step 1: Click "Create Order" button
Step 2: Select customer from dropdown
Step 3: Select item from catalog
Step 4: Set quantity (1-99)
Step 5: Click "Add to Order"
        → Item appears in order table
        → Totals update automatically
Step 6: Repeat Step 3-5 for more items
Step 7: Click "Create Order"
        → Order saved
        → Returns to order list
```

### 2. Real-Time Calculation
```java
Subtotal = Sum of (Price × Quantity) for all items
Tax = Subtotal × 8%
Total = Subtotal + Tax

Example:
  Laptop x1: $1,299.99
  Mouse x2:  $   51.98
  ──────────────────────
  Subtotal:  $1,351.97
  Tax (8%):  $  108.16
  ──────────────────────
  TOTAL:     $1,460.13
```

### 3. Order Status System
```
⏳ Pending  → Yellow background → New orders
✓ Paid     → Green background  → Completed
✗ Cancelled → Red background   → Cancelled

Status can be updated:
- Select order
- Click "Update Status"
- Choose new status
```

### 4. Order Details View
```
Double-click any order to see:
- Order ID
- Customer name
- Order date
- Status
- List of items with quantities
- Subtotal, Tax, Total
```

### 5. Sample Orders Included
```
ORD-001 | John Doe    | 3 items | $270.00  | Paid
ORD-002 | Jane Smith  | 2 items | $162.00  | Pending
ORD-003 | Bob Johnson | 5 items | $540.00  | Paid
ORD-004 | Alice W.    | 1 item  | $97.19   | Pending
ORD-005 | Charlie B.  | 4 items | $345.60  | Cancelled
```

---

## 🎨 UI/UX Highlights

### ItemPanel:
```
✅ Role indicator in title: [Admin Mode] or [View Mode]
✅ Color-coded stock levels for quick scanning
✅ Active/Inactive status with checkmark/X
✅ Price displayed with $ symbol and 2 decimals
✅ Admin-only buttons have purple color
✅ Hover effects on all buttons
✅ Double-click to edit
```

### OrderPanel:
```
✅ Status badges with emojis (⏳ ✓ ✗)
✅ Color-coded status backgrounds
✅ Money amounts right-aligned with $ symbol
✅ Real-time total updates
✅ Item count per order
✅ Professional order details view
✅ Separate dialog for order creation
```

---

## 🔧 Integration with MainFrame

### Update MainFrame to use ItemPanel and OrderPanel:

```java
// In MainFrame.java or MainFrameWithCustomerPanel.java

private void initComponents() {
    // ✅ Real implementations
    customerPanel = new CustomerPanel();
    itemPanel = new ItemPanel(currentRole);     // NEW!
    orderPanel = new OrderPanel();               // NEW!
    
    // TODO: Still need these
    billingPanel = createPlaceholderPanel(...);
    dashboardPanel = createPlaceholderPanel(...);
}
```

---

## 📋 Code Examples

### Example 1: ItemPanel with Role-Based Access

```java
// Create ItemPanel for Admin
ItemPanel adminPanel = new ItemPanel("ADMIN");
// Can see and use "Update Price" button

// Create ItemPanel for Staff
ItemPanel staffPanel = new ItemPanel("STAFF");
// "Update Price" button is hidden
// Price field is read-only when editing
```

### Example 2: Validating SKU Format

```java
// In ItemPanel.validateItemForm()
if (!sku.matches("^[A-Z]{4}-\\d{3}$")) {
    showError("Invalid SKU format!\n\n" +
             "Format: XXXX-NNN\n" +
             "Example: ELEC-001");
    return false;
}

// Prevents duplicate SKU
if (!isEditing && isDuplicateSKU(sku)) {
    showError("SKU already exists!");
    return false;
}
```

### Example 3: Creating Order Programmatically

```java
// This is what the CreateOrderDialog does internally:

// 1. Select customer
String customer = "John Doe";

// 2. Add items
List<OrderItem> items = new ArrayList<>();
items.add(new OrderItem("Laptop", 1, 1299.99));
items.add(new OrderItem("Mouse", 2, 25.99));

// 3. Calculate totals
double subtotal = items.stream()
    .mapToDouble(item -> item.price * item.quantity)
    .sum();
double tax = subtotal * 0.08;
double total = subtotal + tax;

// 4. Create order (Week 6)
// orderService.createOrder(customer, items);
```

---

## 🧪 Testing Scenarios

### ItemPanel Test Cases:

**TC-01: Add Item with Valid SKU**
```
1. Click "Add Item"
2. Enter SKU: ELEC-001
3. Enter Name: Test Laptop
4. Enter Price: 999.99
5. Select Category: Electronics
6. Click Save

Expected: ✅ Item added, appears in table
```

**TC-02: Duplicate SKU Prevention**
```
1. Try to add item with existing SKU: ELEC-001
2. Click Save

Expected: ❌ Error: "SKU already exists!"
```

**TC-03: Admin Price Update**
```
1. Login as Admin
2. Select an item
3. Click "Update Price"
4. Enter new price: 1199.99
5. Click OK

Expected: ✅ Price updated in table
```

**TC-04: Staff Cannot Update Price**
```
1. Login as Staff
2. Select an item

Expected: ❌ "Update Price" button not visible
```

**TC-05: Category Filter**
```
1. Select "Electronics" from category filter
2. Observe table

Expected: ✅ Only electronics items shown
```

### OrderPanel Test Cases:

**TC-06: Create Complete Order**
```
1. Click "Create Order"
2. Select Customer: "John Doe"
3. Select Item: "Laptop - $1,299.99"
4. Quantity: 1
5. Click "Add to Order"
6. Verify subtotal updates
7. Add another item
8. Click "Create Order"

Expected: ✅ Order created, appears in order list
```

**TC-07: Real-Time Total Calculation**
```
1. In Create Order dialog
2. Add item: Laptop x1 ($1,299.99)
3. Observe: 
   - Subtotal: $1,299.99
   - Tax (8%): $103.99
   - Total: $1,403.98
4. Add item: Mouse x2 ($51.98)
5. Observe totals update immediately

Expected: ✅ All totals update in real-time
```

**TC-08: Status Filter**
```
1. Select "Pending" from status filter
2. Observe table

Expected: ✅ Only pending orders shown
```

**TC-09: Cancel Order**
```
1. Select an order
2. Click "Cancel Order"
3. Confirm in dialog
4. Observe status change

Expected: ✅ Status changes to "Cancelled"
```

**TC-10: View Order Details**
```
1. Double-click any order
2. Observe details dialog

Expected: ✅ Shows complete order information
```

---

## 🔗 Service Layer Integration (Week 6)

### ItemPanel Integration Points:

```java
// Replace these TODOs with actual service calls:

// Load items
List<Item> items = itemService.getAllItems(showInactive);

// Add item
Item item = itemService.addItem(name, sku, price, category, stock);

// Update item
itemService.updateItem(item);

// Update price (Admin only)
itemService.updatePrice(sku, newPrice, adminUsername);

// Delete item
itemService.deleteItem(sku);

// Search items
List<Item> results = itemService.searchItems(keyword);

// Check duplicate SKU
boolean exists = itemService.checkSkuDuplicate(sku);
```

### OrderPanel Integration Points:

```java
// Replace these TODOs with actual service calls:

// Load orders
List<Order> orders = orderService.getAllOrders();

// Create order
Order order = orderService.createOrder(customerId, items, couponCode);

// Get order details
Order order = orderService.getOrderById(orderId);
List<OrderItem> items = orderService.getOrderItems(orderId);

// Update status
orderService.updateOrderStatus(orderId, newStatus);

// Cancel order
orderService.cancelOrder(orderId); // Also creates audit log

// Filter by status
List<Order> orders = orderService.getOrdersByStatus(status);

// Search orders
List<Order> results = orderService.searchOrders(keyword);
```

---

## 📊 Complete UI Layer Status

### ✅ Completed (Week 5-6):
```
LoginFrame          ✅ Complete
LoginFrameAdvanced  ✅ Complete
MainFrame           ✅ Complete
CustomerPanel       ✅ Complete
ItemPanel           ✅ Complete
OrderPanel          ✅ Complete
```

### 🚧 Remaining (Week 6-7):
```
BillingPanel        🚧 TODO (Week 7)
DashboardPanel      🚧 TODO (Week 7)
```

---

## 💡 Tips for Team Members

### For Thành viên 1 (Model):
```java
// ItemPanel needs:
public class Item {
    private String sku;
    private String name;
    private double price;
    private String category;
    private int stockQuantity;
    private boolean isActive;
    private Date lastUpdated;
    // getters, setters
}

// OrderPanel needs:
public class Order {
    private String orderId;
    private int customerId;
    private Date orderDate;
    private OrderStatus status;
    private double subtotal;
    private double taxAmount;
    private double totalAmount;
    // getters, setters
}

public class OrderItem {
    private String orderId;
    private String itemSku;
    private int quantity;
    private double unitPrice;
    private double subtotal;
    // getters, setters
}

public enum OrderStatus {
    PENDING, PAID, CANCELLED
}
```

### For Thành viên 2 (Repository):
```java
// Key queries needed:

// Items
SELECT * FROM items WHERE is_active = true;
SELECT * FROM items WHERE sku = ?;
SELECT * FROM items WHERE category = ?;
INSERT INTO items (sku, name, price, ...) VALUES (?, ?, ?, ...);
UPDATE items SET price = ? WHERE sku = ?;

// Orders
SELECT o.*, c.name as customer_name 
FROM orders o 
JOIN customers c ON o.customer_id = c.id;

SELECT * FROM order_items WHERE order_id = ?;

INSERT INTO orders (customer_id, order_date, ...) VALUES (?, ?, ...);
INSERT INTO order_items (order_id, item_sku, ...) VALUES (?, ?, ...);

UPDATE orders SET status = ? WHERE order_id = ?;
```

### For Thành viên 3 (Service):
```java
// Business logic needed:

// ItemService
- Validate SKU format
- Check duplicate SKU before insert
- Admin-only price update check
- Stock level warnings

// OrderService  
- Calculate subtotal, tax (8%), total
- Validate customer exists
- Validate all items exist and in stock
- Create audit log for cancellations
- Support overloaded computeBill() methods:
  * computeBill(price)
  * computeBill(price, quantity)
  * computeBill(price, quantity, couponDiscount)
```

---

## 🎯 Key Achievements

### ItemPanel:
✅ Admin-only features implemented (FR-2.4)
✅ SKU validation prevents duplicates (FR-2.3)
✅ Category-based organization (FR-2.2)
✅ Stock tracking with visual indicators
✅ Role-based UI

### OrderPanel:
✅ Complete order creation workflow (FR-3.1)
✅ Multiple items per order (FR-3.2)
✅ Automatic tax calculation (FR-3.4)
✅ Status management (FR-4.3)
✅ Real-time total updates

---

## 📞 Next Steps

1. **Week 6**: Connect to Service Layer
2. **Week 7**: Implement BillingPanel (with overloaded methods)
3. **Week 7**: Implement DashboardPanel (with statistics)
4. **Week 8**: Integration testing
5. **Week 9**: Final polish and delivery

---

**Created by:** Thành viên 4 (UI Layer)  
**Week:** 5-6 - Advanced Panel Implementation  
**Status:** ✅ ItemPanel & OrderPanel Complete!  
**Files:** ItemPanel.java (30KB), OrderPanel.java (29KB)


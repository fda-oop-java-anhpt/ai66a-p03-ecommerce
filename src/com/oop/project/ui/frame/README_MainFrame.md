# 📱 MainFrame & CustomerPanel - Complete Documentation

## 📦 Package Contents

### 1️⃣ MainFrame.java
- Main application window với JTabbedPane
- Menu bar với File, Settings, Help, Logout
- Status bar với user info và clock
- Role-based access control
- Placeholder panels for all tabs

### 2️⃣ CustomerPanel.java  
- **COMPLETE implementation** of Customer Management
- JTable với CRUD operations
- Search functionality
- Input validation
- Real-time updates

### 3️⃣ MainFrameWithCustomerPanel.java
- **Integrated version** showing how to combine them
- Example of replacing placeholders with real panels
- Ready to use and test!

---

## 🎯 Features Overview

### MainFrame Features ✅
- [x] JTabbedPane với 5 tabs (Customers, Items, Orders, Billing, Dashboard)
- [x] Menu bar với keyboard shortcuts (F5, Ctrl+E, F1, etc.)
- [x] Status bar hiển thị username, role, date/time
- [x] Real-time clock update mỗi giây
- [x] Role-based tab visibility (Admin vs Staff)
- [x] Welcome message khi login
- [x] Logout confirmation
- [x] Exit confirmation
- [x] Window close handler
- [x] Custom tab styling

### CustomerPanel Features ✅
- [x] JTable hiển thị customer list
- [x] Add new customer
- [x] Edit existing customer
- [x] Delete customer với confirmation
- [x] Search by name or phone
- [x] View customer order history
- [x] Phone number validation (Vietnamese format)
- [x] Email validation
- [x] Double-click to edit
- [x] Button states based on selection
- [x] Refresh data
- [x] Professional form dialog
- [x] Real-time table updates

---

## 🚀 Quick Start

### Option 1: Test MainFrame Only
```bash
javac MainFrame.java
java MainFrame
```

### Option 2: Test CustomerPanel Only
```bash
javac CustomerPanel.java
java CustomerPanel
```

### Option 3: Test Integrated Version (RECOMMENDED)
```bash
javac CustomerPanel.java
javac MainFrameWithCustomerPanel.java
java MainFrameWithCustomerPanel
```

---

## 📐 Architecture

### MainFrame Structure
```
MainFrame
├── Menu Bar
│   ├── File (Refresh, Export, Print, Exit)
│   ├── Settings (Preferences, Theme, Admin settings)
│   ├── Help (User Guide, Shortcuts, About)
│   └── Logout
│
├── JTabbedPane
│   ├── Tab 0: CustomerPanel ✅
│   ├── Tab 1: ItemPanel (TODO)
│   ├── Tab 2: OrderPanel (TODO)
│   ├── Tab 3: BillingPanel (TODO)
│   └── Tab 4: DashboardPanel (TODO)
│
└── Status Bar
    ├── Left: User info (username, role)
    └── Right: Date/Time (updating clock)
```

### CustomerPanel Structure
```
CustomerPanel
├── Top Panel
│   ├── Title: "👥 Customer Management"
│   └── Search Bar (with Search & Clear buttons)
│
├── Center Panel
│   └── JTable in JScrollPane
│       ├── Columns: ID, Name, Phone, Email, Address, Date
│       ├── Single selection mode
│       ├── Row height: 25px
│       └── Double-click to edit
│
└── Bottom Panel
    ├── Add Customer
    ├── Edit (disabled when no selection)
    ├── Delete (disabled when no selection)
    ├── View Orders (disabled when no selection)
    └── Refresh
```

---

## 💻 Code Examples

### Example 1: How to Integrate a New Panel

```java
// In MainFrame.java, replace placeholder:

// OLD:
itemPanel = createPlaceholderPanel("Item Catalog", "...");

// NEW:
itemPanel = new ItemPanel(); // Your actual ItemPanel class
```

### Example 2: How to Pass Data Between Panels

```java
// In MainFrame, when creating panels:
private CustomerPanel customerPanel;
private OrderPanel orderPanel;

private void initComponents() {
    customerPanel = new CustomerPanel();
    orderPanel = new OrderPanel();
    
    // Pass reference so OrderPanel can access customers
    orderPanel.setCustomerPanel(customerPanel);
}

// In OrderPanel:
public void setCustomerPanel(CustomerPanel customerPanel) {
    this.customerPanel = customerPanel;
}

// Now OrderPanel can get customer list:
List<Customer> customers = customerPanel.getCustomers();
```

### Example 3: How to Add Service Layer (Week 6)

```java
// In CustomerPanel, replace sample data:

// OLD (Week 5):
private void loadCustomerData() {
    tableModel.setRowCount(0);
    
    // Sample data
    Object[][] sampleData = {...};
    for (Object[] row : sampleData) {
        tableModel.addRow(row);
    }
}

// NEW (Week 6):
private CustomerService customerService;

public CustomerPanel(CustomerService customerService) {
    this.customerService = customerService;
    initComponents();
    setupLayout();
    loadCustomerData();
}

private void loadCustomerData() {
    tableModel.setRowCount(0);
    
    try {
        // ✅ Call actual service
        List<Customer> customers = customerService.getAllCustomers();
        
        for (Customer customer : customers) {
            tableModel.addRow(new Object[]{
                customer.getId(),
                customer.getName(),
                customer.getPhone(),
                customer.getEmail(),
                customer.getAddress(),
                customer.getCreatedDate()
            });
        }
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this,
            "Error loading customers: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
}
```

### Example 4: Form Validation Pattern

```java
// CustomerPanel already has this implemented
private boolean validateCustomerForm() {
    // 1. Check required fields
    if (txtName.getText().trim().isEmpty()) {
        showError("Name is required!");
        txtName.requestFocus();
        return false;
    }
    
    // 2. Format validation
    String phone = txtPhone.getText().trim();
    if (!phone.matches("^0\\d{9,10}$")) {
        showError("Invalid phone format!");
        txtPhone.requestFocus();
        return false;
    }
    
    // 3. Email validation (optional field)
    String email = txtEmail.getText().trim();
    if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
        showError("Invalid email format!");
        txtEmail.requestFocus();
        return false;
    }
    
    return true;
}
```

---

## 🎨 UI/UX Best Practices Applied

### 1. **Visual Feedback**
```java
// Buttons change color on hover
button.addMouseListener(new MouseAdapter() {
    public void mouseEntered(MouseEvent e) {
        button.setBackground(color.darker());
    }
    public void mouseExited(MouseEvent e) {
        button.setBackground(color);
    }
});
```

### 2. **Disabled State Management**
```java
// Buttons disabled when no selection
private void updateButtonStates() {
    boolean hasSelection = customerTable.getSelectedRow() >= 0;
    btnEdit.setEnabled(hasSelection);
    btnDelete.setEnabled(hasSelection);
    btnViewOrders.setEnabled(hasSelection);
}
```

### 3. **Keyboard Navigation**
```java
// Enter key triggers search
txtSearch.addActionListener(e -> performSearch());

// Double-click to edit
customerTable.addMouseListener(new MouseAdapter() {
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
            editCustomer();
        }
    }
});
```

### 4. **Confirmation Dialogs**
```java
// Always confirm destructive actions
int confirm = JOptionPane.showConfirmDialog(this,
    "Are you sure you want to delete customer:\n" + name,
    "Confirm Delete",
    JOptionPane.YES_NO_OPTION,
    JOptionPane.WARNING_MESSAGE);
```

---

## 🧪 Testing Guide

### Manual Test Cases for CustomerPanel

#### TC-01: Add New Customer
```
Steps:
1. Click "Add Customer" button
2. Fill in form:
   - Name: "Test User"
   - Phone: "0901234567"
   - Email: "test@email.com"
   - Address: "123 Test St"
3. Click "Save"

Expected:
✅ New row appears in table
✅ Success message displayed
✅ Dialog closes
```

#### TC-02: Edit Existing Customer
```
Steps:
1. Select a customer row
2. Click "Edit" button (or double-click row)
3. Modify name to "Updated Name"
4. Click "Save"

Expected:
✅ Row updates with new name
✅ Success message displayed
✅ Dialog closes
```

#### TC-03: Delete Customer
```
Steps:
1. Select a customer row
2. Click "Delete" button
3. Click "Yes" in confirmation dialog

Expected:
✅ Confirmation dialog appears
✅ Row removed from table
✅ Success message displayed
```

#### TC-04: Search Functionality
```
Steps:
1. Enter "john" in search field
2. Click "Search" button

Expected:
✅ Table shows only matching customers
✅ Non-matching rows hidden
```

#### TC-05: Validation - Empty Name
```
Steps:
1. Click "Add Customer"
2. Leave Name field empty
3. Fill other fields
4. Click "Save"

Expected:
❌ Error dialog: "Name is required!"
❌ Dialog stays open
✅ Focus on Name field
```

#### TC-06: Validation - Invalid Phone
```
Steps:
1. Click "Add Customer"
2. Enter phone: "123" (invalid)
3. Click "Save"

Expected:
❌ Error dialog: "Invalid phone format!"
✅ Shows format example
```

#### TC-07: View Customer Orders
```
Steps:
1. Select a customer
2. Click "View Orders"

Expected:
✅ Dialog shows order history
✅ Displays total spent
```

---

## 🔧 Customization Guide

### Change Table Colors
```java
// In CustomerPanel constructor
customerTable.getTableHeader().setBackground(Color.BLUE);
customerTable.getTableHeader().setForeground(Color.WHITE);
customerTable.setSelectionBackground(new Color(200, 220, 240));
```

### Add More Table Columns
```java
// Modify table model
String[] columns = {
    "ID", "Name", "Phone", "Email", "Address", 
    "Loyalty Points",  // New column
    "Created Date"
};

// Update loadCustomerData() to include new column
tableModel.addRow(new Object[]{
    id, name, phone, email, address,
    loyaltyPoints,  // New data
    date
});
```

### Add Export to Excel Feature
```java
private void exportToExcel() {
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Save Excel File");
    
    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        File file = chooser.getSelectedFile();
        
        // TODO Week 7: Use Apache POI to write Excel
        // Workbook workbook = new XSSFWorkbook();
        // Sheet sheet = workbook.createSheet("Customers");
        // ... write table data to sheet
        
        JOptionPane.showMessageDialog(this,
            "Exported to: " + file.getAbsolutePath(),
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
    }
}
```

---

## 📋 Integration Checklist

### Week 5 (Current) ✅
- [x] MainFrame structure complete
- [x] CustomerPanel fully implemented
- [x] Integration example provided
- [x] Can run and test independently

### Week 6 (Next Steps) 📝
- [ ] Replace ItemPanel placeholder with actual implementation
- [ ] Replace OrderPanel placeholder with actual implementation
- [ ] Connect CustomerPanel to CustomerService
- [ ] Implement data persistence
- [ ] Add loading indicators for database operations

### Week 7 📝
- [ ] Implement BillingPanel
- [ ] Implement DashboardPanel
- [ ] Add export functionality
- [ ] Add print functionality

### Week 8 📝
- [ ] Complete integration testing
- [ ] Performance optimization
- [ ] UI polish and refinement
- [ ] Bug fixes

---

## 🐛 Known Issues & Workarounds

### Issue 1: Table Not Refreshing
**Problem:** Table doesn't update after add/edit/delete  
**Solution:** Call `tableModel.fireTableDataChanged()` after modifications

### Issue 2: Button States Not Updating
**Problem:** Edit/Delete buttons stay disabled  
**Solution:** Call `updateButtonStates()` after table selection changes

### Issue 3: Form Dialog Not Centered
**Problem:** Dialog appears at screen corner  
**Solution:** Call `dialog.setLocationRelativeTo(this)` before `setVisible()`

---

## 💡 Tips for Other Team Members

### For Thành viên 2 (Repository Layer)
```java
// CustomerPanel will need these methods:
public interface CustomerRepository {
    List<Customer> findAll();
    Customer findById(int id);
    List<Customer> searchByNameOrPhone(String keyword);
    void save(Customer customer);
    void update(Customer customer);
    void delete(int id);
}
```

### For Thành viên 3 (Service Layer)
```java
// CustomerPanel will call these:
public class CustomerService {
    public List<Customer> getAllCustomers();
    public Customer createCustomer(String name, String phone, 
                                   String email, String address);
    public void updateCustomer(Customer customer);
    public void deleteCustomer(int id);
    public List<Customer> searchCustomers(String keyword);
    public List<Order> getCustomerOrderHistory(int customerId);
}
```

### For Thành viên 1 (Model Layer)
```java
// Customer entity needed by CustomerPanel:
public class Customer {
    private Integer id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private Date createdDate;
    private int loyaltyPoints;
    
    // Constructors, getters, setters
}
```

---

## 📞 FAQ

**Q: Có thể test CustomerPanel riêng biệt không?**  
A: Có! Run `java CustomerPanel` để test panel độc lập.

**Q: Làm sao để thêm panel mới?**  
A: Copy structure của CustomerPanel, thay đổi logic cho panel mới, rồi thêm vào MainFrame.

**Q: Sample data có lưu vào database không?**  
A: Không. Đây chỉ là demo data. Week 6 sẽ kết nối database thật.

**Q: Có thể thay đổi color scheme không?**  
A: Có! Modify các constants PRIMARY_COLOR, SUCCESS_COLOR, etc.

---

## 🎓 Learning Resources

### JTable Documentation
- [Oracle JTable Tutorial](https://docs.oracle.com/javase/tutorial/uiswing/components/table.html)
- TableModel và DefaultTableModel
- Custom cell renderers

### Layout Managers
- BorderLayout (cho MainFrame)
- GridBagLayout (cho forms)
- FlowLayout (cho button panels)

### Best Practices
- Event Dispatch Thread (EDT)
- SwingWorker for background tasks
- Input validation patterns

---

**Status:** ✅ Ready for Week 6 integration!  
**Next:** Implement ItemPanel following CustomerPanel pattern  
**Contact:** Thành viên 4 (UI Layer)


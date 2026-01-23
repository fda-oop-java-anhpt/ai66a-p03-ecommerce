## Project requirments
Project 3: E-Commerce Sales and Billing System

3.1 Problem Description
A small online retail business requires a complete billing and order management system
to handle item catalogs, customer information, and invoices for product purchases. The
existing workflow relies on manual calculators and spreadsheets, causing frequent pricing
errors, missing discount rules, inconsistent tax application, and no record of past orders.
Additionally, the shop lacks a login mechanism, making it difficult to restrict access to
administrative tasks such as modifying product prices.
The upgraded system must function as a full e-commerce billing platform and provide:
• Secure user login and role-based access
• Customer record management
• Item catalog management and price maintenance
• Order creation and dynamic billing calculation
• Overloaded billing methods for different pricing scenarios
• Discount and tax calculation rules
• Order dashboard with search, sorting, and filtering
• Persistent file-based storage for all modules
• A full Java Swing GUI for all workflows

3.2 Functional Requirements
FR-0: Login and Authentication
• FR-0.1: The system shall require a username and password at login.
• FR-0.2: The system shall validate credentials using a stored user file.
• FR-0.3: The system shall support multiple roles (Admin, Staff).
• FR-0.4: Admin users shall be allowed to modify product prices and tax rules.
• FR-0.5: The system shall record each login and logout event.


FR-1: Customer Management
• FR-1.1: The system shall allow creating, updating, and deleting customer profiles.
• FR-1.2: The system shall validate phone number and email formats.
• FR-1.3: The system shall allow searching customers by name or phone number.
• FR-1.4: The system shall display all past orders associated with a customer.


FR-2: Item Catalog Management
• FR-2.1: The system shall allow adding, updating, and removing items in the cata-
log.
• FR-2.2: The system shall store item name, unit price, category, and SKU code.
• FR-2.3: The system shall prevent duplicate SKU codes.
• FR-2.4: The system shall allow Admin users to modify the price of any item.


FR-3: Order and Billing Management
• FR-3.1: The system shall allow creating, viewing, updating, and deleting orders.
• FR-3.2: The system shall allow adding multiple items with quantity to an order.
• FR-3.3: The system shall calculate total price using overloaded computeBill()
methods:
– computeBill(price)
– computeBill(price, quantity)
– computeBill(price, quantity, couponDiscount)
• FR-3.4: The system shall apply a fixed 8% tax to every order.
• FR-3.5: The system shall generate a printable invoice containing item list, discount,
tax, and final total.


FR-4: Additional Core Features
• FR-4.1: The system shall allow defining coupon codes with percentage or fixed
discount.
• FR-4.2: The system shall validate coupon expiration dates.
• FR-4.3: The system shall provide order status categories (Pending, Paid, Can-
celled).
• FR-4.4: The system shall maintain an audit log for order creation, updates, and
cancellations.


FR-5: Order Dashboard and Search
• FR-5.1: The system shall display all orders in a sortable list (customer, amount,
date, status).
• FR-5.2: The system shall filter orders by status or date range.
• FR-5.3: The system shall allow searching orders by customer name or order ID.
• FR-5.4: The system shall provide summary statistics (total orders, revenue, can-
celled orders).


FR-6: Persistence and GUI Requirements
• FR-6.1: The system shall store customers, items, and orders using Sequential File
I/O.
• FR-6.2: The system shall store user login credentials in an encoded file.
• FR-6.3: The GUI shall include separate tabs for Customers, Items, Orders, Billing,
and Dashboard.
• FR-6.4: The GUI shall display pricing updates in real time when quantity or dis-
counts change.
• FR-6.5: The GUI shall show all validation or error messages via JOptionPane.
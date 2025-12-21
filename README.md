# 📦 Warehouse Management System

## 📌 Project Description
The Warehouse Management System is a Java desktop application designed to handle warehouse inventory using **Java Swing**, **SQLite**, and **JDBC**, following a clean **MVC architecture**.

## 🎯 Objectives
- Digitize warehouse activity  
- Reduce human errors  
- Enable faster stock updates  
- Provide better UI for inventory management  

## 🏗 System Architecture

### 🔹 UI Layer (Swing)
Handles user interaction:
- Product table  
- Add/Edit dialog  
- Stock receive/dispatch  
- Search bar  

### 🔹 Service Layer (`WarehouseService`)
- Business logic  
- Cache using ConcurrentHashMap  
- Coordinates UI + DAO  

### 🔹 DAO Layer (`ProductDaoImpl`)
- JDBC operations  
- CRUD handling  
- SQLite communication  

### 🔹 Database Schema
```sql
CREATE TABLE product (
    product_id INTEGER PRIMARY KEY,
    sku TEXT,
    name TEXT,
    description TEXT,
    price REAL,
    quantity INTEGER,
    min_stock INTEGER
);
```

---

## ✨ Features
### ✔ Product Management
- Add / Edit / Delete products  
- Auto-increment visible index  
- Hidden DB ID for safe deletion  

### ✔ Inventory Ops
- Receive stock  
- Dispatch stock  
- Quantity validation  
- Low-stock detection  

### ✔ Search System
- Search by Name  
- Search by SKU  
- Case-insensitive  

### ✔ UI Features
- JTable for clean data display  
- Background tasks via SwingWorker  
- Tooltip showing DB ID  

---

## 📂 Project Structure
```
src/
 ├── app/
 │     ├── Main.java
 │     └── UI/
 │           ├── MainFrame.java
 │           ├── ProductPanel.java
 │           └── ProductFormDialog.java
 ├── model/
 │     └── Product.java
 ├── service/
 │     └── WarehouseService.java
 ├── dao/
 │     ├── ProductDao.java
 │     └── ProductDaoImpl.java
 └── util/
       └── SimpleLogger.java
```

---

## ▶️ How to Run the Project
1. Install **Java 17+**  
2. Open project in **IntelliJ IDEA**  
3. Run `Main.java`  
4. Database auto-creates  

---

## 👥 Team Semicolon
- **Dhruv Mittal (Leader)**  
- **Kriti Biswas**  
- **Ayush Kumar Rai**

## 🔮 Future Enhancements
- Login system  
- Export to PDF/Excel  
- Supplier module  
- Cloud integration  

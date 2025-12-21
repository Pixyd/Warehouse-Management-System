# 🏬 Warehouse Management System (WMS)

A **Java-based Warehouse Management System** built using **Swing GUI, JDBC, SQLite, and layered architecture**.  
This application efficiently manages products, inventory movements, and low-stock alerts with **data consistency and transaction safety**.

---

## 📌 Project Overview

The **Warehouse Management System** helps in managing warehouse inventory by allowing users to:

- Add, edit, and delete products  
- Receive and dispatch stock  
- Automatically track inventory changes  
- Trigger **low-stock alerts**  
- Maintain **transaction consistency** between stock updates and logs  

The system follows a **clean architecture**:

```
UI (Swing) → Service Layer → DAO Layer → Database (SQLite)
```

---

## 🛠️ Technologies Used

- **Java (JDK 8+)**
- **Java Swing** – GUI
- **JDBC** – Database connectivity
- **SQLite** – Embedded database
- **Gradle** – Build tool
- **Git & GitHub** – Version control
- **IntelliJ IDEA** – Development environment

---

## 📂 Project Structure

```
warehouse-management/
│
├── src/main/java/
│   ├── app/
│   │   ├── Main.java
│   │   └── UI/
│   │       ├── MainFrame.java
│   │       ├── ProductPanel.java
│   │       └── ProductFormDialog.java
│   │
│   ├── service/
│   │   └── WarehouseService.java
│   │
│   ├── dao/
│   │   ├── ProductDao.java
│   │   ├── ProductDaoImpl.java
│   │   └── DbManager.java
│   │
│   ├── model/
│   │   └── Product.java
│   │
│   └── util/
│       └── SimpleLogger.java
│
├── src/main/resources/
│   └── sql/schema.sql
│
├── warehouse.db
├── build.gradle
└── README.md
```

---

## ⚙️ Core Features

### ✅ Product Management
- Add new products  
- Edit existing products  
- Delete products safely  

### ✅ Inventory Control
- Receive stock (Stock In)  
- Dispatch stock (Stock Out)  
- Prevents negative inventory  

### ✅ Atomic Inventory Transactions
Each inventory operation:
- Updates **product quantity**
- Inserts a corresponding **inventory transaction record**
- Executes inside a **single database transaction**

➡️ Ensures **data consistency** at all times.

### ✅ Low Stock Alert System
- Automatically detects when stock ≤ minimum stock  
- Alerts **only once per low-stock event**  
- Alert resets when stock is restored  
- Prevents repeated popup spam  

### ✅ Search & UI Features
- Search by product name or SKU  
- Clean tabular view with serial numbering  
- Hidden database IDs (safe UI design)  

---

## 🚨 Low Stock Alert Logic (Improved)

- Alert triggers **only when stock crosses the threshold**
- Uses in-memory tracking to avoid duplicate alerts
- Automatically re-enables alert if stock is refilled and drops again

---

## ▶️ How to Run the Project

1. Open the project in **IntelliJ IDEA**
2. Ensure **JDK 8+** is configured
3. Let **Gradle sync** complete
4. Run:
   ```
   src/main/java/app/Main.java
   ```
5. The application window will launch

---

## 🧪 Sample Product Entries

| SKU | Name | Price | Quantity | Min Stock |
|----|------|-------|----------|-----------|
| 101 | USB Keyboard | 799 | 50 | 10 |
| 102 | Wireless Mouse | 599 | 40 | 8 |
| 107 | Wireless Headphones | 3499 | 3 | 5 |

---

## 👨‍💻 Team Details

**Team Name:** Semicolon  

**Team Leader:**  
- Dhruv Mittal  

**Team Members:**  
- Kriti Biswas  
- Ayush Kumar Rai  

---

## 📈 Academic Highlights

- ✔ Layered architecture (UI–Service–DAO)
- ✔ JDBC with transaction management
- ✔ Atomic inventory updates
- ✔ Exception handling & logging
- ✔ Clean, user-friendly GUI
- ✔ GitHub version control

---

## 📜 License

This project is developed for **academic and learning purposes**.

---

### ✅ Status: Completed & Submitted

This project is **fully functional, optimized, and presentation-ready**.

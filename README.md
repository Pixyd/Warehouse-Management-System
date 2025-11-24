# Warehouse Management System (Java Swing + SQLite)

## What's included
- Java Swing GUI application
- SQLite via JDBC (sqlite-jdbc required)
- DAO pattern, Service layer, PreparedStatements, Transactions
- Background low-stock monitor (ScheduledExecutorService)
- Product add/edit/delete, search, receive/dispatch stock

## How to open in IntelliJ
1. Unzip the project.
2. Open IntelliJ -> Open -> select the `warehouse-management` folder.
3. Mark `src/main/resources` as Resources Root (right-click -> Mark Directory As -> Resources Root).
4. Add SQLite JDBC to project libraries: download sqlite-jdbc jar from https://github.com/xerial/sqlite-jdbc/releases and add via File -> Project Structure -> Libraries.
5. Run `app.Main` (right-click Main.java -> Run 'Main.main()').

## Build with Gradle (optional)
A sample `build.gradle` is included. You can import the project as a Gradle project or add the SQLite dependency to your build.

## Notes
- On first run the app initializes `warehouse.db` using `src/main/resources/sql/schema.sql`.
- If you package as a jar, ensure `schema.sql` is included in resources.


# ClassSync — DB Setup Guide

## Requirements
- XAMPP installed with MySQL running on port 3306

## Steps

1. Start XAMPP and make sure **MySQL is running**
2. Open **phpMyAdmin** at `http://localhost/phpmyadmin`
3. Click the **SQL** tab and run the script in `db_setup.sql`
4. Open `src/main/resources/db.properties` and update if needed:
   db.url=jdbc:mysql://localhost:3306/classsync_db?useSSL=false&serverTimezone=UTC
   db.user=root
   db.password=

text

5. Run **Maven Reload** in IntelliJ
6. Build and run the app

## Default Login Credentials

| Name | Email | Password | Role |
|---|---|---|---|
| Alex Johnson | alex@school.edu | pass123 | STUDENT |
| Sarah Chen | sarah@school.edu | pass123 | STUDENT |
| Mike Ross | mike@school.edu | pass123 | STUDENT |
| Prof. Santos | santos@school.edu | pass123 | INSTRUCTOR |
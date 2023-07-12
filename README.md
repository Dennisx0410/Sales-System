Sales system
===

It is a group project for *CSCI3170 Introduction to Database Systems (2022 Fall)*.
In this project, we have to implement a sales system in Java, JDBC and mySQL for a computer part store so that all information about
transactions, computer parts and salespersons is stored.
This is a mirrored archive of the original project.

---

## Project information
- Project name: Sales System
- Group number: 34
- Group members:
    - Ip Tsz Ho 1155144251
    - Sung Man Shing 1155142857
    - Yeung Long Sang 1155168581
- Department of Computer Science and Engineering, The Chinese University of Hong Kong

## Getting started
- Compile files
    ```
        javac SalesSystem.java Admin.java Salesperson.java Manager.java
    ```
- Run program
    ```
        java -classpath ./mysql-jdbc.jar:./ SalesSystem Admin Salesperson Manager
    ```
- or compile-and-run (bash script for linux only) **(Recommended)**
    ``` 
        bash run.sh
    ```

## List of files
### `SalesSystem.java`
The main program of the sales System
### `Admin.java`
Class of the operations for admin menu
### `Salesperson.java`
Class of the operations for salesperson menu
### `Manager.java`
Class of the operations for manager menu
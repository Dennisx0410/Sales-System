# clean old class files
rm SalesSystem.class Admin.class Salesperson.class Manager.class
javac SalesSystem.java Admin.java Salesperson.java Manager.java
java -classpath ./mysql-jdbc.jar:./ SalesSystem Admin Salesperson Manager

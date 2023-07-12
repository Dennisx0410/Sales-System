import java.io.*;
import java.sql.*;

public class SalesSystem {
    public static String dbAddress = "{address}";
    public static String dbUsername = "{username}";
    public static String dbPassword = "{password}";

    public static String[] tableNames = {
        "category", 
        "manufacturer", 
        "salesperson",
        "part", 
        "transaction"
    };

    // Trigger for forcing format of insert
    public static String[] triggerNames = {"manufacturer.insertmphonecheck", "manufacturer.updatemphonecheck", "salesperson.insertsphonecheck", "salesperson.updatesphonecheck"};
    
    public static int showMenu() {
        System.out.println();
        System.out.println("-----Main menu-----");
        System.out.println("What kinds of operation would you like to perform?");
        System.out.println("1. Operations for administrator");
        System.out.println("2. Operations for salesperson");
        System.out.println("3. Operations for manager");
        System.out.println("4. Exit this program");
        System.out.print("Enter Your choice: ");

        BufferedReader in;

        // read choice
        String str = "";
        int choice = -1;
		try {
            in = new BufferedReader(new InputStreamReader(System.in));
            str = in.readLine();
            choice = Integer.parseInt(str);
        }
		catch (IOException e) {
			System.err.println("Error: " + e);
        }

        return choice;
    }

    public static void main(String[] args) throws Exception {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            
        } catch (Exception x) {
            System.err.println("Unable to load the driver class!");
        }
        Connection con = DriverManager.getConnection(dbAddress, dbUsername, dbPassword);

        System.out.println("Welcome to sales system!");
        int choice = -1;
        boolean isActive = true;
        while (isActive) {
            try {
                choice = showMenu();
                switch (choice) {
                    case 1:
                        Admin admin = new Admin(con);
                        admin.showMenu();
                        break;
                    case 2:
                        Salesperson salesperson = new Salesperson(con);
                        salesperson.showMenu();
                        break;
                    case 3:
                        Manager manager = new Manager(dbAddress, dbUsername, dbPassword, tableNames);
                        manager.showMenu();
                        break;
                    case 4:
                        isActive = false;
                        break;
                    default:
                        System.out.println("Invalid Choice! Returning to the main menu...");
                }
            }
            catch (SQLException e) {
                System.err.println("SQLException: " + e);
            }
        }

        //close connection
        con.close();
    }
}

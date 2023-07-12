import java.io.*;
import java.sql.*;

public class Manager {
    public Connection con;
    public String[] tableNames;

    public Manager(String dbAddress, String dbUsername, String dbPassword, String[] tableNames) throws SQLException {
        this.con = DriverManager.getConnection(dbAddress, dbUsername, dbPassword);
        this.tableNames = tableNames;
    }
    
   /* public Manager(Connection con) {
        this.con = con;
    } */

     public void listSales() throws SQLException{
        System.out.println("Choose ordering:");
        System.out.println("1. By ascending order");
        System.out.println("2. By descending order");
        System.out.print("Choose the list ordering: ");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String ordering = "ASC";
        String str;
        int choice = -1;
         /*
        // Input Choice
        int choice = -1;
		try {
            str = in.readLine();
            choice = Integer.parseInt(str);
        }
		catch (IOException e) {
			System.out.println("Error: " + e);
        } */

        try {
            str = in.readLine();
            choice = Integer.parseInt(str);
            if (Integer.parseInt(str) == 1){
                ordering = "ASC";
            }else if (Integer.parseInt(str) == 2){
                ordering = "DESC";
            }else{
                System.out.println("Invalid Choice! Returning to the main menu...");
                return;
            }
        }
		catch (IOException e) {
			System.out.println("Error: " + e);
        }

        String queryString = "";
        switch (choice) {
            case 1:
                // Construct Query String Searching for part name
                queryString = String.format("SELECT sID, sName, sPhoneNumber, sExperience FROM salesperson AS S ORDER BY sExperience %s;", ordering);
                break;
            case 2:
                // Construct Query String searching for manufacturer name
                queryString = String.format("SELECT sID, sName, sPhoneNumber, sExperience FROM salesperson AS S ORDER BY sExperience %s;", ordering);
                break;
            default:
                System.out.println("Invalid Choice! Returning to the main menu...");
                return;
        }

        // Execute Query
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(queryString);
        ResultSetMetaData rsmd = rs.getMetaData();
        int colCnt = rsmd.getColumnCount();
        String[] displayColsName = {"ID", "Name", "Mobile Phone", "Years of Experience"};

        // print column name
        for (int i=0; i<colCnt; i++) {
            System.out.printf("| %s ", displayColsName[i]);
        }
        System.out.println("|");

        // print row fields
        while (rs.next()) {
            for (int i=1; i<=colCnt; i++) {
                String field = rs.getString(i);
                System.out.printf("| %s ", field);
            }
            System.out.println("|");
        }

        System.out.println("End of query.");
        rs.close();
    }

    public void countRecords() throws SQLException{
        System.out.print("Type in the lower bound for years of experience: ");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String str = "";
        int lowerBound = -1;
        try {
            str = in.readLine();
            lowerBound = Integer.parseInt(str);
        }
		catch (IOException e) {
			System.out.println("Error: " + e);
        }

        System.out.print("Type in the upper bound for years of experience: ");
        int upperBound = 999;
        try {
            str = in.readLine();
            upperBound = Integer.parseInt(str);
        }
		catch (IOException e) {
			System.out.println("Error: " + e);
        }

        // Execute Query
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(String.format("SELECT s.sID, sName, sExperience, COUNT(*) FROM salesperson AS s, transaction AS t WHERE s.sID = t.sID AND sExperience >= %d AND sExperience <= %d GROUP BY s.SID ORDER BY s.sID DESC", lowerBound, upperBound));
        ResultSetMetaData rsmd = rs.getMetaData();
        int colCnt = rsmd.getColumnCount();
        String[] displayColsName = {"ID", "Name", "Years of Experience", "Number of Transaction"};

        // print column name
        for (int i=0; i<colCnt; i++) {
            System.out.printf("| %s ", displayColsName[i]);
        }
        System.out.println("|");

        // print row fields
        while (rs.next()) {
            for (int i=1; i<=colCnt; i++) {
                String field = rs.getString(i);
                System.out.printf("| %s ", field);
            }
            System.out.println("|");
        }

        System.out.println("End of query");
        rs.close();
    }

    public void showTotalSales() throws SQLException{
        // Execute Query
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(String.format("SELECT m.mID, m.mName, SUM(p.pPrice) AS Total FROM manufacturer AS m, part AS p, transaction AS t WHERE m.mID = p.mID AND t.pID = p.pID GROUP BY m.mID ORDER BY Total DESC"));
        ResultSetMetaData rsmd = rs.getMetaData();
        int colCnt = rsmd.getColumnCount();
        String[] displayColsName = {"Manufacturer ID", "Manufacturer Name", "Total Sales Value"};

        // print column name
        for (int i=0; i<colCnt; i++) {
            System.out.printf("| %s ", displayColsName[i]);
        }
        System.out.println("|");

        // print row fields
        while (rs.next()) {
            for (int i=1; i<=colCnt; i++) {
                String field = rs.getString(i);
                System.out.printf("| %s ", field);
            }
            System.out.println("|");
        }

        System.out.println("End of query");
        rs.close();
    }

    public void showPopularParts() throws SQLException{
        System.out.print("Type in the number of parts: ");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String str = "";
        int limit = -1;
        try {
            str = in.readLine();
            limit = Integer.parseInt(str);
        }
		catch (IOException e) {
			System.out.println("Error: " + e);
        }

        // Execute Query
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT p.pID, p.pName, COUNT(*) AS tCount FROM part as p, transaction as t WHERE p.pID = t.pID GROUP BY p.pID ORDER BY tCount DESC");
        ResultSetMetaData rsmd = rs.getMetaData();
        int colCnt = rsmd.getColumnCount();
        String[] displayColsName = {"Part ID", "Part Name", "No. of Transaction"};

        // print column name
        for (int i=0; i<colCnt; i++) {
            System.out.printf("| %s ", displayColsName[i]);
        }
        System.out.println("|");

        // print row fields
        while (rs.next() && limit > 0) {
            for (int i=1; i<=colCnt; i++) {
                String field = rs.getString(i);
                System.out.printf("| %s ", field);
            }
            System.out.println("|");
            limit--;
        }

        System.out.println("End of query");
        rs.close();
    }

    public void showMenu() throws SQLException {
        System.out.println();
        System.out.println("-----Operations for manager menu-----");
        System.out.println("What kinds of operation would you like to perform?");
        System.out.println("1. List all salesperson");
        System.out.println("2. Count the no. of sales record of each saleperson under a specfic range on years of experience");
        System.out.println("3. Show the total sales value of each manufacturer");
        System.out.println("4. Show the N most popular part");
        System.out.println("5. Return to the main menu");
        System.out.print("Enter Your choice: ");

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String str;
        int choice = -1;
		try {
            str = in.readLine();
            choice = Integer.parseInt(str);
        }
		catch (IOException e) {
			System.out.println("Error: " + e);
        }

        switch (choice) {
            case 1: 
                listSales();
                break;
            case 2:
                countRecords();
                break;
            case 3:
                showTotalSales();
                break;
            case 4:
                showPopularParts();
                break;
            case 5:
                // close connection
                this.con.close();
                this.con = null;
                return;
            default:
                // close connection
                this.con.close();
                this.con = null;
                return;
        }
    }
}

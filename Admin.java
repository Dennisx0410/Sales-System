import java.io.*;
import java.text.ParseException;  
import java.text.SimpleDateFormat;  
import java.util.Date;  
import java.sql.*;

public class Admin {
    public Connection con;
    public static final String DATE_FMT_OLD = "dd/MM/yyyy";
    public static final String DATE_FMT_NEW = "yyyy-MM-dd";

    public Admin(Connection con) {
        this.con = con;
    }

    public void createTables() throws SQLException {
        System.out.print("Processing...");
        
        Statement stmt = con.createStatement();
        String stmtStr;

        // create category table
        stmtStr = "CREATE TABLE category("
                + "cID INT(1) NOT NULL PRIMARY KEY,"
                + "cName VARCHAR(20) NOT NULL"
                + ");";
        stmt.executeUpdate(stmtStr);

        // create manufacturer table
        stmtStr = "CREATE TABLE manufacturer("
                + "mID INT(2) NOT NULL PRIMARY KEY,"
                + "mName VARCHAR(20) NOT NULL,"
                + "mAddress VARCHAR(50) NOT NULL,"
                + "mPhoneNumber INT(8) NOT NULL"
                + ");";
        stmt.executeUpdate(stmtStr);
        // add trigger to force format
        // Insert Checking on phone number
        stmtStr = "CREATE TRIGGER insertmphonecheck BEFORE INSERT ON manufacturer FOR EACH ROW BEGIN IF NEW.mPhoneNumber < 10000000 OR NEW.mPhoneNumber >= 100000000 THEN signal sqlstate '45000'; end if; end;";
        stmt.executeUpdate(stmtStr);

        // Update checking on phone number
        stmtStr = "CREATE TRIGGER updatemphonecheck BEFORE UPDATE ON manufacturer FOR EACH ROW BEGIN IF NEW.mPhoneNumber < 10000000 OR NEW.mPhoneNumber >= 100000000 THEN signal sqlstate '45000'; end if; end;";
        stmt.executeUpdate(stmtStr);

        // create salesperson table
        stmtStr = "CREATE TABLE salesperson("
                + "sID INT(2) NOT NULL PRIMARY KEY,"
                + "sName VARCHAR(20) NOT NULL,"
                + "sAddress VARCHAR(50) NOT NULL,"
                + "sPhoneNumber INT(8) NOT NULL,"
                + "sExperience INT(1) NOT NULL"
                + ");";
        stmt.executeUpdate(stmtStr);
        // add trigger to force format
        // Insert Checking on phone number
        stmtStr = "CREATE TRIGGER insertsphonecheck BEFORE INSERT ON salesperson FOR EACH ROW BEGIN IF NEW.sPhoneNumber < 10000000 OR NEW.sPhoneNumber >= 100000000 THEN signal sqlstate '45000'; end if; end;";
        stmt.executeUpdate(stmtStr);

        // Update checking on phone number
        stmtStr = "CREATE TRIGGER updatesphonecheck BEFORE UPDATE ON salesperson FOR EACH ROW BEGIN IF NEW.sPhoneNumber < 10000000 OR NEW.sPhoneNumber >= 100000000 THEN signal sqlstate '45000'; end if; end;";
        stmt.executeUpdate(stmtStr);

        // create part table
        stmtStr = "CREATE TABLE part("
                + "pID INT(3) NOT NULL PRIMARY KEY,"
                + "pName VARCHAR(20) NOT NULL,"
                + "pPrice INT(5) NOT NULL,"
                + "mID INT(2) NOT NULL,"
                + "cID INT(1) NOT NULL,"
                + "pWarrantyPeriod INT(2) NOT NULL,"
                + "pAvailableQuantity INT(2) NOT NULL,"
                + "FOREIGN KEY (mID) REFERENCES manufacturer(mID),"
                + "FOREIGN KEY (cID) REFERENCES category(cID)"
                + ");";
        stmt.executeUpdate(stmtStr);

        // create transaction table
        stmtStr = "CREATE TABLE transaction("
                + "tID INT(4) NOT NULL PRIMARY KEY,"
                + "pID INT(3) NOT NULL,"
                + "sID INT(2) NOT NULL,"
                + "tDate DATE NOT NULL,"
                + "FOREIGN KEY (pID) REFERENCES part(pID),"
                + "FOREIGN KEY (sID) REFERENCES salesperson(sID)"
                + ");";
        stmt.executeUpdate(stmtStr);

        stmt.close();
        System.out.println("Done! Database is initialized!");
    }

    public void deletTables() throws SQLException {
        System.out.print("Processing...");
        Statement stmt = con.createStatement();

        // drop tables
        for (int i=SalesSystem.tableNames.length-1; i>=0; i--) { // reverse the order of the table names
            String name = SalesSystem.tableNames[i];
            String stmtStr = String.format("DROP TABLE IF EXISTS %s;", name);
            stmt.executeUpdate(stmtStr);
        }
        // Delete all related triggers
        for (String name : SalesSystem.tableNames) {
            String stmtStr = String.format("DROP TRIGGER IF EXISTS %s;", name);
            stmt.executeUpdate(stmtStr);
        }
        stmt.close();
        System.out.println("Done! Database is removed!");
    }

    public void loadFromFile() throws SQLException { 
        System.out.println();
        System.out.print("Type in the Source Data Folder Path: ");

        BufferedReader in;

        // read source data folder path
        String pathSrc = "";
		try {
            in = new BufferedReader(new InputStreamReader(System.in));
            pathSrc = in.readLine();
        }
		catch (IOException e) {
			System.err.println("Error: " + e);
        }

        // read files and records to all tables
        System.out.print("Processing...");
        try {
            String line = "";
            String fieldsPlaceholder = "";
            
            for (String name : SalesSystem.tableNames) {
                // generate fields placeholder for prepared statement
                if (name.equals("category")) fieldsPlaceholder = "(?, ?)";
                else if (name.equals("manufacturer")) fieldsPlaceholder = "(?, ?, ?, ?)";
                else if (name.equals("part")) fieldsPlaceholder = "(?, ?, ?, ?, ?, ?, ?)";
                else if (name.equals("salesperson")) fieldsPlaceholder = "(?, ?, ?, ?, ?)";
                else if (name.equals("transaction")) fieldsPlaceholder = "(?, ?, ?, ?)";

                PreparedStatement pstmt = con.prepareStatement(String.format("INSERT into %s VALUES %s", name, fieldsPlaceholder));
                in = new BufferedReader(new FileReader(new File(String.format("%s/%s.txt", pathSrc, name))));

                // read lines
                while ((line = in.readLine()) != null) {
                    String[] fields = line.split("\t");

                    // fillin fields
                    for (int i=0; i<fields.length; i++) {
                        if (name.equals("transaction") && i == fields.length - 1) {
                            SimpleDateFormat formatter = new SimpleDateFormat(DATE_FMT_OLD);
                            try {
                                Date date = formatter.parse(fields[i]);
                                formatter.applyPattern(DATE_FMT_NEW);
                                String dateStr = formatter.format(date);
                                java.sql.Date sqlDate = java.sql.Date.valueOf(dateStr); //converting string into sql date  
                                pstmt.setDate(i + 1, sqlDate); // fields is 0 based but sql is 1 based
                            }
                            catch (ParseException e) {
                                System.err.println("Erorr: " + e);
                            }  
                        }
                        else {
                            pstmt.setString(i + 1, fields[i]); // fields is 0 based but sql is 1 based
                        }
                    }
                    pstmt.executeUpdate();
                }

                pstmt.close();
            }
        }
        catch (IOException e) {
            System.err.println("Error: " + e);
        }
        System.out.println("Done! Database is inputted to the database!");
    }

    public void showTable() throws SQLException {
        System.out.print("Which table would you like to show: ");

        BufferedReader in;

        // read table name
        String name = "";
		try {
            in = new BufferedReader(new InputStreamReader(System.in));
            name = in.readLine();
        }
		catch (IOException e) {
			System.err.println("Error: " + e);
        }

        System.out.printf("Content of the table %s:\n", name);

        // get table content
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(String.format("SELECT * FROM %s", name));
        ResultSetMetaData rsmd = rs.getMetaData();
        int colCnt = rsmd.getColumnCount();
        
        // print column name
        for (int i=1; i<=colCnt; i++) {
            System.out.printf("| %s ", rsmd.getColumnName(i));
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

        rs.close();
    }

    public void showMenu() throws SQLException {
        System.out.println();
        System.out.println("-----Operations for administrator menu-----");
        System.out.println("What kinds of operation would you like to perform?");
        System.out.println("1. Create all tables");
        System.out.println("2. Delete all tables");
        System.out.println("3. Load from datafile");
        System.out.println("4. Show content of a table");
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
                createTables();
                break; 
            case 2:
                deletTables();
                break; 
            case 3:
                loadFromFile();
                break;
            case 4:
                showTable();
                break;
            case 5:
                System.out.println("Returning to the main menu...");
                return;
            default:
                System.out.println("Invalid Choice! Returning to the main menu...");
                return;
        }
    }
}


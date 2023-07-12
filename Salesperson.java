import java.io.*;
import java.sql.*;
public class Salesperson {
    public Connection con;

    public Salesperson(Connection con) {
        this.con = con;
    }

    public void searchPart() throws SQLException{
        System.out.println("Choose the Search criterion:");
        System.out.println("1. Part Name");
        System.out.println("2. Manufacturer Name");
        System.out.print("Choose the search criterion: ");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String str;

        // Input Choice
        int choice = -1;
		try {
            str = in.readLine();
            choice = Integer.parseInt(str);
        }
		catch (IOException e) {
			System.out.println("Error: " + e);
        }

        // Input Keyword
        String searchKey = "";
        System.out.print("Type in the Search Keyword: ");
		try {
            searchKey = in.readLine();
        }
		catch (IOException e) {
			System.out.println("Error: " + e);
        }

        // Input ordering
        System.out.println("Choose ordering:");
        System.out.println("1. By price, ascending order");
        System.out.println("2. By price, descending order");
        System.out.print("Choose the ordering: ");
        String ordering = "ASC";
        try {
            str = in.readLine();
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
                queryString = String.format("SELECT pID, pName, mName, cName, pAvailableQuantity, pWarrantyPeriod, pPrice FROM part AS P, category AS C, manufacturer AS M WHERE P.cID = C.cID AND P.mID = M.mID AND pName LIKE BINARY '%s' ORDER BY pPrice %s;","%"+searchKey+"%", ordering);
                break;
            case 2:
                // Construct Query String searching for manufacturer name
                queryString = String.format("SELECT pID, pName, mName, cName, pAvailableQuantity, pWarrantyPeriod, pPrice FROM part AS P, category AS C, manufacturer AS M WHERE P.cID = C.cID AND P.mID = M.mID AND mName LIKE BINARY '%s' ORDER BY pPrice %s;","%"+searchKey+"%", ordering);
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
        String[] displayColsName = {"ID", "Name", "Manufacturer", "Category", "Quantity", "Warranty", "Price"};

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

    public void sellPart() throws SQLException{
        System.out.print("Enter The Part ID: ");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String str;
        // Read part ID
        int partID = -1;
        try {
            str = in.readLine();
            partID = Integer.parseInt(str);
        }
        catch (IOException e) {
            System.out.println("Error: " + e);
        }

        // Read salesperson id
        System.out.print("Enter The Salesperson ID: ");
        int salesID = -1;
        try {
            str = in.readLine();
            salesID = Integer.parseInt(str);
        }
        catch (IOException e) {
            System.out.println("Error: " + e);
        }
        
        // Perform part exist and quantity check
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(String.format("SELECT pId, pName, pAvailableQuantity FROM part WHERE pID = %s", partID));
        String partName;
        int quantity;
        if (rs.next()){
            if (rs.getInt(3) <= 0){
                System.out.println("The requested part is out of stock.");
                return;
            }else{
                partName = rs.getString(2);
                quantity = rs.getInt(3);
            }
        }else{
            System.err.println("Part with such pID doesn't exist.");
            return;
        }

        // Perform salesman exist check
        ResultSet rs2 = stmt.executeQuery(String.format("SELECT * FROM salesperson WHERE sID = %s", salesID));
        if (!rs2.next()){
            System.err.println("Salesperson with such sID doesn't exist.");
            return;
        }

        stmt.executeUpdate(String.format("UPDATE part SET pAvailableQuantity = pAvailableQuantity - 1 WHERE pID = %s", partID));
        // query the total number of transaction
        rs = stmt.executeQuery("SELECT COUNT(*) FROM transaction");
        int tID = -1;
        if (rs.next()){
            tID = rs.getInt(1) + 1;
        }else{
            System.err.println("Error occurred when querying transaction count.");
            return;
        }
        long mills = System.currentTimeMillis();
        String query = "INSERT INTO transaction VALUES (?, ?, ?, ?)";
        PreparedStatement pstmt = con.prepareStatement(query);
        pstmt.setInt(1, tID);
        pstmt.setInt(2, partID);
        pstmt.setInt(3, salesID);
        pstmt.setDate(4, new Date(mills));
        pstmt.execute();
        System.out.println(String.format("Product: %s(id: %d) Remaining Quality: %d", partName, partID, quantity - 1));
    }

    public void showMenu() throws SQLException {
        System.out.println();
        System.out.println("-----Operations for salesperson menu-----");
        System.out.println("What kinds of operation would you like to perform?");
        System.out.println("1. Search for parts");
        System.out.println("2. Sell a part");
        System.out.println("3. Return to the main menu");
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
                searchPart();
                break;
            case 2:
                sellPart();
                break;
            case 3:
                System.out.println("Returning to the main menu...");
                return;
            default:
                System.out.println("Invalid Choice! Returning to the main menu...");
                return;
        }
    }
}

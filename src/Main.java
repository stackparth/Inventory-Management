import java.sql.*;
import java.util.Scanner;

public class Main {

    static final String URL =
            "jdbc:mysql://localhost:3306/retail_db";
    static final String USER = "root";
    static final String PASSWORD = "root";

    static Connection con;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(
                    URL, USER, PASSWORD
            );

            System.out.println("Database Connected Successfully!");

            while (true) {

                System.out.println("\n===== INVENTORY MANAGEMENT SYSTEM =====");
                System.out.println("1. Insert Product Records");
                System.out.println("2. Retrieve Products by Category");
                System.out.println("3. Update Product Stock");
                System.out.println("4. Search and Modify Product Price");
                System.out.println("5. Aggregate Query and Conditional Deletion");
                System.out.println("6. Exit");

                System.out.print("Enter your choice: ");
                int choice = sc.nextInt();

                switch (choice) {

                    case 1:
                        insertProducts();
                        break;

                    case 2:
                        retrieveProducts();
                        break;

                    case 3:
                        updateStock();
                        break;

                    case 4:
                        modifyProduct();
                        break;

                    case 5:
                        aggregateAndDelete();
                        break;
                    case 6:
                        con.close();
                        System.out.println("Program Exited!");
                        return;

                    default:
                        System.out.println("Invalid Choice!");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // (a) Insert Product Records
    static void insertProducts() {

        String query = "INSERT INTO product " +
                "(product_id, product_name, category, quantity, price) " +
                "VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = con.prepareStatement(query);

            for (int i = 1; i <= 3; i++) {

                System.out.println("\nEnter details of Product " + i);

                System.out.print("Product ID: ");
                int id = sc.nextInt();
                sc.nextLine();

                System.out.print("Product Name: ");
                String name = sc.nextLine();

                System.out.print("Category: ");
                String category = sc.nextLine();

                System.out.print("Quantity: ");
                int quantity = sc.nextInt();

                System.out.print("Price: ");
                double price = sc.nextDouble();

                ps.setInt(1, id);
                ps.setString(2, name);
                ps.setString(3, category);
                ps.setInt(4, quantity);
                ps.setDouble(5, price);

                try {
                    int rows = ps.executeUpdate();

                    if (rows > 0) {
                        System.out.println("Product inserted successfully!");
                    }

                } catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println("Duplicate Product ID! Try another ID.");
                }
            }

            ps.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (b) Conditional Retrieval and Sorting
    static void retrieveProducts() {

        String query = "SELECT * FROM product " +
                "WHERE category = ? AND quantity < 20 " +
                "ORDER BY price DESC";

        try {
            System.out.print("Enter category: ");
            sc.nextLine();
            String category = sc.nextLine();

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, category);

            ResultSet rs = ps.executeQuery();

            System.out.println("\n----- PRODUCT DETAILS -----");

            boolean found = false;

            while (rs.next()) {

                found = true;

                System.out.println("Product ID: " + rs.getInt("product_id"));
                System.out.println("Product Name: " + rs.getString("product_name"));
                System.out.println("Category: " + rs.getString("category"));
                System.out.println("Quantity: " + rs.getInt("quantity"));
                System.out.println("Price: " + rs.getDouble("price"));

                System.out.println("---------------------------");
            }

            if (!found) {
                System.out.println("No matching products found!");
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (c) Update Product Stock
    static void updateStock() {

        String query = "UPDATE product SET quantity = quantity + ? " +
                "WHERE product_id = ?";

        try {
            System.out.print("Enter Product ID: ");
            int id = sc.nextInt();

            System.out.print("Enter quantity to add: ");
            int addedQuantity = sc.nextInt();

            if (addedQuantity <= 0) {
                System.out.println("Quantity must be greater than zero!");
                return;
            }

            PreparedStatement ps = con.prepareStatement(query);

            ps.setInt(1, addedQuantity);
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Product stock updated successfully!");

                String selectQuery = "SELECT * FROM product WHERE product_id = ?";
                PreparedStatement ps2 = con.prepareStatement(selectQuery);
                ps2.setInt(1, id);

                ResultSet rs = ps2.executeQuery();

                if (rs.next()) {
                    System.out.println("\n----- UPDATED PRODUCT DETAILS -----");
                    System.out.println("Product ID: " + rs.getInt("product_id"));
                    System.out.println("Product Name: " + rs.getString("product_name"));
                    System.out.println("Quantity: " + rs.getInt("quantity"));
                    System.out.println("Price: " + rs.getDouble("price"));
                }

                rs.close();
                ps2.close();

            } else {
                System.out.println("Product does not exist!");
            }

            ps.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (d) Search and Modify Product Details
    static void modifyProduct() {

        try {
            System.out.print("Enter Product ID: ");
            int id = sc.nextInt();

            String selectQuery = "SELECT * FROM product WHERE product_id = ?";

            PreparedStatement ps = con.prepareStatement(selectQuery);
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                System.out.println("\n----- PRODUCT DETAILS -----");
                System.out.println("Product ID: " + rs.getInt("product_id"));
                System.out.println("Product Name: " + rs.getString("product_name"));
                System.out.println("Category: " + rs.getString("category"));
                System.out.println("Quantity: " + rs.getInt("quantity"));
                System.out.println("Current Price: " + rs.getDouble("price"));

                rs.close();
                ps.close();

                System.out.print("\nEnter New Price: ");
                double newPrice = sc.nextDouble();

                if (newPrice <= 0) {
                    System.out.println("Price must be greater than zero!");
                    return;
                }

                String updateQuery = "UPDATE product SET price = ? WHERE product_id = ?";

                PreparedStatement ps2 = con.prepareStatement(updateQuery);

                ps2.setDouble(1, newPrice);
                ps2.setInt(2, id);

                int rows = ps2.executeUpdate();

                if (rows > 0) {

                    System.out.println("Product price updated successfully!");

                    String updatedQuery = "SELECT * FROM product WHERE product_id = ?";

                    PreparedStatement ps3 = con.prepareStatement(updatedQuery);
                    ps3.setInt(1, id);

                    ResultSet rs2 = ps3.executeQuery();

                    if (rs2.next()) {
                        System.out.println("\n----- UPDATED PRODUCT DETAILS -----");
                        System.out.println("Product ID: " + rs2.getInt("product_id"));
                        System.out.println("Product Name: " + rs2.getString("product_name"));
                        System.out.println("Category: " + rs2.getString("category"));
                        System.out.println("Quantity: " + rs2.getInt("quantity"));
                        System.out.println("New Price: " + rs2.getDouble("price"));
                    }

                    rs2.close();
                    ps3.close();
                }

                ps2.close();

            } else {
                System.out.println("Product does not exist!");
                rs.close();
                ps.close();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // (e) Aggregate Query and Conditional Deletion
    static void aggregateAndDelete() {

        try {

            // Aggregate Query
            String aggregateQuery =
                    "SELECT COUNT(*) AS total_products, " +
                            "SUM(quantity) AS total_stock, " +
                            "AVG(price) AS average_price FROM product";

            Statement st = con.createStatement();

            ResultSet rs = st.executeQuery(aggregateQuery);

            if (rs.next()) {

                System.out.println("\n----- INVENTORY SUMMARY -----");

                System.out.println("Total Products: " +
                        rs.getInt("total_products"));

                System.out.println("Total Stock: " +
                        rs.getInt("total_stock"));

                System.out.println("Average Price: " +
                        rs.getDouble("average_price"));
            }

            rs.close();
            st.close();

            // Conditional Deletion
            System.out.print("\nEnter Product ID to delete: ");
            int id = sc.nextInt();

            String deleteQuery =
                    "DELETE FROM product WHERE product_id = ? AND quantity = 0";

            PreparedStatement ps = con.prepareStatement(deleteQuery);

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                System.out.println("Product deleted successfully!");
            } else {
                System.out.println(
                        "Product not found or stock quantity is not zero!"
                );
            }

            ps.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

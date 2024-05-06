package DigitalStore;
import java.sql.*;
import java.util.Scanner;

public class DigitalStore {
    static Scanner scanner = new Scanner(System.in);
    static Connection conn = null;

    public static void main(String[] args) {
        try {
            establishConnection();

            createTables();

            System.out.println("Welcome to Digital Store!");
            while (true) {
                System.out.println("\nAre you a new user, existing user, or admin?");
                System.out.println("1. New User");
                System.out.println("2. Existing User");
                System.out.println("3. Admin");
                System.out.println("4. Exit");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline character

                switch (choice) {
                    case 1:
                        newUser();
                        break;
                    case 2:
                        existingUser();
                        break;
                    case 3:
                        adminLogin();
                        break;
                    case 4:
                        System.out.println("Goodbye!");
                        closeConnection();
                        return;
                    default:
                        System.out.println("Invalid choice!");
                        break;
                }
            }
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection();
        }
    }

    private static void establishConnection() throws SQLException {
        String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
        String DB_URL = "jdbc:mysql://localhost/digital_store";
        String USER = "root";
        String PASS = "root";

        conn = DriverManager.getConnection(DB_URL, USER, PASS);
    }

    private static void createTables() throws SQLException {
        Statement stmt = conn.createStatement();
        String createUserTableSQL = "CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) UNIQUE, password VARCHAR(50), admin BOOLEAN)";
        String createProductTableSQL = "CREATE TABLE IF NOT EXISTS products (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) UNIQUE, price DOUBLE, stock INT)";
        stmt.executeUpdate(createUserTableSQL);
        stmt.executeUpdate(createProductTableSQL);

        // Add default admin user
        String insertAdminUserSQL = "INSERT IGNORE INTO users (username, password, admin) VALUES (?, ?, ?)";
        PreparedStatement adminUserStatement = conn.prepareStatement(insertAdminUserSQL);
        adminUserStatement.setString(1, "Dheeraj");
        adminUserStatement.setString(2, "Nothing@123");
        adminUserStatement.setBoolean(3, true);
        adminUserStatement.executeUpdate();
    }

    private static void closeConnection() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        }
    }

    private static void newUser() throws SQLException {
        System.out.println("Enter username:");
        String username = scanner.nextLine();

        System.out.println("Enter password:");
        String password = scanner.nextLine();

        System.out.println("Verify password:");
        String verifyPassword = scanner.nextLine();

        if (!password.equals(verifyPassword)) {
            System.out.println("Passwords do not match!");
            return;
        }

        String sql = "INSERT INTO users (username, password, admin) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);
        preparedStatement.setBoolean(3, false);
        preparedStatement.executeUpdate();
        System.out.println("New user created successfully!");
    }

    private static void existingUser() throws SQLException {
        System.out.println("Enter username:");
        String username = scanner.nextLine();

        System.out.println("Enter password:");
        String password = scanner.nextLine();

        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);
        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) {
            System.out.println("Logged in successfully!");
            if (rs.getBoolean("admin")) {
                adminMenu();
            } else {
                userMenu();
            }
        } else {
            System.out.println("Invalid username or password!");
        }
    }

    private static void adminLogin() throws SQLException {
        System.out.println("Enter username:");
        String username = scanner.nextLine();

        System.out.println("Enter password:");
        String password = scanner.nextLine();

        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, username);
        preparedStatement.setString(2, password);
        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next() && rs.getBoolean("admin")) {
            System.out.println("Logged in successfully as admin!");
            adminMenu();
        } else {
            System.out.println("Invalid username or password for admin!");
        }
    }

    private static void adminMenu() throws SQLException {
        while (true) {
            System.out.println("\nAdmin Menu:");
            System.out.println("1. Add Product");
            System.out.println("2. View Products");
            System.out.println("3. Delete Product");
            System.out.println("4. Update Product Stock");
            System.out.println("5. Logout");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            switch (choice) {
                case 1:
                    addProduct();
                    break;
                case 2:
                    viewProducts();
                    break;
                case 3:
                    deleteProduct();
                    break;
                case 4:
                    updateProductStock();
                    break;
                case 5:
                    System.out.println("Logged out successfully!");
                    return;
                default:
                    System.out.println("Invalid choice!");
                    break;
            }
        }
    }

    private static void addProduct() throws SQLException {
        System.out.println("Enter product name:");
        String name = scanner.nextLine();

        System.out.println("Enter product price:");
        double price = scanner.nextDouble();

        System.out.println("Enter product stock:");
        int stock = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        String sql = "INSERT INTO products (name, price, stock) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setString(1, name);
        preparedStatement.setDouble(2, price);
        preparedStatement.setInt(3, stock);
        preparedStatement.executeUpdate();
        System.out.println("Product added successfully!");
    }

    private static void viewProducts() throws SQLException {
        String sql = "SELECT * FROM products";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        System.out.println("\nProducts:");
        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            double price = rs.getDouble("price");
            int stock = rs.getInt("stock");
            System.out.println("ID: " + id + ", Name: " + name + ", Price: ₨-" + price + ", Stock: " + stock);
        }
        rs.close();
        stmt.close();
    }

    private static void deleteProduct() throws SQLException {
        viewProducts();
        System.out.println("\nEnter the ID of the product you want to delete:");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        String sql = "DELETE FROM products WHERE id = ?";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        int rowsDeleted = preparedStatement.executeUpdate();
        if (rowsDeleted > 0) {
            System.out.println("Product deleted successfully");
        } else {
            System.out.println("No product found with the given ID");
        }
    }

    private static void updateProductStock() throws SQLException {
        viewProducts();
        System.out.println("\nEnter the ID of the product you want to update:");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        System.out.println("Enter new stock:");
        int stock = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        String sql = "UPDATE products SET stock = ? WHERE id = ?";
        PreparedStatement preparedStatement = conn.prepareStatement(sql);
        preparedStatement.setInt(1, stock);
        preparedStatement.setInt(2, id);
        int rowsUpdated = preparedStatement.executeUpdate();
        if (rowsUpdated > 0) {
            System.out.println("Stock updated successfully");
        } else {
            System.out.println("No product found with the given ID");
        }
    }

    private static void userMenu() throws SQLException {
        while (true) {
            System.out.println("\nUser Menu:");
            System.out.println("1. View Products");
            System.out.println("2. Buy Product");
            System.out.println("3. Logout");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            switch (choice) {
                case 1:
                    viewProducts();
                    break;
                case 2:
                    buyProduct();
                    break;
                case 3:
                    System.out.println("Logged out successfully!");
                    return;
                default:
                    System.out.println("Invalid choice!");
                    break;
            }
        }
    }

    private static void buyProduct() throws SQLException {
        viewProducts();
        System.out.println("\nEnter the ID of the product you want to buy:");
        int id = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        System.out.println("Enter quantity:");
        int quantity = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        String getProductSQL = "SELECT * FROM products WHERE id = ?";
        PreparedStatement preparedStatement = conn.prepareStatement(getProductSQL);
        preparedStatement.setInt(1, id);
        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) {
            int stock = rs.getInt("stock");
            if (stock >= quantity) {
                double price = rs.getDouble("price");
                double total = price * quantity;
                System.out.println("Total amount: ₨-" + total);
                System.out.println("Thank you for your purchase!");
                stock -= quantity;

                String updateStockSQL = "UPDATE products SET stock = ? WHERE id = ?";
                preparedStatement = conn.prepareStatement(updateStockSQL);
                preparedStatement.setInt(1, stock);
                preparedStatement.setInt(2, id);
                preparedStatement.executeUpdate();
            } else {
                System.out.println("Insufficient stock!");
            }
        } else {
            System.out.println("No product found with the given ID");
        }
    }
}



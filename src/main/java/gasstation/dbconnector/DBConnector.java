package gasstation.dbconnector;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class DBConnector {
    private static DBConnector instance;
    private Connection connection;
    private DBCredentials credentials;

    public static DBConnector getInstance(){
        if(instance == null){
            instance = new DBConnector();
        }
        return instance;
    }

    private DBConnector(){
        loadCredentials();
        if(this.credentials != null) {
            initConnection();
        }
    }

    private class DBCredentials{
        private String localhost;
        private String port;
        private String schema;
        private String username;
        private String password;
    }

    private void loadCredentials(){
        StringBuilder credentialsText = new StringBuilder();
        File file = new File("db_settings_mydatabase.json");
        try(Scanner sc = new Scanner(file);){
            while (sc.hasNextLine()){
                credentialsText.append(sc.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        System.out.println(credentialsText.toString());
        DBCredentials dbCredentials = gson.fromJson(credentialsText.toString(), DBCredentials.class);
        if(dbCredentials == null){
            System.out.println("Credentials missing in configuration file");
            return;
        }
        this.credentials = dbCredentials;
    }

    public Connection getConnection() {
        if(this.connection == null){
            this.initConnection();
        }
        return this.connection;
    }

    public void closeConnection(){
        try{
            this.connection.close();
        } catch (SQLException throwables) {
            System.out.println("Closing connection failed - " + throwables.getMessage());
        }

        this.connection = null;
    }

    public void initConnection(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection("jdbc:mysql://" + this.credentials.localhost + ":" +
                            this.credentials.port + "/" + this.credentials.schema,
                    this.credentials.username, this.credentials.password);

        } catch (ClassNotFoundException e) {
            System.out.println("Error finding class. Real reason: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("Cannot make connection. Real reason: " + e.getMessage());
        }
    }
}
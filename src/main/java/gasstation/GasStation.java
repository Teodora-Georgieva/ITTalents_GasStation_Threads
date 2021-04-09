package gasstation;

import gasstation.dbconnector.DBConnector;
import gasstation.people.CarOwner;
import gasstation.people.Cashier;
import gasstation.people.Worker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class GasStation {
    private ArrayList<Column> columns;
    private CashDesk cashDesk1;
    private CashDesk cashDesk2;
    private Worker worker1;
    private Worker worker2;
    private Cashier cashier1;
    private Cashier cashier2;
    private double money;
    private HashSet<Record> records;
    private Connection connection = DBConnector.getInstance().getConnection();

    public GasStation(){
        Car.gasStation = this;
        CarCreator.gasStation = this;
        Cashier.gasStation = this;
        Worker.gasStation = this;

        this.records = new HashSet<>();
        this.columns = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            this.columns.add(new Column());
        }

        this.cashDesk1 = new CashDesk("CashDesk1");
        this.cashDesk2 = new CashDesk("CashDesk2");
        this.worker1 = new Worker("Worker1");
        this.worker2 = new Worker("Worker2");
        this.cashier1 = new Cashier("Cashier1", this.cashDesk1);
        this.cashier2 = new Cashier("Cashier2", this.cashDesk2);
    }

    public void startWork(){
        CarCreator carCreator = new CarCreator();
        carCreator.start();
        this.worker1.start();
        this.worker2.start();
        this.cashier1.start();
        this.cashier2.start();
        Thread inquiryGenerator = new Thread(new Runnable() {
            int numOfFile = 1;
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(150000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    generateInquiries(numOfFile);
                    numOfFile+=4;
                }
            }
        });
        inquiryGenerator.setDaemon(true);
        inquiryGenerator.start();
    }

    public Column getRandomColumnForCar() {
        int colIdx = Randomizer.getRandomNumber(0, this.columns.size() - 1);
        return this.columns.get(colIdx);
    }

    public void receiveCarOwner(CarOwner carOwner){
        Random random = new Random();
        CashDesk cashDesk = random.nextBoolean() ? this.cashDesk1 : this.cashDesk2;
        synchronized (this) {
            cashDesk.offerCarOwner(carOwner);
        }
    }

    public void chargeCars() {
        Column column = null;
        synchronized (this) {
            while (this.isEmpty()) {
                try {
                    System.out.println(Thread.currentThread().getName() + " waits, because gas station is empty");
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            column = this.getColumnForWork();
            column.setBusy();
        }

        Car car = column.getNextCar();
        int liters = Randomizer.getRandomNumber(10, 40);
        car.addFuel(liters);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Car number " + car.getID() + " is fueled");
        CarOwner carOwner = car.getCarOwner();
        carOwner.setChargeTime(LocalDateTime.now());
        carOwner.setColumnID(column.getID());
        carOwner.setLitersFueled(liters);
        this.receiveCarOwner(carOwner);
        while (!carOwner.isAlreadyPaid()){

        }
        //........
        column.removeCar();//todo - maybe after owner pays- this code maybe should not be here???
        column.setFree();//todo - maybe after owner pays - this code maybe should not be here????

        synchronized (this){
            notifyAll(); //???????????????
        }
    }

    private Column getColumnForWork() {
        for(Column column : this.columns){
            if(!column.isEmpty() && !column.isNowCharging()){
                return column;
            }
        }

        return null;
    }

    private boolean isEmpty(){
        for(Column column : this.columns){
            if(!column.isEmpty() && !column.isNowCharging()){
                return false;
            }
        }
        return true;
    }

    public synchronized void receiveCar(Car car) {
        car.offerOnRandomColumn();
        notifyAll();
    }

    public void serveCarOwners(CashDesk cashDesk) {
        synchronized (this) {
            while (cashDesk.isEmpty()) {
                try {
                    System.out.println(Thread.currentThread().getName() + " waits because " + cashDesk.getName() + " is empty");
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        CarOwner carOwner = cashDesk.getNextCarOwner();
        int columnID = carOwner.getColumnID();
        FuelType fuelType = carOwner.getCar().getFuelType();
        int fuelQuantity = carOwner.getLitersFueled();
        LocalDateTime chargeTime = carOwner.getChargeTime();
        System.out.println(Thread.currentThread().getName() + " is serving car owner " + carOwner.getName() + "...");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        synchronized (this) {
            this.records.add(new Record(columnID, fuelType, fuelQuantity, chargeTime));
            this.saveDataToDB(columnID, fuelType, fuelQuantity, chargeTime);
        }
        double fuelPrice = fuelType.getPrice();
        double moneyToPay = fuelQuantity*fuelPrice;
        carOwner.payFuel(moneyToPay);
        synchronized (this) {
            this.money += moneyToPay;
        }
        cashDesk.removeCarOwner();
        System.out.println(carOwner.getName() + " has already paid and exits " + cashDesk.getName());
        carOwner.setAlreadyPaid();
        synchronized (this){
            notifyAll(); //?????????????????
        }
    }

    private void saveDataToDB(int columnID, FuelType fuelType, int fuelQuantity, LocalDateTime chargeTime) {
        String insertIntoDB = "INSERT INTO station_loadings (kolonka_id, fuel_type, fuel_quantity, loading_time) " +
                               "VALUES (?, ?, ?, ?)";
        try(PreparedStatement ps = this.connection.prepareStatement(insertIntoDB);){
            ps.setInt(1, columnID);
            ps.setString(2, fuelType.toString());
            ps.setInt(3, fuelQuantity);
            ps.setTimestamp(4, Timestamp.valueOf(chargeTime));
            ps.executeUpdate();
        } catch (SQLException throwables) {
            System.out.println("Error inserting into DB - " + throwables.getMessage());
        }
    }

    public void generateInquiries(int numOfFile){
        System.out.println("---------- GENERATING INQUIRIES -----------------------");
        this.listAllFuelments(numOfFile);
        this.getCountOfCarsByColumns(numOfFile+1);
        TreeMap<String, Integer> totalQuantityOfFuelByFuelType = this.getTotalFuelQuantityByFuelType(numOfFile+2);
        this.getTotalSumFromFuel(totalQuantityOfFuelByFuelType, numOfFile+3);
        System.out.println("--------------------------------------------------------");

    }

    private void getTotalSumFromFuel(TreeMap<String, Integer> totalQuantityOfFuelByFuelType, int numOfFile) {
        double totalSum = 0;
        for(Map.Entry<String, Integer> entry : totalQuantityOfFuelByFuelType.entrySet()){
            String fuelTypeString = entry.getKey();
            FuelType fuelType = null;
            switch (fuelTypeString){
                case "GAS": fuelType = FuelType.GAS; break;
                case "DIESEL": fuelType = FuelType.DIESEL; break;
                default: fuelType = FuelType.PETROL;break;
            }

            double fuelPrice = fuelType.getPrice();
            int quantity = entry.getValue();
            double crrSum = fuelPrice*quantity;
            totalSum+=crrSum;
        }

        System.out.println("TOTAL SUM FROM FUEL: " + totalSum);
        String localDate = LocalDate.now().toString().replace("-", ".");
        File file = new File("report-" + numOfFile + "-" + localDate + ".txt");
        try(PrintStream ps = new PrintStream(file);){
            System.out.println("TRYING TO CREATE FILE");
            ps.println("Total sum from fuel: " + totalSum);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private TreeMap<String, Integer> getTotalFuelQuantityByFuelType(int numOfFile) {
        TreeMap<String, Integer> totalFuelQuantityByFuelType = new TreeMap<>();
        String sql = "SELECT fuel_type, SUM(fuel_quantity) FROM station_loadings GROUP BY fuel_type";
        try(PreparedStatement ps = this.connection.prepareStatement(sql);){
            ResultSet rows = ps.executeQuery();
            while (rows.next()){
                String fuelType = rows.getString(1);
                int totalLiters = rows.getInt(2);
                totalFuelQuantityByFuelType.put(fuelType, totalLiters);
            }

            System.out.println("SHOWING TOTAL LITERS OF FUEL BY FUEL TYPE:");
            for(Map.Entry<String, Integer> entry : totalFuelQuantityByFuelType.entrySet()){
                System.out.println(entry.getKey() + ": " + entry.getValue() + " литра");
            }
            System.out.println();

            String localDate = LocalDate.now().toString().replace("-", ".");
            File file = new File("report-" + numOfFile + "-" + localDate + ".txt");
            try(PrintStream printStream = new PrintStream(file);){
                System.out.println("TRYING TO CREATE FILE");
                for(Map.Entry<String, Integer> entry : totalFuelQuantityByFuelType.entrySet()){
                    printStream.println(entry.getKey() + ": " + entry.getValue() + " литра");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } catch (SQLException throwables) {
            System.out.println("Error retrieving data from DB - " + throwables.getMessage());
        }

        return totalFuelQuantityByFuelType;
    }

    private void getCountOfCarsByColumns(int numOfFile) {
        TreeMap<Integer, Integer> numOfCarsByColumns = new TreeMap<>();
        String sql = "SELECT kolonka_id, COUNT(*) FROM station_loadings GROUP BY kolonka_id";
        try(PreparedStatement ps = this.connection.prepareStatement(sql);){
            ResultSet rows = ps.executeQuery();
            while (rows.next()){
                int colID = rows.getInt(1);
                int numOfCars = rows.getInt(2);
                numOfCarsByColumns.put(colID, numOfCars);
            }

            System.out.println("SHOWING COUNT OF CARS FUELED ON EACH COLUMN:");
            for(Map.Entry<Integer, Integer> entry : numOfCarsByColumns.entrySet()){
                System.out.println("Колонка " + entry.getKey() + ": " + entry.getValue() + " автомобила");
            }
            System.out.println();

            String localDate = LocalDate.now().toString().replace("-", ".");
            File file = new File("report-" + numOfFile + "-" + localDate + ".txt");
            try(PrintStream printStream = new PrintStream(file);){
                System.out.println("TRYING TO CREATE FILE");
                for(Map.Entry<Integer, Integer> entry : numOfCarsByColumns.entrySet()){
                    printStream.println("Колонка " + entry.getKey() + ": " + entry.getValue() + " автомобила");
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


        } catch (SQLException throwables) {
            System.out.println("Error retrieving data from DB - " + throwables.getMessage());
        }
    }

    private void listAllFuelments(int numOfFile){
        TreeMap<Integer, TreeSet<Record>> treeMap = new TreeMap<>();
        String selectAll = "SELECT * FROM station_loadings";
        try(PreparedStatement ps = this.connection.prepareStatement(selectAll);){
            ResultSet rows = ps.executeQuery();
            while(rows.next()){
                int colNum = rows.getInt("kolonka_id");
                FuelType fuelType = null;
                String fuelTypeString = rows.getString("fuel_type");
                switch (fuelTypeString){
                    case "GAS": fuelType = FuelType.GAS; break;
                    case "DIESEL": fuelType = FuelType.DIESEL; break;
                    default: fuelType = FuelType.PETROL;break;
                }
                int fuelQuantity = rows.getInt("fuel_quantity");
                LocalDateTime loadingTime = rows.getTimestamp("loading_time").toLocalDateTime();

                if(!treeMap.containsKey(colNum)){
                    treeMap.put(colNum, new TreeSet<>());
                }

                TreeSet<Record> records = treeMap.get(colNum);
                Record record = new Record(colNum, fuelType, fuelQuantity, loadingTime);
                records.add(record);
            }

            System.out.println("LISTING ALL FUELMENTS");
            for(Map.Entry<Integer, TreeSet<Record>> entry : treeMap.entrySet()){
                System.out.println("Колонка " + entry.getKey() + ":");
                for(Record record : entry.getValue()){
                    System.out.println("    " + record);
                }
            }
            System.out.println();

            String localDate = LocalDate.now().toString().replace("-", ".");
            File file = new File("report-" + numOfFile + "-" + localDate + ".txt");
            try(PrintStream printStream = new PrintStream(file);){
                System.out.println("TRYING TO CREATE FILE");
                for(Map.Entry<Integer, TreeSet<Record>> entry : treeMap.entrySet()){
                    printStream.println("Колонка " + entry.getKey() + ":");
                    for(Record record : entry.getValue()){
                        printStream.println("    " + record);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } catch (SQLException throwables) {
            System.out.println("Error retrieving data from DB - " + throwables.getMessage());
        }
    }
}
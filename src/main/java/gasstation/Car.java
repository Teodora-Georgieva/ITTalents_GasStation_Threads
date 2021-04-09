package gasstation;

import gasstation.people.CarOwner;

import java.util.Collections;

public class Car {
    private FuelType fuelType;
    private int fuelQuantity;
    public static GasStation gasStation;
    private CarOwner carOwner;
    private int carID;
    private static int id = 1;

    public Car(){
        this.carID = id++;
        int num = Randomizer.getRandomNumber(1, 3);
        switch (num){
            case 1: this.fuelType = FuelType.GAS; break;
            case 2: this.fuelType = FuelType.PETROL; break;
            default: this.fuelType = FuelType.DIESEL; break;
        }

        CarOwner carOwner = new CarOwner();
        this.carOwner = carOwner;
        carOwner.setCar(this);
    }

    public FuelType getFuelType() {
        return this.fuelType;
    }

    public void addFuel(int fuelQuantity){
        System.out.println("Adding " + fuelQuantity + " liters of " + this.fuelType + " to car number " + this.carID + "...");
        this.fuelQuantity+=fuelQuantity;
    }

    public void offerOnRandomColumn(){
        Column column = gasStation.getRandomColumnForCar();
        column.addCar(this);
        System.out.println("Car number " + this.carID + " offered on column " + column.getID());
    }

    public CarOwner getCarOwner() {
        return this.carOwner;
    }

    public int getID() {
        return this.carID;
    }
}
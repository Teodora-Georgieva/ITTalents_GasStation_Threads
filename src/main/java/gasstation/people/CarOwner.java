package gasstation.people;

import gasstation.Car;
import gasstation.FuelType;

import java.time.LocalDateTime;

public class CarOwner {
    private String name;
    private double money;
    private static int id = 1;
    private int carOwnerID;
    private volatile boolean alreadyPaid;
    private Car car;
    private int litersFueled;
    private int columnID;
    private LocalDateTime chargeTime;

    public CarOwner(){
        this.carOwnerID = id++;
        this.name = "CarOwner" + this.carOwnerID;
        this.money = 2000;
        this.alreadyPaid = false;
    }

    public void setCar(Car car) {
        if(this.car == null) {
            this.car = car;
        }
    }

    public String getName() {
        return name;
    }

    public void setChargeTime(LocalDateTime chargeTime) {
        this.chargeTime = chargeTime;
    }

    public void setColumnID(int columnID) {
        this.columnID = columnID;
    }

    public void setLitersFueled(int litersFueled) {
        this.litersFueled = litersFueled;
    }

    public Car getCar() {
        return this.car;
    }

    public int getColumnID() {
        return columnID;
    }

    public int getLitersFueled() {
        return litersFueled;
    }

    public LocalDateTime getChargeTime() {
        return chargeTime;
    }

    public void payFuel(double money){
        this.money -= money;
    }

    public int getCarOwnerID() {
        return this.carOwnerID;
    }

    public boolean isAlreadyPaid() {
        return this.alreadyPaid;
    }

    public void setAlreadyPaid() {
        this.alreadyPaid = true;
    }
}
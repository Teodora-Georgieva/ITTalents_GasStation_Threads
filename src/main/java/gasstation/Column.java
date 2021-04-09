package gasstation;

import java.util.LinkedList;
import java.util.Queue;

public class Column {
    private Queue<Car> cars;
    private volatile boolean isNowCharging;
    private int columnID;
    private static int id = 1;

    public Column(){
        this.columnID = id++;
        this.cars = new LinkedList<>();
    }

    public void addCar(Car car){
        this.cars.offer(car);
    }

    public boolean isEmpty(){
        return this.cars.isEmpty();
    }

    public boolean isNowCharging() {
        return this.isNowCharging;
    }

    public Car getNextCar(){
        return this.cars.peek();
    }

    public void removeCar(){
        System.out.println("Car number " + this.cars.peek().getID() + " exiting gas station, removing from column " + this.columnID);
        this.cars.poll();
    }

    public void setBusy() {
        this.isNowCharging = true;
    }

    public void setFree() {
        this.isNowCharging = false;
    }

    public int getID() {
        return this.columnID;
    }
}
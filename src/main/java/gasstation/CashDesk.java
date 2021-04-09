package gasstation;

import gasstation.people.CarOwner;

import java.util.LinkedList;
import java.util.Queue;

public class CashDesk {
    private Queue<CarOwner> carOwners;
    private String name;

    public CashDesk(String name){
        this.name = name;
        this.carOwners = new LinkedList<>();
    }

    public void offerCarOwner(CarOwner carOwner){
        this.carOwners.offer(carOwner);
    }

    public CarOwner getNextCarOwner(){
        return this.carOwners.peek();
    }

    public void removeCarOwner(){
        this.carOwners.poll();
    }

    public String getName() {
        return this.name;
    }

    public boolean isEmpty() {
        return this.carOwners.isEmpty();
    }
}
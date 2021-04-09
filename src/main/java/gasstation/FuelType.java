package gasstation;

public enum FuelType {
    GAS(1.60), DIESEL(2.40), PETROL(2.0);

    private double price;

    FuelType(double price){
        this.price = price;
    }

    public double getPrice(){
        return this.price;
    }
}
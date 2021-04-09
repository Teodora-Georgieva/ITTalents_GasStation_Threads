package gasstation;

public class CarCreator extends Thread{
    public static GasStation gasStation;
    @Override
    public void run() {
        while (true){
            Car car = new Car();
            gasStation.receiveCar(car);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
package gasstation.people;

import gasstation.CashDesk;
import gasstation.GasStation;

public class Worker extends Thread{
    public static GasStation gasStation;

    public Worker(String name){
        super(name);
    }

    @Override
    public void run() {
        while (true){
            gasStation.chargeCars();
        }
    }
}
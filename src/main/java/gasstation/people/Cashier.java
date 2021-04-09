package gasstation.people;

import gasstation.CashDesk;
import gasstation.GasStation;

public class Cashier extends Thread{
    public static GasStation gasStation;
    private CashDesk cashDesk;

    public Cashier(String name, CashDesk cashDesk){
        super(name);
        this.cashDesk = cashDesk;
    }

    @Override
    public void run() {
        while (true){
            gasStation.serveCarOwners(this.cashDesk);
        }
    }
}
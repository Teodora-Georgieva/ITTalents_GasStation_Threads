package gasstation;

import java.time.LocalDateTime;
import java.util.Objects;

public class Record implements Comparable<Record>{
    private int columnID;
    private FuelType fuelType;
    private int fuelQuantity;
    private LocalDateTime chargeTime;

    public Record(int columnID, FuelType fuelType, int fuelQuantity, LocalDateTime chargeTime) {
        this.columnID = columnID;
        this.fuelType = fuelType;
        this.fuelQuantity = fuelQuantity;
        this.chargeTime = chargeTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Record record = (Record) o;
        return columnID == record.columnID &&
                fuelQuantity == record.fuelQuantity &&
                fuelType == record.fuelType &&
                Objects.equals(chargeTime, record.chargeTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnID, fuelType, fuelQuantity, chargeTime);
    }

    @Override
    public int compareTo(Record o) {
        return this.chargeTime.compareTo(o.chargeTime);
    }

    @Override
    public String toString() {
        return this.fuelType + ", " + fuelQuantity + " литра, " + this.chargeTime;
    }
}
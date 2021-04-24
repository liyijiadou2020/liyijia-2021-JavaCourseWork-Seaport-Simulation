package pojo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class Cargo {
    private CargoType typeCargo;
    private int weight;

    public Cargo() {
    }

    public Cargo(Cargo cargo) {
        typeCargo = cargo.typeCargo;
        weight = cargo.weight;
    }

    public Cargo(CargoType typeOfCargo, int amountOfCargo) {
        if (amountOfCargo <= 0) {
            throw new IllegalArgumentException("Amount of cargo must be more zero!");
        }
        this.typeCargo = typeOfCargo;
        this.weight = amountOfCargo;
    }

    public CargoType getTypeCargo() {
        return typeCargo;
    }

    public void setTypeCargo(CargoType typeCargo) {
        this.typeCargo = typeCargo;
    }

    public Integer getWeight() {
        return weight;
    }

    public void reduceAmountOfCargo(int countCargo) {
        weight -= countCargo;
        if (weight < 0) {
            weight = 0;
        }
    }

    public void setWeight(Integer weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight of cargo must not be 0!");
        }
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Cargo{" +
                "typeOfCargo=" + typeCargo +
                ", amountOfCargo=" + weight +
                '}';
    }
}

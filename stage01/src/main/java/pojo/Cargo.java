package pojo;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class Cargo {
    private CargoType cargoType;
    private int weight;

    public Cargo() {
    }

    public Cargo(Cargo cargo) {
        cargoType = cargo.cargoType;
        weight = cargo.weight;
    }

    public Cargo(CargoType typeOfCargo, int amountOfCargo) {
        if (amountOfCargo <= 0) {
            throw new IllegalArgumentException("Amount of cargo must be more zero!");
        }
        this.cargoType = typeOfCargo;
        this.weight = amountOfCargo;
    }

    public CargoType getCargoType() {
        return cargoType;
    }

    public void setCargoType(CargoType cargoType) {
        this.cargoType = cargoType;
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
                "typeOfCargo=" + cargoType +
                ", amountOfCargo=" + weight +
                '}';
    }
}

package car.example.autowire;

public class Car {

    private Specification specification;

    public void displayDetails() {
        System.out.println("Car Details: " + specification);
    }

    public Car(Specification specification) {
        this.specification = specification;
    }
}

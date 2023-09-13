package entity;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Customer {
    private String id;
    private String name;
    private String address;
    private double salary;

    public void setSalary(double salary) {
        if (salary>=0){
            this.salary = salary;
        }
    }
}

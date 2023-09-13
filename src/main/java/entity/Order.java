package entity;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@ToString
public class Order {
    private String id;
    private String date;
    private String customerId;
}

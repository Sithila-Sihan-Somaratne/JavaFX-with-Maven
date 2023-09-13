package dto.tm;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class OrderDetailsTm extends RecursiveTreeObject<OrderDetailsTm> {
    private String itemCode;
    private String desc;
    private int qty;
    private double Amount;
}

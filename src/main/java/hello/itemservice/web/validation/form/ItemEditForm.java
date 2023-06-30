package hello.itemservice.web.validation.form;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ItemEditForm {
    //  수정시에는 아이디가 필수
    @NotNull
    private Long id;

    @NotBlank
    private String itemName;

    @NotNull
    @Range(min=1000,max=10000000)
    private Integer price;

    //  수정시에는 수량 무제한
    @NotNull
    private Integer quantity;

}

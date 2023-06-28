package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/validation/v1/items")
@RequiredArgsConstructor
public class ValidationItemControllerV1 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v1/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v1/item";
    }


    //  [중요] new Item()이라는 빈 객체를 넘겨준 이유는 검증에 실패 했을 때 ,
    //  기존에 입력했던 값들이 다시 재사용 가능하도록 하기 위함.
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v1/addForm";
    }

    @PostMapping("/add")
    public String addItem(@ModelAttribute Item item, RedirectAttributes redirectAttributes,Model model) {
        //검증 오류 결과 보관
        Map<String, String> errors = new HashMap<>();
        if(!StringUtils.hasText(item.getItemName())){
            errors.put("itemName","상품 이름은 필수 입니다.");
        }
        if(item.getPrice()==null || item.getPrice()<1000 || item.getPrice()>1000000){
            errors.put("price","가격은 1,000원에서 1,000,000까지 허용 합니다.");
        }
        if(item.getQuantity()>9999 || item.getQuantity()==null){
            errors.put("quantity","수량은 최대 9999까지 가능합니다.");
        }
        //  특정 필드가 아닌 복합 룰
        if(item.getPrice()!=null || item.getQuantity()!=null){
            int resultPrice = item.getPrice()*item.getQuantity();
            if(resultPrice<10000){
                errors.put("globalError","가격 * 수량의 결과는 10,000원 이상이어야 합니다.");
            }
        }
        //  검증에 실패하면  다시 입력 폼으로
        if(!errors.isEmpty()){
            model.addAttribute("errors",errors);
            return "validation/v1/addForm";
        }
        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v1/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v1/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v1/items/{itemId}";
    }

}


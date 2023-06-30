package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.domain.item.SaveCheck;
import hello.itemservice.domain.item.UpdateCheck;
import hello.itemservice.web.validation.form.ItemEditForm;
import hello.itemservice.web.validation.form.ItemSaveForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/validation/v4/items")
@RequiredArgsConstructor
public class ValidationItemControllerV4 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v4/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/item";
    }

    //  [중요] new Item()이라는 빈 객체를 넘겨준 이유는 검증에 실패 했을 때 ,
    //  기존에 입력했던 값들이 다시 재사용 가능하도록 하기 위함.
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v4/addForm";
    }

    //  ModelAttribute("") 처럼 비어있을 경우 객체 첫자가 소문자인 이름으로 모델값이 들어가게 된다.
    //  뷰템플릿 변경을 하기 싫으면 그래서 설정해줘야함.
    @PostMapping("/add")
    public String addItem (@Validated @ModelAttribute("item") ItemSaveForm form,
                           BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (form.getPrice() != null && form.getQuantity() != null) {
            int resultPrice = form.getPrice() * form.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }
        
        //  검증에 실패하면  다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors= {} ", bindingResult);
            return "validation/v4/addForm";
        }

        // 성공 로직
        Item item = new Item();
        item.setItemName(form.getItemName());
        item.setPrice(form.getPrice());
        item.setQuantity(form.getQuantity());

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
    }

        @GetMapping("/{itemId}/edit")
        public String editForm (@PathVariable Long itemId, Model model){
            Item item = itemRepository.findById(itemId);
            model.addAttribute("item", item);
            return "validation/v4/editForm";
        }

    @PostMapping("/{itemId}/edit")
    public String edit (@PathVariable Long itemId, @Validated @ModelAttribute("item") ItemEditForm form, BindingResult bindingResult){

        //  특정 필드가 아닌 복합 룰
        if (form.getPrice() != null && form.getQuantity() != null) {
            int resultPrice = form.getPrice() * form.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }
        if (bindingResult.hasErrors()) {
            log.info("errors= {} ", bindingResult);
            return "validation/v4/editForm";
        }
        Item itemParam = new Item();
        itemParam.setItemName(form.getItemName());
        itemParam.setPrice(form.getPrice());
        itemParam.setQuantity(form.getQuantity());

        itemRepository.update(itemId, itemParam);


        return "redirect:/validation/v4/items/{itemId}";
    }

    }



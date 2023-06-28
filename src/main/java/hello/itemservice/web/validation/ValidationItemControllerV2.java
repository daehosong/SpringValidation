package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;

    @InitBinder
    public void init(WebDataBinder dataBinder){
        dataBinder.addValidators(itemValidator);
    }

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    //  [중요] new Item()이라는 빈 객체를 넘겨준 이유는 검증에 실패 했을 때 ,
    //  기존에 입력했던 값들이 다시 재사용 가능하도록 하기 위함.
    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }

    //BindingResult는 무조건 @ModelAttribute 다음에 와야함 -> 검증해야될 객체인 'target' 바로 다음에 온다.
    //@PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item,
                            BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        //검증 오류 결과 보관
        Map<String, String> errors = new HashMap<>();
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수 입니다."));
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price", "가격은 1,000이상 1,000,000미만 입니다."));
        }
        if (item.getQuantity() > 10000 || item.getQuantity() == null) {
            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9999까지 가능합니다."));
        }
        //  특정 필드가 아닌 복합 룰
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item", "가격 * 수량의 결과는 10,000원 이상이어야 합니다."));
            }
        }
        //  검증에 실패하면  다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors= {} ", bindingResult);
            return "validation/v2/addForm";
        }

        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //  @PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item,
                            BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        //검증 오류 결과 보관
        Map<String, String> errors = new HashMap<>();
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName",
                    item.getItemName(), false, null, null, "상품 이름은 필수입니다."));
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price",
                    item.getItemName(), false, null, null, "상품 가격은 필수입니다."));
        }

        if (item.getQuantity() > 10000 || item.getQuantity() == null) {
            bindingResult.addError(new FieldError("item", "quantity",
                    item.getItemName(), false, null, null, "상품 수량은 필수입니다."));
        }

        //  특정 필드가 아닌 복합 룰
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.addError(new ObjectError("item", null, null, "가격 * 수량의 결과는 10,000원 이상이어야 합니다."));
            }
            //  검증에 실패하면  다시 입력 폼으로
            if (bindingResult.hasErrors()) {
                log.info("errors= {} ", bindingResult);
                return "validation/v2/addForm";
            }
        }
        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";

    }

//        @PostMapping("/add")
        public String addItemV3 (@ModelAttribute Item item,
                BindingResult bindingResult, RedirectAttributes redirectAttributes) {
            //검증 오류 결과 보관

            if (!StringUtils.hasText(item.getItemName())) {
                bindingResult.addError(new FieldError("item", "itemName",
                        item.getItemName(), false, new String[]{"required.item.itemName"}, null, null));
            }

            if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
                bindingResult.addError(new FieldError("item", "price",
                        item.getPrice(), false, new String[]{"range.item.price"}, new Object[]{1000,1000000}, null));
            }

            if (item.getQuantity() > 10000 || item.getQuantity() == null) {
                bindingResult.addError(new FieldError("item", "quantity",
                        item.getQuantity(), false, new String[]{"max.item.quantity"}, new Object[]{9999}, null));
            }

            //  특정 필드가 아닌 복합 룰
            if (item.getPrice() != null && item.getQuantity() != null) {
                int resultPrice = item.getPrice() * item.getQuantity();
                if (resultPrice < 10000) {
                    bindingResult.addError(new ObjectError("item", new String[]{"totalPriceMin"}, new Object[]{10000,resultPrice}, null));
                }
                //  검증에 실패하면  다시 입력 폼으로
                if (bindingResult.hasErrors()) {
                    log.info("errors= {} ", bindingResult);
                    return "validation/v2/addForm";
                }
            }
            // 성공 로직
            Item savedItem = itemRepository.save(item);
            redirectAttributes.addAttribute("itemId", savedItem.getId());
            redirectAttributes.addAttribute("status", true);
            return "redirect:/validation/v2/items/{itemId}";

        }

//    @PostMapping("/add")
    public String addItemV4 (@ModelAttribute Item item,
                             BindingResult bindingResult, RedirectAttributes redirectAttributes,Model model) {
        //검증 오류 결과 보관

        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.rejectValue("itemName","required");
        }

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.rejectValue("price","range",new Object[]{1000,1000000},null);
        }

        if (item.getQuantity() > 10000 || item.getQuantity() == null) {
            bindingResult.rejectValue("quantity","max",new Object[]{9999},null);
        }

        //  특정 필드가 아닌 복합 룰
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000,resultPrice},null);
            }
            //  검증에 실패하면  다시 입력 폼으로
            if (bindingResult.hasErrors()) {
                log.info("errors= {} ", bindingResult);
                return "validation/v2/addForm";
            }
        }
        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV5 (@ModelAttribute Item item,
                             BindingResult bindingResult, RedirectAttributes redirectAttributes,Model model) {

        itemValidator.validate(item,bindingResult);

            //  검증에 실패하면  다시 입력 폼으로
            if (bindingResult.hasErrors()) {
                log.info("errors= {} ", bindingResult);
                return "validation/v2/addForm";
            }
        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }


    @PostMapping("/add")
    public String addItemV6 (@Validated @ModelAttribute Item item,
                             BindingResult bindingResult, RedirectAttributes redirectAttributes,Model model) {

        //  검증에 실패하면  다시 입력 폼으로
        if (bindingResult.hasErrors()) {
            log.info("errors= {} ", bindingResult);
            return "validation/v2/addForm";
        }
        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }
        @GetMapping("/{itemId}/edit")
        public String editForm (@PathVariable Long itemId, Model model){
            Item item = itemRepository.findById(itemId);
            model.addAttribute("item", item);
            return "validation/v2/editForm";
        }

        @PostMapping("/{itemId}/edit")
        public String edit (@PathVariable Long itemId, @ModelAttribute Item item){
            itemRepository.update(itemId, item);
            return "redirect:/validation/v2/items/{itemId}";
        }
    }



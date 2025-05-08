package jp.co.metateam.library.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.service.BookMstService;
import lombok.extern.log4j.Log4j2;

/**
 * 書籍関連クラス
 */
@Log4j2
@Controller
public class BookController {
    
    private final BookMstService bookMstService;

    @Autowired
    public BookController(BookMstService bookMstService){
        this.bookMstService = bookMstService;
    }

    @GetMapping("/book/index")
    public String index(Model model) {
        // 書籍を全件取得
        List<BookMstDto> bookMstList = this.bookMstService.findAvailableWithStockCount();
        
        model.addAttribute("bookMstList", bookMstList);

        return "book/index";
    }

    @GetMapping("/book/add")
    public String add(Model model) {
        if (!model.containsAttribute("bookMstDto")) {
            model.addAttribute("bookMstDto", new BookMstDto());
        }

        return "book/add"; //書籍登録画面
    }
    
    //新しく
 @PostMapping("/book/add")
    public String register(@Valid @ModelAttribute BookMstDto bookMstDto, BindingResult result, RedirectAttributes ra, Model model) {

            try {
            boolean errTitleFlg = false;
            boolean errIsbnFlg = false;
            String title = bookMstDto.getTitle();
            String isbn = bookMstDto.getIsbn();

                //書籍名が空値・nullかチェック
            if(title == null || title.trim().isEmpty()){
                result.rejectValue("title", "error.value", "書籍名は必須です");
                errTitleFlg = true; //エラーだったらtrue
            }
                //書籍名255文字以内かチェック
            if(title.length() > 255) {
                result.rejectValue("title", "error.length", "書籍名は255文字以内で入力してください");
                errTitleFlg = true;
            }
            
                // ISBNがnullかチェック
            if(isbn == null || isbn.trim().isEmpty()){
                result.rejectValue("isbn", "error.value", "ISBNは必須です");
                errIsbnFlg = true; 
            }

                // ISBNが半角数字のみで構成されているかをチェック            
            else if (!isbn.matches("\\d+")) {
                    result.rejectValue("isbn", "error.numeric", "ISBNは半角数字で入力してください");
                    errIsbnFlg = true;
            }
            
                // ISBNが13桁であるかをチェック           
            else if (isbn.length() != 13) {
                    result.rejectValue("isbn", "error.length", "ISBNは13桁で入力してください");
                    errIsbnFlg = true;
            }      

            if (bookMstService.selectByIsbn(bookMstDto.getIsbn()) != null) {
                result.rejectValue("isbn", "error.value", "登録済みのISBNです");
 
            }


                //何か一つでもエラーだとエラー扱いにする
            if (errTitleFlg || errIsbnFlg) {
                return "book/add" ;
            }

            bookMstService.save(bookMstDto);

            return "redirect:/book/index";//書籍一覧画面

        } catch (Exception e) {
            log.error(e.getMessage());
            ra.addFlashAttribute("bookMstDto", bookMstDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.bookMstDto", result);

            return "redirect:/book/add";
        }
    }
}
            

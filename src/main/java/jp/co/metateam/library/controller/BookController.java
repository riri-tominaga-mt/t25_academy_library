package jp.co.metateam.library.controller;

import java.time.LocalDateTime;
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
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.service.BookMstService;
import lombok.extern.log4j.Log4j2;

import java.sql.Timestamp;

@Controller
@Log4j2
public class BookController {

    private final BookMstService bookMstService;

    @Autowired
    public BookController(BookMstService bookMstService) {
        this.bookMstService = bookMstService;
    }

    // バリデーション
    private void validateBookDto(BookMstDto bookDto, BindingResult result) {
        String title = bookDto.getTitle();
        String isbn = bookDto.getIsbn();

        if (title == null || title.trim().isEmpty()) {
            result.rejectValue("title", "error.value", "書籍名は必須です");
        } else if (title.length() > 255) {
            result.rejectValue("title", "error.value", "書籍名は255文字以内で入力してください");
        }

        if (isbn == null || isbn.trim().isEmpty()) {
            result.rejectValue("isbn", "error.value", "ISBNは必須です");
        } else {
            if (!isbn.matches("\\d+")) {
                result.rejectValue("isbn", "error.numeric", "ISBNは半角数字で入力してください");
            }
            if (isbn.length() != 13) {
                result.rejectValue("isbn", "error.length", "ISBNは13桁で入力してください");
            }
        }
    }

    // 書籍一覧
    @GetMapping("/book/index")
    public String index(Model model) {
        List<BookMstDto> bookMstList = this.bookMstService.findAvailableWithStockCount();
        model.addAttribute("bookMstList", bookMstList);
        model.addAttribute("title", "書籍一覧");
        return "book/index";
    }

    // 書籍追加画面
    @GetMapping("/book/add")
    public String add(Model model) {
        if (!model.containsAttribute("bookMstDto")) {
            model.addAttribute("bookMstDto", new BookMstDto());
        }
        return "book/add";
    }

    // 書籍登録
    @PostMapping("/book/add")
    public String register(@Valid @ModelAttribute BookMstDto bookMstDto, BindingResult result,
                           RedirectAttributes ra, Model model) {

        validateBookDto(bookMstDto, result);

        if (result.hasErrors()) {
            return "book/add";
        }

        try {
            bookMstService.save(bookMstDto);
            return "redirect:/book/index";
        } catch (Exception e) {
            log.error(e.getMessage());
            ra.addFlashAttribute("bookMstDto", bookMstDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.bookMstDto", result);
            return "redirect:/book/add";
        }
    }

    // 書籍編集画面
    @GetMapping("/book/edit/{id}")
    public String editBook(@PathVariable("id") Long id, Model model) {
        BookMst book = bookMstService.selectById(id);
        if (book == null) {
            model.addAttribute("errorMessage", "指定された書籍が存在しません。");
            return "book/edit";
        }
        model.addAttribute("book", book);
        return "book/edit";
    }

    // 書籍変更・削除
    @PostMapping("/book/edit")
    public String updateBook(@Valid @ModelAttribute("book") BookMstDto bookDto,
                             BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        try {
        //取得できなかった（= null） か、削除済み（deletedFlg == 1）の場合は、エラーメッセージを表示用に渡し、編集画面にリダイレクト
            BookMst original = bookMstService.selectById(bookDto.getId());
            if (original == null || original.getDeletedFlg() == 1) {
                redirectAttributes.addFlashAttribute("errorMessage", "この書籍は削除されています。");
                return "redirect:/book/edit/" + bookDto.getId();
            }

            if (original.getTitle().equals(bookDto.getTitle()) &&
                original.getIsbn().equals(bookDto.getIsbn())) {
                model.addAttribute("errorMessage", "変更点はありません。");
                model.addAttribute("book", bookDto);
                return "book/edit";
            }

            // バリデーションチェック
            validateBookDto(bookDto, result);

            // ISBN重複チェック（自分以外のIDに同じISBNがないか）
            BookMst existing = bookMstService.selectByIsbn(bookDto.getIsbn());
            if (existing != null && !existing.getId().equals(bookDto.getId())) {
                result.rejectValue("isbn", "error.value", "登録済みのISBNです");
            }

            if (result.hasErrors()) {
                model.addAttribute("book", bookDto);
                return "book/edit";
            }

            BookMst entity = new BookMst();
            entity.setId(bookDto.getId());
            entity.setTitle(bookDto.getTitle());
            entity.setIsbn(bookDto.getIsbn());

            if (!bookMstService.update(entity)) {
                model.addAttribute("errorMessage", "更新に失敗しました。");
                model.addAttribute("book", bookDto);
                return "book/edit";
            }

            redirectAttributes.addFlashAttribute("successMessage", "書籍が変更されました");
            return "redirect:/book/index";

        } catch (Exception e) {
            log.error("更新処理中にエラーが発生しました", e);
            model.addAttribute("errorMessage", "予期しないエラーが発生しました");
            model.addAttribute("book", bookDto);
            return "book/edit";
        }
    }

    // 書籍削除
    @PostMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id, RedirectAttributes redirectAttributes, Model model) {
        BookMst book = bookMstService.selectById(id);

        if (book == null || book.getDeletedFlg() == 1) {
            redirectAttributes.addFlashAttribute("errorMessage", "この書籍はすでに削除されています。");
            return "redirect:/book/index";
        }

        book.setDeletedFlg(true);
        book.setDeletedAt(Timestamp.valueOf(LocalDateTime.now()));
        bookMstService.update(book);

        redirectAttributes.addFlashAttribute("successMessage", "書籍を削除しました。");
        return "redirect:/book/index";
    }
}

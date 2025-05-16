package jp.co.metateam.library.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.repository.BookMstRepository;

@Service
public class BookMstService {

    private final BookMstRepository bookMstRepository;

    @Autowired
    public BookMstService(BookMstRepository bookMstRepository) {
        this.bookMstRepository = bookMstRepository;
    }

    public BookMst selectByIsbn(String isbn) {
        return this.bookMstRepository.findByIsbn(isbn).orElse(null);
    }

    public List<BookMstDto> findAvailableWithStockCount() {
        List<BookMst> books = this.bookMstRepository.findLimitedBook();
        List<BookMstDto> bookMstDtoList = new ArrayList<>();

        for (BookMst book : books) {
            BookMstDto dto = new BookMstDto();
            dto.setId(book.getId());
            dto.setIsbn(book.getIsbn());
            dto.setTitle(book.getTitle());
            bookMstDtoList.add(dto);
        }

        return bookMstDtoList;
    }

    @Transactional
    public void save(BookMstDto bookMstDto) {
        BookMst bookMst = new BookMst();
        bookMst.setTitle(bookMstDto.getTitle());
        bookMst.setIsbn(bookMstDto.getIsbn());
        this.bookMstRepository.save(bookMst);
    }

    public BookMst selectById(Long id) {
        return bookMstRepository.findById(id).orElse(null);
    }

    @Transactional
    public boolean update(BookMst bookMst) {
        BookMst existingBook = bookMstRepository.findById(bookMst.getId()).orElse(null);

        if (existingBook != null) {
            existingBook.setTitle(bookMst.getTitle());
            existingBook.setIsbn(bookMst.getIsbn());
            bookMstRepository.save(existingBook);
            return true;
        }

        return false;
    }

    /** ✅ 削除処理（論理削除） */
    @Transactional
public boolean logicalDelete(Long id) {
    BookMst book = bookMstRepository.findById(id).orElse(null);
    if (book != null && book.getDeletedFlg() == 0) {
        book.setDeletedFlg(true); // ← ここを true に直す！
        book.setDeletedAt(Timestamp.valueOf(LocalDateTime.now()));
        bookMstRepository.save(book);
        return true;
    }
    return false;
}

   

}

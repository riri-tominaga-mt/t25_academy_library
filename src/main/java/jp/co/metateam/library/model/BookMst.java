package jp.co.metateam.library.model;

import java.sql.Timestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 書籍マスタ
 */
@Entity
@Table(name = "book_mst")
public class BookMst {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String isbn;

    @Column(name = "deleted_flg")
    private boolean deletedFlg;

    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    // --- getter/setter ---
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIsbn() {
        return this.isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public boolean isDeletedFlg() {
    return deletedFlg;
    }

    public void setDeletedFlg(boolean deletedFlg) {
    this.deletedFlg = deletedFlg;
    }


    public Timestamp getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Timestamp deletedAt) {
        this.deletedAt = deletedAt;
    }

    // 編集画面データ用の内部クラス（分けるのが理想）
    public class BookMstDto {
        private Long id;
        private String title;
        private String isbn;

        public BookMstDto() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getIsbn() { return isbn; }
        public void setIsbn(String isbn) { this.isbn = isbn; }
    }
}

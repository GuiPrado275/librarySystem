package com.guilherme.librarySystem.services;

import com.guilherme.librarySystem.models.Book;
import com.guilherme.librarySystem.models.dto.BookCreateDTO;
import com.guilherme.librarySystem.models.dto.BookLoanCountDTO;
import com.guilherme.librarySystem.models.dto.BookUpdateDTO;
import com.guilherme.librarySystem.models.enums.ProfileEnum;
import com.guilherme.librarySystem.repositories.BookRepository;
import com.guilherme.librarySystem.repositories.LoanRepository;
import com.guilherme.librarySystem.security.UserSpringSecurity;
import com.guilherme.librarySystem.services.exceptions.AuthorizationException;
import com.guilherme.librarySystem.services.exceptions.BusinessRuleException;
import com.guilherme.librarySystem.services.exceptions.DataBindingViolationException;
import com.guilherme.librarySystem.services.exceptions.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private LoanRepository loanRepository;

    public Book findById(Long id) {
        Optional<Book> book = this.bookRepository.findById(id);
        return book.orElseThrow(() -> new ObjectNotFoundException(
                "Livro não encontrado! Id: " + id + ", Tipo: " + Book.class.getName()));
    }

    public List<Book> findAllActive() {
        return this.bookRepository.findByIsActiveTrue();
    }

    public List<Book> findByTitle(String title) {
        return this.bookRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Book> findByAuthor(String author) {
        return this.bookRepository.findByAuthorContainingIgnoreCase(author);
    }

    @Transactional
    public Book create(Book book) {
        checkStaff();

        book.setId(null);
        book.setAvailableCopies(book.getTotalCopies()); //when registering, all copies start out available
        book.setRegistrationDate(LocalDateTime.now());
        book.setActive(true);
        return this.bookRepository.save(book);
    }

    @Transactional
    public Book update(Long id, Book updatedBook) {
        checkStaff();

        Book existingBook = findById(id);

        int borrowedCopies = existingBook.getTotalCopies() - existingBook.getAvailableCopies();
        if (updatedBook.getTotalCopies() < borrowedCopies) {
            throw new BusinessRuleException(
                    "Não é possível reduzir o total de exemplares para um valor menor que a quantidade " +
                            "atualmente emprestada (" + borrowedCopies + ").");
        }

        //adjust available copies proportionally to the change in total copies
        int copiesDifference = updatedBook.getTotalCopies() - existingBook.getTotalCopies();

        existingBook.setTitle(updatedBook.getTitle());
        existingBook.setAuthor(updatedBook.getAuthor());
        existingBook.setCategory(updatedBook.getCategory());
        existingBook.setPublicationYear(updatedBook.getPublicationYear());
        existingBook.setTotalCopies(updatedBook.getTotalCopies());
        existingBook.setAvailableCopies(existingBook.getAvailableCopies() + copiesDifference);

        return this.bookRepository.save(existingBook);
    }

    @Transactional
    public void delete(Long id) {
        checkStaff();

        Book book = findById(id);

        if (!book.getAvailableCopies().equals(book.getTotalCopies())) {
            throw new BusinessRuleException(
                    "Não é possível remover um livro que possui exemplares emprestados no momento.");
        }

        try {
            book.setActive(false); //soft delete: preserves the history of loans already made
            this.bookRepository.save(book);
        } catch (Exception e) {
            throw new DataBindingViolationException("O livro não pode ser removido.");
        }
    }

    @Transactional
    void decreaseAvailableCopy(Book book) {
        if (book.getAvailableCopies() <= 0) {
            throw new BusinessRuleException(
                    "Não há exemplares disponíveis para o livro '" + book.getTitle() + "'.");
        }
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        this.bookRepository.save(book);
    }

    @Transactional
    void increaseAvailableCopy(Book book) {
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            this.bookRepository.save(book);
        }
    }

    //Report: ADMIN and LIBRARIAN only
    public List<BookLoanCountDTO> findMostBorrowedBooks(int topN) {
        checkStaff();
        return this.loanRepository.findMostBorrowedBooks(PageRequest.of(0, topN));
    }

    public Book fromDTO(BookCreateDTO dto) {
        Book book = new Book();
        book.setTitle(dto.title());
        book.setAuthor(dto.author());
        book.setCategory(dto.category());
        book.setPublicationYear(dto.publicationYear());
        book.setTotalCopies(dto.totalCopies());
        return book;
    }

    public Book fromDTO(BookUpdateDTO dto) {
        Book book = new Book();
        book.setTitle(dto.title());
        book.setAuthor(dto.author());
        book.setCategory(dto.category());
        book.setPublicationYear(dto.publicationYear());
        book.setTotalCopies(dto.totalCopies());
        return book;
    }

    // to check if the user is a librarian or an admin
    private void checkStaff() {
        UserSpringSecurity userSpringSecurity = UserService.autheticated();
        if (Objects.isNull(userSpringSecurity)
                || !userSpringSecurity.hasRole(ProfileEnum.ADMIN) && !userSpringSecurity.hasRole(ProfileEnum.LIBRARIAN)) {
            throw new AuthorizationException("Acesso negado! Apenas bibliotecários e administradores podem realizar esta ação.");
        }
    }
}
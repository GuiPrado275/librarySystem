package com.guilherme.librarySystem.services;

import com.guilherme.librarySystem.models.Book;
import com.guilherme.librarySystem.models.Loan;
import com.guilherme.librarySystem.models.Reservation;
import com.guilherme.librarySystem.models.User;
import com.guilherme.librarySystem.models.dto.LoanCreateDTO;
import com.guilherme.librarySystem.models.enums.ProfileEnum;
import com.guilherme.librarySystem.repositories.BookRepository;
import com.guilherme.librarySystem.repositories.LoanRepository;
import com.guilherme.librarySystem.repositories.ReservationRepository;
import com.guilherme.librarySystem.repositories.UserRepository;
import com.guilherme.librarySystem.security.UserSpringSecurity;
import com.guilherme.librarySystem.services.exceptions.AuthorizationException;
import com.guilherme.librarySystem.services.exceptions.BusinessRuleException;
import com.guilherme.librarySystem.services.exceptions.ObjectNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
public class LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);

    public static final int MAX_ACTIVE_LOANS_PER_USER = 5;
    public static final int LOAN_PERIOD_DAYS = 14;
    public static final BigDecimal FINE_PER_DAY = new BigDecimal("2.00");

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private BookService bookService;

    public Loan findById(Long id) {
        Loan loan = this.loanRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Empréstimo não encontrado! Id: " + id + ", Tipo: " + Loan.class.getName()));
        checkOwnerOrStaff(loan.getUser().getId());
        return loan;
    }

    public List<Loan> findByUser(Long userId) {
        checkOwnerOrStaff(userId);
        return this.loanRepository.findByUserId(userId);
    }

    public List<Loan> findActiveByUser(Long userId) {
        checkOwnerOrStaff(userId);
        return this.loanRepository.findByUserIdAndIsReturnedFalse(userId);
    }

    //Administrative query: LIBRARIAN/ADMIN only
    public List<Loan> findAllActive() {
        checkStaff();
        return this.loanRepository.findByIsReturnedFalse();
    }

    @Transactional
    public Loan create(LoanCreateDTO dto) {
        User user = this.userRepository.findById(dto.userId())
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Usuário não encontrado! Id: " + dto.userId() + ", Tipo: " + User.class.getName()));

        Book book = this.bookRepository.findById(dto.bookId())
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Livro não encontrado! Id: " + dto.bookId() + ", Tipo: " + Book.class.getName()));

        return create(user, book);
    }

    @Transactional
    public Loan create(User user, Book book) {
        checkOwnerOrStaff(user.getId());

        if (!book.isActive()) {
            throw new BusinessRuleException("Este livro não está mais disponível no acervo.");
        }

        if (book.getAvailableCopies() <= 0) {
            throw new BusinessRuleException(
                    "Não há exemplares disponíveis de '" + book.getTitle() + "'. " +
                            "Faça uma reserva para ser avisado quando o livro voltar a estar disponível.");
        }

        if (hasOverdueLoans(user.getId())) {
            throw new BusinessRuleException(
                    "Usuário possui empréstimo(s) em atraso. Regularize a devolução antes de pegar outro livro.");
        }

        long activeLoans = this.loanRepository.countByUserIdAndIsReturnedFalse(user.getId());
        if (activeLoans >= MAX_ACTIVE_LOANS_PER_USER) {
            throw new BusinessRuleException(
                    "Usuário atingiu o limite máximo de " + MAX_ACTIVE_LOANS_PER_USER + " empréstimos simultâneos.");
        }

        if (this.loanRepository.existsByUserIdAndBookIdAndIsReturnedFalse(user.getId(), book.getId())) {
            throw new BusinessRuleException("Usuário já possui um empréstimo em aberto para este livro.");
        }

        this.bookService.decreaseAvailableCopy(book);

        Loan loan = new Loan();
        loan.setUser(user);
        loan.setBook(book);
        loan.setLoanDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(LOAN_PERIOD_DAYS));
        loan.setFine(BigDecimal.ZERO);
        loan.setReturned(false);
        loan = this.loanRepository.save(loan);

        //if the user was in the waiting queue for this book, their reservation is consumed
        this.reservationRepository.findByUserIdAndBookIdAndIsActiveTrue(user.getId(), book.getId())
                .ifPresent(reservation -> {
                    reservation.setActive(false);
                    this.reservationRepository.save(reservation);
                });

        return loan;
    }

    //only LIBRARIAN/ADMIN
    @Transactional
    public Loan returnLoan(Long loanId) {
        checkStaff();

        Loan loan = findById(loanId);

        if (loan.isReturned()) {
            throw new BusinessRuleException("Este empréstimo já foi devolvido.");
        }

        loan.setReturnDate(LocalDate.now());
        loan.setFine(calculateFine(loan));
        loan.setReturned(true);
        loan = this.loanRepository.save(loan);

        this.bookService.increaseAvailableCopy(loan.getBook());
        notifyNextInQueue(loan.getBook());

        return loan;
    }

    //Calculates the late payment penalty
    private BigDecimal calculateFine(Loan loan) {
        long daysLate = ChronoUnit.DAYS.between(loan.getDueDate(), loan.getReturnDate());
        if (daysLate <= 0) {
            return BigDecimal.ZERO;
        }
        return FINE_PER_DAY.multiply(BigDecimal.valueOf(daysLate));
    }

    //Check if there is an overdue loan
    private boolean hasOverdueLoans(Long userId) {
        LocalDate today = LocalDate.now();
        return this.loanRepository.findByUserIdAndIsReturnedFalse(userId).stream()
                .anyMatch(loan -> loan.getDueDate().isBefore(today));
    }

    // Reservation / waiting queue
    // USER can only reserve for themselves, LIBRARIAN/ADMIN can reserve for any user.
    @Transactional
    public Reservation reserve(LoanCreateDTO dto) {
        return reserve(dto.userId(), dto.bookId());
    }

    @Transactional
    public Reservation reserve(Long userId, Long bookId) {
        checkOwnerOrStaff(userId);

        User user = this.userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Usuário não encontrado! Id: " + userId + ", Tipo: " + User.class.getName()));

        Book book = this.bookRepository.findById(bookId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Livro não encontrado! Id: " + bookId + ", Tipo: " + Book.class.getName()));

        if (book.getAvailableCopies() > 0) {
            throw new BusinessRuleException(
                    "Há exemplares disponíveis de '" + book.getTitle() + "'. Realize o empréstimo diretamente.");
        }

        if (this.reservationRepository.existsByUserIdAndBookIdAndIsActiveTrue(userId, bookId)) {
            throw new BusinessRuleException("Usuário já está na fila de espera para este livro.");
        }

        if (this.loanRepository.existsByUserIdAndBookIdAndIsReturnedFalse(userId, bookId)) {
            throw new BusinessRuleException("Usuário já está com este livro emprestado.");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setReservationDate(LocalDateTime.now());
        reservation.setActive(true);
        reservation.setNotified(false);

        return this.reservationRepository.save(reservation);
    }

    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = this.reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ObjectNotFoundException(
                        "Reserva não encontrada! Id: " + reservationId + ", Tipo: " + Reservation.class.getName()));

        checkOwnerOrStaff(reservation.getUser().getId());

        reservation.setActive(false);
        this.reservationRepository.save(reservation);
    }

    public List<Reservation> findReservationsByUser(Long userId) {
        checkOwnerOrStaff(userId);
        return this.reservationRepository.findByUserIdAndIsActiveTrue(userId);
    }

    //Full waiting queue for a book, administrative use, LIBRARIAN/ADMIN only
    public List<Reservation> findQueueForBook(Long bookId) {
        checkStaff();
        return this.reservationRepository.findByBookIdAndIsActiveTrueOrderByReservationDateAsc(bookId);
    }

    private void notifyNextInQueue(Book book) {
        this.reservationRepository.findFirstByBookIdAndIsActiveTrueOrderByReservationDateAsc(book.getId())
                .ifPresent(reservation -> {
                    reservation.setNotified(true);
                    this.reservationRepository.save(reservation);
                    logger.info("Livro '{}' disponível para o usuário {} (reserva {}).",
                            book.getTitle(), reservation.getUser().getEmail(), reservation.getId());
                });
    }

    //Authorization
    private void checkOwnerOrStaff(Long userId) {
        UserSpringSecurity userSpringSecurity = UserService.autheticated();
        if (Objects.isNull(userSpringSecurity)
                || !userSpringSecurity.hasRole(ProfileEnum.ADMIN) && !userSpringSecurity.hasRole(ProfileEnum.LIBRARIAN)
                && !userId.equals(userSpringSecurity.getId())) {
            throw new AuthorizationException("Acesso negado!");
        }
    }

    private void checkStaff() {
        UserSpringSecurity userSpringSecurity = UserService.autheticated();
        if (Objects.isNull(userSpringSecurity)
                || !userSpringSecurity.hasRole(ProfileEnum.ADMIN) && !userSpringSecurity.hasRole(ProfileEnum.LIBRARIAN)) {
            throw new AuthorizationException("Acesso negado! Apenas bibliotecários e administradores podem realizar esta ação.");
        }
    }
}

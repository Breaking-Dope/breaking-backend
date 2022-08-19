package com.dope.breaking.repository;

import com.dope.breaking.domain.financial.Purchase;
import com.dope.breaking.domain.financial.TransactionType;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.financial.QTransactionInfoResponseDto;
import com.dope.breaking.dto.financial.TransactionInfoResponseDto;
import com.dope.breaking.dto.post.WriterDto;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Objects;

import static com.dope.breaking.domain.financial.QTransaction.transaction;


@Repository
public class TransactionRepositoryCustomImpl implements TransactionRepositoryCustom{

    private final TransactionRepository transactionRepository;
    private final JPAQueryFactory queryFactory;

    public TransactionRepositoryCustomImpl(@Lazy TransactionRepository transactionRepository, @Lazy EntityManager em){
        this.transactionRepository = transactionRepository;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<TransactionInfoResponseDto> transactionList(User me, Long cursorId, int size){

        List<TransactionInfoResponseDto> content = queryFactory
                .select(new QTransactionInfoResponseDto(
                        transaction.id,
                        transaction.transactionDate,
                        transaction.transactionType.stringValue().toLowerCase(),
                        transaction.amount,
                        transaction.balance
                ))
                .from(transaction)
                .orderBy(transaction.transactionDate.desc())
                .where(
                        cursorPagination(cursorId),
                        transaction.user.eq(me)
                )
                .limit(size)
                .fetch();


        for (TransactionInfoResponseDto transactionInfoResponseDto : content) {

            if(Objects.equals(transactionInfoResponseDto.getTransactionType(), TransactionType.BUY_POST.getTitle())){

                Purchase purchase = transactionRepository.getById(transactionInfoResponseDto.getCursorId()).getPurchase();
                transactionInfoResponseDto.setPostId(purchase.getPost().getId());
                transactionInfoResponseDto.setPostTitle(purchase.getPost().getTitle());

                User seller = purchase.getPost().getUser();
                transactionInfoResponseDto.setTargetUser(new WriterDto(seller.getId(),seller.getCompressedProfileImgURL(),seller.getNickname()));

            }

            else if(Objects.equals(transactionInfoResponseDto.getTransactionType(), TransactionType.SELL_POST.getTitle())){

                Purchase purchase = transactionRepository.getById(transactionInfoResponseDto.getCursorId()).getPurchase();
                transactionInfoResponseDto.setPostId(purchase.getPost().getId());
                transactionInfoResponseDto.setPostTitle(purchase.getPost().getTitle());

                User buyer = purchase.getUser();
                transactionInfoResponseDto.setTargetUser(new WriterDto(buyer.getId(),buyer.getCompressedProfileImgURL(),buyer.getNickname()));

            }

        }

        return content;

    }

    private Predicate cursorPagination(Long cursorId) {
        if(cursorId == null || cursorId == 0) {
            return null;
        }
        return transaction.id.lt(cursorId);
    }

}

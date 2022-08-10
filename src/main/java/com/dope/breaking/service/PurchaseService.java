package com.dope.breaking.service;

import com.dope.breaking.domain.financial.Purchase;
import com.dope.breaking.domain.post.Post;
import com.dope.breaking.domain.post.PostType;
import com.dope.breaking.domain.user.User;
import com.dope.breaking.dto.user.ForListInfoResponseDto;
import com.dope.breaking.exception.auth.InvalidAccessTokenException;
import com.dope.breaking.exception.financial.NotEnoughBalanceException;
import com.dope.breaking.exception.post.NoSuchPostException;
import com.dope.breaking.exception.post.NotPurchasablePostException;
import com.dope.breaking.exception.post.SoldExclusivePostException;
import com.dope.breaking.exception.user.NoPermissionException;
import com.dope.breaking.repository.FollowRepository;
import com.dope.breaking.repository.PostRepository;
import com.dope.breaking.repository.PurchaseRepository;
import com.dope.breaking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final TransactionService transactionService;
    private final FollowRepository followRepository;

    public void purchasePost(String username, Long postId) {

        User buyer = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        Post post = postRepository.findById(postId).orElseThrow(NoSuchPostException::new);

        User seller = post.getUser();

        if (!post.getIsPurchasable()) {
            throw new NotPurchasablePostException();
        }

        if (post.getPostType() == PostType.FREE) {

            Purchase purchase = new Purchase(buyer, post, post.getPrice());
            purchaseRepository.save(purchase);
            post.updateIsSold(true);

        }

        else if (post.getPostType() ==PostType.CHARGED) {

            if (buyer.getBalance() < post.getPrice()) {
                throw new NotEnoughBalanceException();
            }

            moneyTransfer (buyer, seller, post.getPrice());
            Purchase purchase = new Purchase(buyer, post, post.getPrice());
            purchaseRepository.save(purchase);

            post.updateIsSold(true);

            transactionService.purchasePostTransaction(buyer, seller, purchase);

        }
        else if (post.getPostType() == PostType.EXCLUSIVE) {

            if (post.isSold()) {
                throw new SoldExclusivePostException();
            }

            if (buyer.getBalance() < post.getPrice()) {
                throw new NotEnoughBalanceException();
            }

            moneyTransfer (buyer, seller, post.getPrice());
            Purchase purchase = new Purchase(buyer, post, post.getPrice());
            purchaseRepository.save(purchase);

            post.updateIsSold(true);

            transactionService.purchasePostTransaction(buyer, seller, purchase);

        }

    }

    public void moneyTransfer(User buyer, User seller, int amount) {

        buyer.updateBalance(amount*(-1));
        seller.updateBalance(amount);

    }

    public List<ForListInfoResponseDto> purchaserList(String username, Long postId){

        User user = userRepository.findByUsername(username).orElseThrow(InvalidAccessTokenException::new);
        Post post = postRepository.findById(postId).orElseThrow(NoSuchPostException::new);

        if(user != post.getUser()){
            throw new NoPermissionException();
        }

        List<Purchase> purchaseList = purchaseRepository.findAllByPost(post);
        List<ForListInfoResponseDto> forListInfoResponseDtoList = new ArrayList<>();

        if(purchaseList != null){
            for (Purchase purchase : purchaseList) {
                User purchasedUser = purchase.getUser();
                boolean isFollowing = followRepository.existsFollowsByFollowedAndFollowing(purchasedUser,user);
                forListInfoResponseDtoList.add(new ForListInfoResponseDto(purchasedUser.getId(),purchasedUser.getNickname(),purchasedUser.getStatusMsg(),purchasedUser.getOriginalProfileImgURL(),isFollowing));
            }
        }

        return forListInfoResponseDtoList;

    }

}

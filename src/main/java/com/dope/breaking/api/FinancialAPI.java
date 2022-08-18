package com.dope.breaking.api;

import com.dope.breaking.domain.financial.TransactionType;
import com.dope.breaking.dto.financial.AmountRequestDto;
import com.dope.breaking.dto.financial.TransactionInfoResponseDto;
import com.dope.breaking.dto.user.ForListInfoResponseDto;
import com.dope.breaking.service.PurchaseService;
import com.dope.breaking.service.StatementService;
import com.dope.breaking.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class FinancialAPI {

    private final StatementService statementService;
    private final PurchaseService purchaseService;
    private final TransactionService transactionService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/financial/deposit")
    public ResponseEntity deposit(Principal principal,@RequestBody @Valid AmountRequestDto depositAmount) {

        statementService.depositOrWithdraw(principal.getName(), depositAmount.getAmount(), TransactionType.DEPOSIT);
        return ResponseEntity.ok().build();

    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/financial/withdraw")
    public ResponseEntity withdraw(Principal principal, @RequestBody @Valid AmountRequestDto withdrawAmount) {

        statementService.depositOrWithdraw(principal.getName(), withdrawAmount.getAmount(), TransactionType.WITHDRAW);
        return ResponseEntity.ok().build();

    }

    @PreAuthorize("isAuthenticated")
    @PostMapping("/post/{postId}/purchase")
    public ResponseEntity purchase(Principal principal, @PathVariable Long postId) {

        purchaseService.purchasePost(principal.getName(), postId);
        return ResponseEntity.ok().build();

    }

    @PreAuthorize("isAuthenticated")
    @GetMapping("/post/{postId}/buy-list")
    public ResponseEntity<List<ForListInfoResponseDto>> buyerList(Principal principal, @PathVariable Long postId, @RequestParam(value="cursor") Long cursorId, @RequestParam(value="size") int size){

        return ResponseEntity.status(HttpStatus.OK).body(purchaseService.purchaseList(principal.getName(), postId, cursorId, size));

    }

    @PreAuthorize("isAuthenticated")
    @GetMapping("/profile/transaction")
    public ResponseEntity<List<TransactionInfoResponseDto>> transactionList(Principal principal, @RequestParam(value="cursor") Long cursorId, @RequestParam(value="size") int size){

        return ResponseEntity.status(HttpStatus.OK).body(transactionService.transactionInfoList(principal.getName(), cursorId, size));

    }

}

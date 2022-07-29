package com.dope.breaking.api;

import com.dope.breaking.domain.financial.TransactionType;
import com.dope.breaking.dto.financial.AmountRequestDto;
import com.dope.breaking.service.PurchaseService;
import com.dope.breaking.service.StatementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequiredArgsConstructor
public class FinancialAPI {

    private final StatementService statementService;
    private final PurchaseService purchaseService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/financial/deposit")
    public ResponseEntity deposit (Principal principal,@RequestBody @Valid AmountRequestDto depositAmount) {

        statementService.depositOrWithdraw(principal.getName(), depositAmount.getAmount(), TransactionType.DEPOSIT);
        return ResponseEntity.ok().build();

    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/financial/withdraw")
    public ResponseEntity withdraw (Principal principal, @RequestBody @Valid AmountRequestDto withdrawAmount) {

        statementService.depositOrWithdraw(principal.getName(), withdrawAmount.getAmount(), TransactionType.WITHDRAW);
        return ResponseEntity.ok().build();

    }

    @PreAuthorize("isAuthenticated")
    @PostMapping("/post/{postId}/purchase")
    public ResponseEntity purchase (Principal principal, @PathVariable Long postId) {

        purchaseService.purchasePost(principal.getName(), postId);
        return ResponseEntity.ok().build();

    }

}

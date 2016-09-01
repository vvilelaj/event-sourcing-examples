package net.chrisrichardson.eventstore.javaexamples.banking.web.queryside.accounts;

import net.chrisrichardson.eventstore.javaexamples.banking.backend.queryside.accounts.AccountInfo;

import net.chrisrichardson.eventstore.javaexamples.banking.backend.queryside.accounts.AccountNotFoundException;
import net.chrisrichardson.eventstore.javaexamples.banking.backend.queryside.accounts.AccountQueryService;
import net.chrisrichardson.eventstore.javaexamples.banking.common.accounts.AccountHistoryEntry;
import net.chrisrichardson.eventstore.javaexamples.banking.common.accounts.AccountHistoryResponse;
import net.chrisrichardson.eventstore.javaexamples.banking.common.accounts.AccountTransactionInfo;
import net.chrisrichardson.eventstore.javaexamples.banking.common.accounts.GetAccountResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
public class AccountQueryController {

  private AccountQueryService accountInfoQueryService;

  @Autowired
  public AccountQueryController(AccountQueryService accountInfoQueryService) {
    this.accountInfoQueryService = accountInfoQueryService;
  }

  @RequestMapping(value = "/accounts/{accountId}", method = RequestMethod.GET)
  public CompletableFuture<GetAccountResponse> get(@PathVariable String accountId) {
    return accountInfoQueryService.findByAccountId(accountId)
            .thenApply(accountInfo -> new GetAccountResponse(accountInfo.getId(), new BigDecimal(accountInfo.getBalance()), accountInfo.getTitle(), accountInfo.getDescription()));
  }

  @RequestMapping(value = "/accounts", method = RequestMethod.GET)
  public CompletableFuture<List<GetAccountResponse>> getAccountsForCustomer(@RequestParam("customerId") String customerId) {
    return accountInfoQueryService.findByCustomerId(customerId)
            .thenApply(accountInfoList -> accountInfoList.stream().map(accountInfo -> new GetAccountResponse(accountInfo.getId(), new BigDecimal(accountInfo.getBalance()), accountInfo.getTitle(), accountInfo.getDescription())).collect(Collectors.toList()));
  }

  @RequestMapping(value = "/accounts/{accountId}/history", method = RequestMethod.GET)
  public CompletableFuture<AccountHistoryResponse> getTransactionsHistory(@PathVariable String accountId) {
    CompletableFuture<AccountHistoryResponse> res = accountInfoQueryService.findByAccountId(accountId)
            .thenApply(accountInfo -> new AccountHistoryResponse(new AccountHistoryEntry(accountInfo.getDate()),
                            accountInfo.getTransactions(),
                            accountInfo.getChanges())
            );

    return res;
  }

  @ResponseStatus(value= HttpStatus.NOT_FOUND, reason="account not found")
  @ExceptionHandler(AccountNotFoundException.class)
  public void accountNotFound() {
  }
}

package com.kkk.op.user.application.service.impl;

import com.kkk.op.support.models.command.AccountCreateCommand;
import com.kkk.op.support.models.command.AccountUpdateCommand;
import com.kkk.op.support.models.dto.AccountDTO;
import com.kkk.op.support.types.LongId;
import com.kkk.op.user.application.service.AccountApplicationService;
import com.kkk.op.user.assembler.AccountDTOAssembler;
import com.kkk.op.user.domain.entity.Account;
import com.kkk.op.user.domain.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author KaiKoo
 */
@Service
public class AccountApplicationServiceImpl implements AccountApplicationService {

    private final AccountDTOAssembler accountDTOAssembler = AccountDTOAssembler.INSTANCE;

    private final AccountService accountService;

    public AccountApplicationServiceImpl(@Autowired AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public long createAccount(AccountCreateCommand createCommand) {
        // 转换对象
        var account = Account.builder()
                .userId(LongId.valueOf(createCommand.getUserId()))
                .build();
        // 行为发生
        account.save(accountService);
        // todo... 触发事件

        // 返回id
        return account.getId().getValue();
    }

    @Override
    public void updateAccount(AccountUpdateCommand updateCommand) {
        // 转换对象
        var account = Account.builder()
                .id(LongId.valueOf(updateCommand.getId()))
                .userId(LongId.valueOf(updateCommand.getUserId()))
                .build();
        // 行为发生
        account.save(accountService);
        // todo... 触发事件

    }

    @Override
    public void deleteAccount(Long id) {
        var account = Account.builder().id(LongId.valueOf(id)).build();
        account.remove(accountService);
    }

    @Override
    public AccountDTO queryAccountById(Long id) {
        return accountDTOAssembler.toDTO(accountService.find(LongId.valueOf(id)));
    }

}

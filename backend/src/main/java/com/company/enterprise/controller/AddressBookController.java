package com.company.enterprise.controller;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.Result;
import com.company.enterprise.common.annotation.RequireRoles;
import static com.company.enterprise.common.RoleCodes.GENERAL_MANAGER;
import static com.company.enterprise.common.RoleCodes.HR_MANAGER;
import com.company.enterprise.dto.AddressBookQuery;
import com.company.enterprise.dto.AddressBookRequest;
import com.company.enterprise.entity.AddressBook;
import com.company.enterprise.service.AddressBookService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/address-books")
public class AddressBookController {

    private final AddressBookService addressBookService;

    public AddressBookController(AddressBookService addressBookService) {
        this.addressBookService = addressBookService;
    }

    @GetMapping("/page")
    public Result<PageResult<AddressBook>> page(@Valid AddressBookQuery query) {
        return Result.success(addressBookService.page(query));
    }

    @GetMapping("/{id}")
    public Result<AddressBook> detail(@PathVariable Long id) {
        return Result.success(addressBookService.detail(id));
    }

    @PostMapping
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    public Result<Long> create(@Valid @RequestBody AddressBookRequest request) {
        return Result.success(addressBookService.create(request));
    }

    @PutMapping
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    public Result<Void> update(@Valid @RequestBody AddressBookRequest request) {
        addressBookService.update(request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireRoles({GENERAL_MANAGER, HR_MANAGER})
    public Result<Void> delete(@PathVariable Long id) {
        addressBookService.delete(id);
        return Result.success();
    }
}

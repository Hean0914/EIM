package com.company.enterprise.service;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.dto.AddressBookQuery;
import com.company.enterprise.dto.AddressBookRequest;
import com.company.enterprise.entity.AddressBook;

public interface AddressBookService {

    PageResult<AddressBook> page(AddressBookQuery query);

    AddressBook detail(Long id);

    Long create(AddressBookRequest request);

    void update(AddressBookRequest request);

    void delete(Long id);
}

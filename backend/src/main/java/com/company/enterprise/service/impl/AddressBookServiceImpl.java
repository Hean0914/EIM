package com.company.enterprise.service.impl;

import com.company.enterprise.common.PageResult;
import com.company.enterprise.common.exception.BusinessException;
import com.company.enterprise.dto.AddressBookQuery;
import com.company.enterprise.dto.AddressBookRequest;
import com.company.enterprise.entity.AddressBook;
import com.company.enterprise.mapper.AddressBookMapper;
import com.company.enterprise.service.AddressBookService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {

    private final AddressBookMapper addressBookMapper;

    public AddressBookServiceImpl(AddressBookMapper addressBookMapper) {
        this.addressBookMapper = addressBookMapper;
    }

    @Override
    public PageResult<AddressBook> page(AddressBookQuery query) {
        long current = ServiceSupport.normalizeCurrent(query.getCurrent());
        long size = ServiceSupport.normalizeSize(query.getSize());
        long offset = (current - 1) * size;
        Long total = addressBookMapper.countAddressPage(query);
        List<AddressBook> records = total == null || total == 0
                ? List.of()
                : addressBookMapper.selectAddressPage(query, offset, size);
        return new PageResult<>(total == null ? 0 : total, current, size, records);
    }

    @Override
    public AddressBook detail(Long id) {
        return requireAddress(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(AddressBookRequest request) {
        AddressBook addressBook = new AddressBook();
        BeanUtils.copyProperties(request, addressBook);
        normalize(addressBook);
        addressBookMapper.insertAddress(addressBook);
        return addressBook.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(AddressBookRequest request) {
        if (request.getId() == null) {
            throw new BusinessException("通讯录ID不能为空");
        }
        requireAddress(request.getId());
        AddressBook addressBook = new AddressBook();
        BeanUtils.copyProperties(request, addressBook);
        normalize(addressBook);
        addressBookMapper.updateAddress(addressBook);
    }

    @Override
    public void delete(Long id) {
        requireAddress(id);
        addressBookMapper.logicDeleteAddressById(id);
    }

    private AddressBook requireAddress(Long id) {
        if (id == null) {
            throw new BusinessException("通讯录ID不能为空");
        }
        AddressBook addressBook = addressBookMapper.selectAddressById(id);
        if (addressBook == null) {
            throw new BusinessException("通讯录记录不存在");
        }
        return addressBook;
    }

    private void normalize(AddressBook addressBook) {
        if (addressBook.getSortOrder() == null) {
            addressBook.setSortOrder(0);
        }
        if (addressBook.getStatus() == null) {
            addressBook.setStatus(1);
        }
    }
}

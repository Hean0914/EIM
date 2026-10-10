package com.company.enterprise.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.enterprise.dto.AddressBookQuery;
import com.company.enterprise.entity.AddressBook;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AddressBookMapper extends BaseMapper<AddressBook> {

    List<AddressBook> selectAddressPage(@Param("query") AddressBookQuery query,
                                        @Param("offset") Long offset,
                                        @Param("size") Long size);

    Long countAddressPage(@Param("query") AddressBookQuery query);

    AddressBook selectAddressById(@Param("id") Long id);

    int insertAddress(AddressBook addressBook);

    int updateAddress(AddressBook addressBook);

    int logicDeleteAddressById(@Param("id") Long id);
}

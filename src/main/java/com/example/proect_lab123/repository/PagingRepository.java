package com.example.proect_lab123.repository;
import com.example.proect_lab123.domain.Entity;
import com.example.proect_lab123.util.paging.Page;
import com.example.proect_lab123.util.paging.Pageable;

import java.util.Optional;

public interface PagingRepository<ID , E extends Entity<ID>> extends RepositoryWithOptional<ID, E> {

    //Optional<E> findOne(Long id);

    Page<E> findAllOnPage(Pageable pageable);
    //Page<E> findAllOnPage(Pageable pageable,String type);  Este mai bun/complex cu DuckFilterDTO,asa ca nu mai am nevoie de String type
    //<T extends FilterDTO> Page<E> findAllOnPage(Pageable pageable, T filter);
}

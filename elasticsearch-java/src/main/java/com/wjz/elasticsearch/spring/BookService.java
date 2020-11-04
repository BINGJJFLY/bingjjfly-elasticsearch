package com.wjz.elasticsearch.spring;

import org.springframework.beans.factory.annotation.Autowired;

public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public void createDoc() {
        Book book = new Book();
        bookRepository.save(book);
    }
}

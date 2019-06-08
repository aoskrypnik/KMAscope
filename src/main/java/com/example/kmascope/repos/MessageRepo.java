package com.example.kmascope.repos;

import com.example.kmascope.domain.Message;
import com.example.kmascope.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface MessageRepo extends CrudRepository<Message, Integer> {

    Page<Message> findByTag(String tag, Pageable pageable);

    Page<Message> findAll(Pageable pageable);

    Page<Message> findByAuthor(User author, Pageable pageable);
}

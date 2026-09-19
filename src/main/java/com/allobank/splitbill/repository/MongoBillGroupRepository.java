package com.allobank.splitbill.repository;

import com.allobank.splitbill.repository.document.BillGroupDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoBillGroupRepository extends MongoRepository<BillGroupDocument, String> {}

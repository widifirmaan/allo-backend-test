package com.allobank.splitbill.repository;

import com.allobank.splitbill.model.BillGroup;
import com.allobank.splitbill.repository.document.BillGroupDocument;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class MongoBillGroupStore implements BillGroupStore {

  private final MongoBillGroupRepository mongoRepository;

  public MongoBillGroupStore(MongoBillGroupRepository mongoRepository) {
    this.mongoRepository = mongoRepository;
  }

  @Override
  public BillGroup saveNewGroup(String groupName, List<String> participantNames) {
    BillGroup grupBaru = new BillGroup(UUID.randomUUID().toString(), groupName, participantNames);
    mongoRepository.save(BillGroupDocument.fromDomain(grupBaru));
    return grupBaru;
  }

  @Override
  public BillGroup save(BillGroup grup) {
    mongoRepository.save(BillGroupDocument.fromDomain(grup));
    return grup;
  }

  @Override
  public Optional<BillGroup> findById(String groupId) {
    return mongoRepository.findById(groupId).map(BillGroupDocument::toDomain);
  }

  @Override
  public boolean deleteById(String groupId) {
    if (!mongoRepository.existsById(groupId)) {
      return false;
    }
    mongoRepository.deleteById(groupId);
    return true;
  }
}

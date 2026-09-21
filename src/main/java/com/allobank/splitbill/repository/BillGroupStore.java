package com.allobank.splitbill.repository;

import com.allobank.splitbill.exception.GroupNotFoundException;
import com.allobank.splitbill.model.BillGroup;
import java.util.List;
import java.util.Optional;

public interface BillGroupStore {

  BillGroup saveNewGroup(String groupName, List<String> participantNames);

  BillGroup save(BillGroup grup);

  Optional<BillGroup> findById(String groupId);

  /** Hapus grup. False kalau groupId tidak ada. */
  boolean deleteById(String groupId);

  /** Ambil semua grup dari database. */
  List<BillGroup> findAll();

  default BillGroup findByIdOrThrow(String groupId) {
    return findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
  }
}

package com.carlisenceplate.lisenceplate.repository;

import com.carlisenceplate.lisenceplate.entity.FileData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileDataRepository  extends JpaRepository<FileData,Integer> {
}

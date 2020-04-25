package com.equakelyzer.earthquakedataservice.repository;

import com.equakelyzer.earthquakedataservice.repository.state.EarthQuakeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EarthQuakeHistoryRepository extends JpaRepository<EarthQuakeHistory, Integer> {
}

package br.ifsp.events.service;

import br.ifsp.events.dto.time.CapitaoTransferDTO;
import br.ifsp.events.dto.time.TimeCreateDTO;
import br.ifsp.events.dto.time.TimeUpdateDTO;
import br.ifsp.events.dto.time.TimeResponseDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

public interface TimeService {

    /**
     * @param createDTO
     * @param auth
     * @return
     */
    TimeResponseDTO createTime(TimeCreateDTO createDTO, Authentication auth);

    /**
     * @param timeId
     * @param updateDTO
     * @param auth
     * @return
     */
    TimeResponseDTO updateTime(Long timeId, TimeUpdateDTO updateDTO, Authentication auth);

    /**
     * @param timeId
     * @param transferDTO
     * @param auth
     * @return
     */
    TimeResponseDTO transferCaptaincy(Long timeId, CapitaoTransferDTO transferDTO, Authentication auth);

    /**
     * @param timeId
     * @param auth
     */
    void deleteTime(Long timeId, Authentication auth);

    /**
     * @param timeId
     * @return
     */
    TimeResponseDTO getTimeById(Long timeId);


    /**
     * @param pageable
     * @return
     */
    Page<TimeResponseDTO> listAllTimes(Pageable pageable);
}
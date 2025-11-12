package br.ifsp.events.repository;

import br.ifsp.events.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    
    Page<Post> findAllByComunidadeId(Long comunidadeId, Pageable pageable);
}

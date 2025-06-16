package it.overzoom.taf.service;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.overzoom.taf.model.News;
import it.overzoom.taf.repository.NewsRepository;

@Service
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;

    public NewsServiceImpl(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @Override
    public Page<News> findAll(Pageable pageable) {
        return newsRepository.findAll(pageable);
    }

    @Override
    public Optional<News> findById(String id) {
        return newsRepository.findById(id);
    }

    @Override
    public boolean existsById(String id) {
        return newsRepository.existsById(id);
    }

    @Override
    @Transactional
    public News create(News news) {
        return newsRepository.save(news);
    }

    @Override
    @Transactional
    public Optional<News> update(News news) {
        return newsRepository.findById(news.getId()).map(existing -> {
            existing.setTitle(news.getTitle());
            existing.setContent(news.getContent());
            existing.setAuthor(news.getAuthor());
            existing.setPublicationDate(news.getPublicationDate());
            existing.setTags(news.getTags());
            return existing;
        }).map(newsRepository::save);
    }

    @Override
    @Transactional
    public Optional<News> partialUpdate(String id, News news) {
        return newsRepository.findById(id).map(existing -> {
            if (news.getTitle() != null)
                existing.setTitle(news.getTitle());
            if (news.getContent() != null)
                existing.setContent(news.getContent());
            if (news.getAuthor() != null)
                existing.setAuthor(news.getAuthor());
            if (news.getPublicationDate() != null)
                existing.setPublicationDate(news.getPublicationDate());
            if (news.getTags() != null)
                existing.setTags(news.getTags());
            return existing;
        }).map(newsRepository::save);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        newsRepository.deleteById(id);
    }

}

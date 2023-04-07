package com.iuj.backend.api.service;

import com.iuj.backend.api.domain.entity.FavFilter;
import com.iuj.backend.api.domain.entity.News;
import com.iuj.backend.api.domain.entity.SearchRegion;
import com.iuj.backend.api.domain.entity.ViewNews;
import com.iuj.backend.api.domain.enums.NewsCategory;
import com.iuj.backend.api.domain.enums.Sido;
import com.iuj.backend.api.repository.NewsRepository;
import com.iuj.backend.api.repository.SearchRegionRepository;
import com.iuj.backend.api.repository.ViewNewsRepository;
import com.iuj.backend.api.repository.recomm.RecommRepository;
import com.iuj.backend.util.NewsUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@AllArgsConstructor
public class NewsService {

    private final static int MAX_NEWS_NUM = 8;

    private final RecommRepository recommRepository;
    private final NewsRepository newsRepository;
    private final SearchRegionRepository searchRegionRepository;
    private final ViewNewsRepository viewNewsRepository;

    /**
     * 뉴스 클릭한 내역 저장
     * 오늘 같은 뉴스를 클릭했을 경우 updatedAt 컬럼 수정
     * */
    public void saveViewNews(int newsId, String email){
        System.out.println(viewNewsRepository.getViewNewsByEmail(email));
        ViewNews viewNews = viewNewsRepository.getOneTodayViewByEmailAndNewsId(email, newsId);
        if(viewNews != null){
            viewNews.setUpdatedAt(LocalDateTime.now());
            viewNewsRepository.save(viewNews);
        } else{
            viewNewsRepository.save(ViewNews.builder()
                    .email(email)
                    .newsId(newsId)
                    .build());
        }
    }

    /**
     * 로그인하지 않은 유저 - 메인에서 보여줄 뉴스 리스트
     * */
    public List<News> getNewsList(){
        List<News> list = newsRepository.getNewsBySchoolAndLocalEqualsLimit(NewsCategory.getRandom().getName(), Sido.ALL.getName(), MAX_NEWS_NUM);
        return list;
    }

    /**
     * 로그인한 유저 - 메인에서 보여줄 뉴스 리스트
     * */
    public List<News> getNewsListForUser(String email){
        List<News> result = new ArrayList<>();
        ViewNews viewNews = viewNewsRepository.getTopMyRecentView(email);
        System.out.println(viewNews);

        if(viewNews == null){
            System.out.println("viewNews XXXX");
            // 선호필터 중 가장 많이 등록된 학군 카테고리로 뉴스 카테고리 가져오기
            List<FavFilter> favFilters = recommRepository.findByEmail(email);
            NewsCategory category = NewsUtil.getCategory(favFilters);

            // 최근 검색 지역, 없을 경우 전국으로 자동 설정
            SearchRegion region = searchRegionRepository.findById(email).orElse(null);
            Sido sido = Sido.findByName(region != null ? region.getSido() : null);
            List<News> filteredNewsList = newsRepository.getNewsBySchoolAndLocalEqualsLimit(category.getName(), sido.getName(), MAX_NEWS_NUM);

            result.addAll(filteredNewsList);

            int listSize = filteredNewsList.size();
            if(listSize < 6) {
                // 부족한 값 채우기
                int limit = MAX_NEWS_NUM - listSize;
                List<News> ramdomNewsList = newsRepository.getRandomLimit(limit);

                // 결과 추가
                result.addAll(ramdomNewsList);
            }
        } else{
            System.out.println("viewNews 잇음");
            // 가장 최근 본 뉴스 정보
            News recentViewNews = newsRepository.findById(viewNews.getNewsId()).orElseThrow(InternalError::new);

            // 가장 최근 본 뉴스와 비슷한 뉴스 리스트
            List<Integer> similarNewsIdList = recentViewNews.getSimilarList();
            
            // 이전에 본 적 있는 뉴스 리스트 -> id만 리스트로 따로 만들기
            List<ViewNews> recentViewNewsList = viewNewsRepository.getMyRecentView(email);
            List<Integer> recentViewIdList = new ArrayList<>();
            for(ViewNews v : recentViewNewsList){
                recentViewIdList.add(v.getNewsId());
            }
            
            // 비슷한 뉴스 id 리스트에서 이전에 본 적 있는 뉴스 id 리스트 제거
            similarNewsIdList.removeAll(recentViewIdList);

            if(similarNewsIdList.isEmpty()){
                result = newsRepository.getNewsByIdIsNotInOrderByRandomLimit(recentViewIdList, MAX_NEWS_NUM);
            }else{
                List<News> similarNewsList = newsRepository.getNewsByIdIsIn(similarNewsIdList);

                int listSize = similarNewsList.size();
                int limit = MAX_NEWS_NUM - listSize;

                // 중복 제거하여 검색
                List<Integer> removeIdList = new ArrayList<>();
                removeIdList.addAll(recentViewIdList);
                removeIdList.addAll(similarNewsIdList);
                List<News> ramdomNewsList = newsRepository.getNewsByIdIsNotInOrderByRandomLimit(removeIdList, limit);

                result.addAll(ramdomNewsList);
                result.addAll(similarNewsList);
            }

        }
        
//        // 선호필터 중 가장 많이 등록된 학군 카테고리로 뉴스 카테고리 가져오기
//        List<FavFilter> favFilters = recommRepository.findByEmail(email);
//        NewsCategory category = NewsUtil.getCategory(favFilters);
//
//        // 최근 검색 지역, 없을 경우 전국으로 자동 설정
//        SearchRegion region = searchRegionRepository.findById(email).orElse(null);
//        Sido sido = Sido.findByName(region != null ? region.getSido() : null);
//        result = newsRepository.getTop6BySchoolAndLocalEquals(category.getName(), sido.getName(), 6);

        return result;
    }
}

package com.iuj.backend.api.repository.building;

import com.iuj.backend.api.domain.dto.request.BasicFilter;
import com.iuj.backend.api.domain.dto.response.AverageDeal;
import com.iuj.backend.api.domain.dto.response.BuildingDto;
import com.iuj.backend.api.domain.enums.BuildingType;
import com.iuj.backend.api.domain.enums.DealType;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JDBCBuildingRepository {

    private final JdbcTemplate jdbcTemplate;
    private final String START_QUERY = "select building.id, name, road_addr, lat, lng, deal_type, concat(building.sigungu,' ', building.bungi) as addr, min(floor) as floor_min, max(floor) as floor_max, min(area) as area_min, max(area) as area_max, ";
    private final String FIRST_INLINE_QUERY_START = "SELECT id, name, road_addr, lat, lng, sigungu, bungi FROM ";
    private final String FIRST_INLINE_QUERY_WHERE = "where lat > ? and lng > ? and lat < ? and lng < ? ";

    /**
     * 건물 리스트 쿼리의 맨 바깥의 select문 만들어줌
     * @return
     * */
    private StringBuilder initQuery(DealType dealType){
        StringBuilder query = new StringBuilder(START_QUERY);

        if(dealType.equals(DealType.BUY)){
            query.append("avg(deal.price) ");
        } else if(dealType.equals(DealType.LONG_TERM_RENT)){
            query.append("avg(deal.guarantee) ");
        } else {
            query.append("avg(deal.guarantee), avg(deal.monthly) ");
        }
        query.append(" from (");

        return query;
    }

    public List<BuildingDto> getBuildingList(String[] sw, String[] ne, DealType dealType, BuildingType buildingType, BasicFilter filter){
        try {

            String buildingTableName = buildingType.getName();
            String dealTableName = buildingTableName + "_deal";
            String FkId = buildingTableName + "_id";

            List<String> queryArgs = new ArrayList<>();
            queryArgs.addAll(Arrays.asList(sw));
            queryArgs.addAll(Arrays.asList(ne));
            queryArgs.add(dealType.getName());

            StringBuilder query = initQuery(dealType);
            query.append(FIRST_INLINE_QUERY_START)
                    .append(' ').append(buildingTableName).append(' ')
                    .append(FIRST_INLINE_QUERY_WHERE)
                    .append(") building ")
                    .append(" left join ( ")
                    .append(" select floor, area, deal_type,")
                    .append(FkId)
                    .append(',');

            if(dealType.equals(DealType.BUY)){
                query.append(" price ");
            } else if(dealType.equals(DealType.LONG_TERM_RENT)){
                query.append(" guarantee ");
            } else {
                query.append(" guarantee, monthly ");
            }

            query.append(' ')
                    .append(" from ")
                    .append(dealTableName)
                    .append(" where deal_type = ?")
                    .append(" and area >= ? and area < ? ")
                    .append(" and floor >= ? and floor <= ? ");

            queryArgs.addAll(Arrays.stream(filter.getExtent())
                    .mapToObj(String::valueOf)
                    .collect(Collectors.toList()));
            queryArgs.addAll(Arrays.stream(filter.getFloor())
                    .mapToObj(String::valueOf)
                    .collect(Collectors.toList()));

            if(dealType.equals(DealType.BUY)){
                query.append(" and price >= ? and price <= ? ");
                queryArgs.addAll(Arrays.stream(filter.getPrice())
                        .mapToObj(String::valueOf)
                        .collect(Collectors.toList()));
            } else if(dealType.equals(DealType.LONG_TERM_RENT)){
                query.append(" and guarantee >= ? and guarantee <= ? ");
                queryArgs.addAll(Arrays.stream(filter.getPrice())
                        .mapToObj(String::valueOf)
                        .collect(Collectors.toList()));
            } else {
                query.append(" and guarantee >= ? and guarantee <= ? ")
                        .append(" and monthly >= ? and monthly <= ? ");
                queryArgs.addAll(Arrays.stream(filter.getPrice())
                        .mapToObj(String::valueOf)
                        .collect(Collectors.toList()));
                queryArgs.addAll(Arrays.stream(filter.getPrice())
                        .mapToObj(String::valueOf)
                        .collect(Collectors.toList()));
            }


            query.append(" ) deal ")
                    .append(" on building.id =  ")
                    .append(FkId)
                    .append(" group by building.id ");



            System.out.println(query);
            System.out.println(queryArgs);

            if(dealType.equals(DealType.BUY)){
                return jdbcTemplate.query(query.toString(), rowBuyMapper, queryArgs.toArray());
            } else if(dealType.equals(DealType.LONG_TERM_RENT)){
                return jdbcTemplate.query(query.toString(), rowLongTermRentMapper, queryArgs.toArray());
            } else {
                return jdbcTemplate.query(query.toString(), rowMonthlyRentMapper, queryArgs.toArray());
            }
        } catch(EmptyResultDataAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    static RowMapper<BuildingDto> rowBuyMapper = (rs, rowNum) -> BuildingDto.builder()
            .id(rs.getLong("building.id"))
            .name(rs.getString("name"))
            .address(new String[]{rs.getString("road_addr"), rs.getString("addr")})
            .latlng(new Double[]{Double.parseDouble(rs.getString("lat")), Double.parseDouble(rs.getString("lng"))})
            .rangeFloor(new int[]{rs.getInt("floor_min"), rs.getInt("floor_max")})
            .rangeExtent(new Double[]{rs.getDouble("area_min"), rs.getDouble("area_max")})
            .averageDeal(AverageDeal.builder()
                    .deal_type(DealType.findByName(rs.getString("deal_type")))
                    .price((int) Math.round(rs.getDouble("avg(deal.price)")))
                    .build()
            )
            .build();

    static RowMapper<BuildingDto> rowLongTermRentMapper = (rs, rowNum) -> BuildingDto.builder()
            .id(rs.getLong("building.id"))
            .name(rs.getString("name"))
            .address(new String[]{rs.getString("road_addr"), rs.getString("addr")})
            .latlng(new Double[]{Double.parseDouble(rs.getString("lat")), Double.parseDouble(rs.getString("lng"))})
            .rangeFloor(new int[]{rs.getInt("floor_min"), rs.getInt("floor_max")})
            .rangeExtent(new Double[]{rs.getDouble("area_min"), rs.getDouble("area_max")})
            .averageDeal(AverageDeal.builder()
                    .deal_type(DealType.findByName(rs.getString("deal_type")))
                    .guarantee((int) Math.round(rs.getDouble("avg(deal.guarantee)")))
                    .build()
            )
            .build();

    static RowMapper<BuildingDto> rowMonthlyRentMapper = (rs, rowNum) -> BuildingDto.builder()
            .id(rs.getLong("building.id"))
            .name(rs.getString("name"))
            .address(new String[]{rs.getString("road_addr"), rs.getString("addr")})
            .latlng(new Double[]{Double.parseDouble(rs.getString("lat")), Double.parseDouble(rs.getString("lng"))})
            .rangeFloor(new int[]{rs.getInt("floor_min"), rs.getInt("floor_max")})
            .rangeExtent(new Double[]{rs.getDouble("area_min"), rs.getDouble("area_max")})
            .averageDeal(AverageDeal.builder()
                    .deal_type(DealType.findByName(rs.getString("deal_type")))
                    .guarantee((int) Math.round(rs.getDouble("avg(deal.guarantee)")))
                    .monthly((int) Math.round(rs.getDouble("avg(deal.monthly)")))
                    .build()
            )
            .build();


}
